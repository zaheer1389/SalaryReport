package com.ahmadinfotech.salaryreport.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.ahmadinfotech.salaryreport.db.DataBaseHandler;


/**
 * Created by root on 30/6/18.
 */

public class SalaryReportApp extends Application {

    private static SalaryReportApp instance = null;
    private static boolean isActivityVisible = false;
    private static Handler mHandler = null;
    private static final int mInterval = 99000;
    private static SharedPreferences mPreferences;
    private static DataBaseHandler sDBHadler;
    Runnable mStatusChecker = new Runnable() {
        public void run() {
            try {
                SalaryReportApp.mHandler.sendEmptyMessage(0);
            } finally {
                SalaryReportApp.mHandler.postDelayed(SalaryReportApp.this.mStatusChecker, 99000);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SalaryReportApp", "App created");
        initialize();
    }

    public static SalaryReportApp getInstance() {
        return instance;
    }

    private void initialize() {
        instance = this;
        sDBHadler = new DataBaseHandler(getApplicationContext());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public static DataBaseHandler getDBHandler() {
        return sDBHadler;
    }

    public static SharedPreferences getPreference() {
        return mPreferences;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void onTerminate() {
        super.onTerminate();
        stopRepeatingTask();
        try {
            if (this.mStatusChecker != null) {
                this.mStatusChecker = null;
            }
            if (sDBHadler != null) {
                sDBHadler.close();
            }
            sDBHadler = null;
        } catch (Exception e) {
        }
    }

    private void startRepeatingTask() {
        this.mStatusChecker.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(this.mStatusChecker);
    }


}

