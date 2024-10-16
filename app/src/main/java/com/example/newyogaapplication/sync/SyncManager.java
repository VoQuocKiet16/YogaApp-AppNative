package com.example.newyogaapplication.sync;

import android.content.Context;
import android.os.Handler;

public class SyncManager {

    private SyncWorker syncWorker;
    private Handler handler;
    private final long interval = 5000; //

    public SyncManager(Context context) {
        syncWorker = new SyncWorker(context);
        handler = new Handler();
    }


    public void startSyncing() {
        handler.postDelayed(syncRunnable, interval);
    }

    public void stopSyncing() {
        handler.removeCallbacks(syncRunnable);
    }

    private Runnable syncRunnable = new Runnable() {
        @Override
        public void run() {
            syncWorker.syncFirebaseWithSQLite();
            syncWorker.syncSQLiteWithFirebase();

            handler.postDelayed(this, interval);
        }
    };
}
