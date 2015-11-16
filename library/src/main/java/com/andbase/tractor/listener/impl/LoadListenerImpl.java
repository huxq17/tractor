package com.andbase.tractor.listener.impl;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andbase.tractor.listener.LoadListener;
import com.andbase.tractor.utils.BackGroudSeletor;
import com.andbase.tractor.utils.DensityUtil;

public class LoadListenerImpl implements LoadListener {
	private Context context;
	private Dialog mProgressDialog;
	private String mMessage;

	public LoadListenerImpl() {
	}

	public LoadListenerImpl(Context context) {
		this.context = context;
		if (context != null) {
			initProgressDialog(null);
		}
	}

	public LoadListenerImpl(Context context, String Message) {
		this.context = context;
		mMessage = Message;
		if (context != null) {
			initProgressDialog(Message);
		}
	}

	@Override
	public void onFail(Object result) {
		dimiss();
	}

	@Override
	public void onStart() {
		show();
	}

	public void dimiss() {
		try {
			if (null != mProgressDialog && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			if (progressBar != null) {
				stopAnim(progressBar);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	ImageView progressBar;

	private void show() {
		if (null != mProgressDialog && !mProgressDialog.isShowing()) {
			mProgressDialog.show();
			RelativeLayout contentView = new RelativeLayout(context);
			ViewGroup.LayoutParams rlp = new ViewGroup.LayoutParams(
					DensityUtil.dip2px(context, 330), DensityUtil.dip2px(
							context, 84));
			contentView.setBackgroundColor(Color.WHITE);

			RelativeLayout.LayoutParams pblp = new RelativeLayout.LayoutParams(
					DensityUtil.dip2px(context, 19), DensityUtil.dip2px(
					context, 19));
			progressBar = new ImageView(context);
			progressBar.setId(101);
			pblp.addRule(RelativeLayout.CENTER_VERTICAL);
			pblp.setMargins(DensityUtil.dip2px(context, 24), 0,
					DensityUtil.dip2px(context, 12), 0);
			contentView.addView(progressBar, pblp);
			startAnim(progressBar);

			ImageView iv = new ImageView(context);
			iv.setId(102);
			RelativeLayout.LayoutParams ivlp = new RelativeLayout.LayoutParams(
					DensityUtil.dip2px(context, 27), DensityUtil.dip2px(
					context, 27));
			iv.setImageDrawable(BackGroudSeletor.getdrawble("gy_image_close",
					context));
			ivlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			ivlp.addRule(RelativeLayout.CENTER_VERTICAL);
			ivlp.setMargins(DensityUtil.dip2px(context, 24), 0,
					DensityUtil.dip2px(context, 24), 0);
			contentView.addView(iv, ivlp);
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onCancelClick();
				}
			});

			tv = new TextView(context);
			RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			tvlp.addRule(RelativeLayout.CENTER_VERTICAL);
			tvlp.addRule(RelativeLayout.RIGHT_OF, progressBar.getId());
			tvlp.addRule(RelativeLayout.LEFT_OF, iv.getId());
			tv.setText(mMessage);
			tv.setTextSize(18);
			tv.setTextColor(Color.BLACK);
			contentView.addView(tv, tvlp);

			contentView.setBackgroundDrawable(BackGroudSeletor.get9png(
					"gy_image_editview", context));
			mProgressDialog.setContentView(contentView, rlp);
		}
	}

	TextView tv;

	public void stopAnim(View view) {
		AnimationDrawable anim = (AnimationDrawable) view.getBackground();
		if (anim.isRunning()) { // 如果正在运行,就停止
			anim.stop();
		}
	}

	public void setMessage(String msg) {
		mMessage = msg;
		if (tv != null) {
			tv.setText(mMessage);
		}
	}

	public void startAnim(View view) {
		// 完全编码实现的动画效果
		AnimationDrawable anim = new AnimationDrawable();
		for (int i = 1; i <= 8; i++) {
			// 根据资源名称和目录获取R.java中对应的资源ID
			Drawable drawable = BackGroudSeletor.getdrawble("0" + i, context);
			// 将此帧添加到AnimationDrawable中
			anim.addFrame(drawable, 100);
		}
		anim.setOneShot(false); // 设置为loop
		view.setBackgroundDrawable(anim); // 将动画设置为ImageView背景
		anim.start(); // 开始动画
	}

	private void initProgressDialog(String msg) {
		if (null == this.mProgressDialog) {
			ProgressDialog progressDialog = new ProgressDialog(context);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setCancelable(false);
			this.mProgressDialog = progressDialog;
		}
	}

	@Override
	public void onSuccess(Object result) {
		dimiss();
	}

	@Override
	public void onLoading(Object result) {

	}

	@Override
	public void onCancel(Object result) {
		dimiss();
	}
	@Override
	public void onTimeout(Object result) {
		dimiss();
	}

	@Override
	public void onCancelClick() {
		dimiss();
	}

}
