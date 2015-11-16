package com.andbase.tractor.http;

import android.annotation.TargetApi;
import android.os.Build;

import com.andbase.tractor.Constants.Constants;
import com.andbase.tractor.handler.LoadHandler;
import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.andbase.tractor.utils.HandlerUtils;
import com.andbase.tractor.utils.LogUtils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;



/**
 * Created by xiaoqian.hu on 2015/10/15.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class OKHttp implements HttpBase {

	private static final OkHttpClient mOkHttpClient = new OkHttpClient();

	static {
		mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
		mOkHttpClient.networkInterceptors().add(new RedirectInterceptor());
		int versionCode = Build.VERSION.SDK_INT;
		if(versionCode>=9){
			mOkHttpClient.setCookieHandler(new CookieManager(null,
					CookiePolicy.ACCEPT_ORIGINAL_SERVER));
		}
	}

	static class RedirectInterceptor implements Interceptor {
		@Override
		public Response intercept(Chain chain) throws IOException {
			Request originalRequest = chain.request();
			Response response = chain.proceed(originalRequest);
			return response;
		}
	}

	@Override
	public CallWrap get(String url, LoadListener listener, Object... tag) {
		Request.Builder builder = getBuilder().url(url);
		addTag(builder, tag);
		return execute(builder.build(), listener, getTag(tag));
	}

	@Override
	public CallWrap post(String url, String params, LoadListener listener,
			Object... tag) {
		Request.Builder builder = getBuilder().url(url).post(
				RequestBody.create(MediaTypeWrap.MEDIA_TYPE_MARKDOWN, params));
		addTag(builder, tag);
		return execute(builder.build(), listener, getTag(tag));
	}

	@Override
	public CallWrap post(String url, HashMap<String, String> header,
			String params, LoadListener listener, Object... tag) {
		Request.Builder builder = getBuilder().url(url).post(
				RequestBody.create(MediaTypeWrap.MEDIA_TYPE_MARKDOWN, params));
		if (header != null) {
			for (HashMap.Entry<String, String> entry : header.entrySet()) {
				builder.addHeader(entry.getKey(), entry.getValue());
			}
		}
		addTag(builder, tag);
		return execute(builder.build(), listener, getTag(tag));
	}

	@Override
	public CallWrap post(String url, HashMap<String, String> params,
			LoadListener listener, Object... tag) {
		RequestBody formBody = addParams(params);
		if (formBody == null) {
			listener.onFail("params is null");
			return null;
		} else {
			Request.Builder builder = getBuilder().url(url).post(formBody);
			addTag(builder, tag);
			return execute(builder.build(), listener, getTag(tag));
		}
	}

	public RequestBody addParams(HashMap<String, String> params) {
		FormEncodingBuilder formbuiBuilder = new FormEncodingBuilder();
		if (params != null && params.size() > 0) {
			for (String key : params.keySet()) {
				formbuiBuilder.add(key, params.get(key));
			}
			return formbuiBuilder.build();
		}
		return null;
	}

	public void addTag(Request.Builder builder, Object... tag) {
		if (tag != null && tag.length == 1) {
			builder.tag(tag[0]);
		}
	}

	private Object getTag(Object... tag) {
		if (tag != null && tag.length == 1) {
			return tag[0];
		}
		return null;
	}

	@Override
	public void cancel(Object... tag) {
		if (tag != null && tag.length == 1) {
			for (int i = 0; i < tag.length; i++) {
				mOkHttpClient.cancel(tag[i]);
			}
		}

	}

	private Request.Builder getBuilder() {
		return new Request.Builder();
	}

	private static CallWrap execute(final Request request,
			final LoadListener listener, Object tag) {
		final LoadHandler handler = new LoadHandler(listener);
		CallWrap callWrap = new CallWrap();
		final Call call = mOkHttpClient.newCall(request);
		callWrap.setCall(call);
		Task netWorkTask = new Task(tag, handler) {
			@Override
			public void onRun() {
				try {
					Response response = call.execute();
					if (!response.isSuccessful()) {
						notifyFail(new IOException("Unexpected code "
								+ response));
					} else {
						String result = response.body().string();
						LogUtils.d("okresult=" + result);
						notifySuccess(result);
					}
				} catch (Exception e) {
					if (e.toString().toLowerCase().contains("canceled")
							|| e.toString().toLowerCase().contains("closed")) {
						notifyCancel(e);
					} else {
						notifyFail(e);
					}
					e.printStackTrace();
				}
			}

			@Override
			public void cancelTask() {
				call.cancel();
			}
		};
		TaskPool.getInstance().execute(netWorkTask);
		return callWrap;
	}

	public static void enqueue(Request request, final LoadListener listener) {
		final LoadHandler handler = new LoadHandler(listener);
		HandlerUtils.sendMsg(handler, Constants.LOAD_START);
		mOkHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Response response) throws IOException {
				if (!response.isSuccessful()) {
					HandlerUtils.sendMsg(handler, Constants.LOAD_FAIL,
							new IOException("Unexpected code " + response));
				} else {
					HandlerUtils.sendMsg(handler, Constants.LOAD_SUCCESS,
							response.body().string());
				}
				// Headers responseHeaders = response.headers();
				// for (int i = 0; i < responseHeaders.size(); i++) {
				// System.out.println(responseHeaders.name(i) + ": " +
				// responseHeaders.value(i));
				// }
				// System.out.println(response.body().string());

			}

			@Override
			public void onFailure(Request request, IOException exception) {
				exception.printStackTrace();
				HandlerUtils.sendMsg(handler, Constants.LOAD_FAIL, exception);
			}
		});
	}

}
