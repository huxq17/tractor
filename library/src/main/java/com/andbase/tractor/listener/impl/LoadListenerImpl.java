package com.andbase.tractor.listener.impl;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.andbase.tractor.LittleDialog;
import com.andbase.tractor.listener.LoadListener;

public class LoadListenerImpl implements LoadListener {
    private Context context;
    private ProgressDialog mProgressDialog;
    private String mMessage;

    public LoadListenerImpl() {
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        mProgressDialog = progressDialog;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void show() {
        if (null != mProgressDialog && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }


    public void setMessage(String msg) {
        mMessage = msg;
        if (null != mProgressDialog) {
                mProgressDialog.setMessage(mMessage);
        }
    }

    private void initProgressDialog(String msg) {
        if (null == mProgressDialog) {
            mProgressDialog = new LittleDialog(context, msg, new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancelClick();
                }
            });
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
    }

}
