package com.example.alice.ftvargame_verifytickets_app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;

/**
 * Created by alice on 2018/9/19.
 */

public class NetworkStatus {

    public static final int TYPE_LOGIN = 0;
    public static final int TYPE_NO_INTERNET = 8;
    public static final int TYPE_OTHER = 9;

    private static NetworkStatus instance;

    private NetworkStatus() {}

    public static NetworkStatus getInstance() {
        if (null == instance) {
            instance = new NetworkStatus();
        }
        return instance;
    }

    public void status(final Activity activity, final int type, final NetworkStatusCallback networkCallback) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null){
            switch (type){
                case TYPE_LOGIN:
                    showLoginDialog(activity,type,networkCallback);
                    break;
                case TYPE_NO_INTERNET:
                    showNoInternetDialog(activity);
                    break;
                case TYPE_OTHER:
                    showOtherDialog(activity);
                    break;
            }
        }else {
            networkCallback.networkOk();
        }
    }

    private void showLoginDialog(final Activity activity, final int type, final NetworkStatusCallback networkCallback){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("無法連線").setMessage("請重新檢查網路連線");
        builder.setCancelable(false);
        builder.setPositiveButton("重試", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                status(activity,type,networkCallback);
            }
        });
        builder.show();
    }

    private void showNoInternetDialog(final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("無法連線").setMessage("請檢查網路後在重試");
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showOtherDialog(final Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("無法連線").setMessage("請檢查網路後在重試");
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                activity.finish();
            }
        });
        builder.setPositiveButton("回上頁", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.show();
    }

    public interface NetworkStatusCallback {
        void networkOk();
    }

}
