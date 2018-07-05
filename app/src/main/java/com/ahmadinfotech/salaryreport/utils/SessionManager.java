package com.ahmadinfotech.salaryreport.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ahmadinfotech.salaryreport.app.SalaryReportApp;
import com.ahmadinfotech.salaryreport.preferences.ActivityPreferences;

/**
 * Created by root on 30/6/18.
 */

public class SessionManager {

    public static final String FIRST_TIME = "isAppInstalled";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String KEY_BACKUP = "backup";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_PRO = "KEY_IS_PRO";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_NAME = "Hii";
    private static final String KEY_PASS = "pass";
    private static final String KEY_USER_NAME = "username";
    public static final String LAST_SEARCH = "LAST_SEARCH";
    private static final String PREF_ALARM_DATE = "PREF_ALARM_DATE";
    public static final String PREF_BACKUP_MAIL = "PREF_BACKUP_MAIL";
    public static final String PREF_DROPBOX_TOKEN = "DB_ACCESS_TOKEN";
    private static final String PREF_IS_REFRESH_ACCOUNT_LIST = "PREF_IS_REFRESH_ACCOUNT_LIST";
    public static final String PREF_STORED_DB_NAME = "PREF_STORED_DB_NAME";
    private static final String PREF_USER_INTERACTION_COUNT = "PREF_USER_INTERACTION_COUNT";
    public static final String REGISTER_ON_SERVER = "count";
    public static final String REMEMBER_ME = "rememberMe";
    private static final String U_ID = "u_id";

    public static String getCurrency(Context context) {
        int id = Integer.parseInt(SalaryReportApp.getPreference().getString(ActivityPreferences.PREF_CURRENCY, "0"));
        if (id == 0) {
            return "";
        }
        return context.getString(AppConstants.ARR_CURRENCY[id - 1]);
    }

    public static boolean getListOrder() {
        if (SalaryReportApp.getPreference().getString(ActivityPreferences.PREF_LIST_ORDER, "asc").equalsIgnoreCase("asc")) {
            return true;
        }
        return false;
    }

    public static int getInteractionCount() {
        return SalaryReportApp.getPreference().getInt(PREF_USER_INTERACTION_COUNT, 0);
    }

    public static void setInteractionCount(int count) {
        SharedPreferences.Editor editor = SalaryReportApp.getPreference().edit();
        editor.putInt(PREF_USER_INTERACTION_COUNT, count);
        editor.apply();
    }

    public static void incrementInteractionCount() {
        SharedPreferences.Editor editor = SalaryReportApp.getPreference().edit();
        editor.putInt(PREF_USER_INTERACTION_COUNT, getInteractionCount() + 1);
        editor.apply();
    }
}
