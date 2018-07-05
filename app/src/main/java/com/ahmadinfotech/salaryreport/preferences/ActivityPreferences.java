package com.ahmadinfotech.salaryreport.preferences;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;

/**
 * Created by root on 30/6/18.
 */

public class ActivityPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DB_BACKUP_DAY = "DB_BACKUP_DAY";
    public static final String PREF_CURRENCY = "PREF_CURRENCY";
    public static final String PREF_DATE_FORMAT = "PREF_DATE_FORMAT";
    public static final String PREF_DB_BACKUP = "PREF_DB_BACKUP";
    public static final String PREF_LANGUAGE = "PREF_LANGUAGE";
    public static final String PREF_LIST_ORDER = "PREF_LIST_ORDER";
    public static final String PREF_LIST_TYPE = "PREF_LIST_TYPE";
    public static final String PREF_PASSWORD = "PREF_PASSWORD";
    private static final String PREF_SETTLE = "PREF_SETTLE";

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
