package com.example.newyogaapplication.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkAvailable(context)) {
            // Hiển thị Toast khi có mạng
            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
        } else {
            // Hiển thị Toast khi mất mạng
            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm kiểm tra trạng thái kết nối mạng
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
