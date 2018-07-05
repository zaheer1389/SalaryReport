package com.ahmadinfotech.salaryreport.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build.VERSION;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.ahmadinfotech.salaryreport.app.SalaryReportApp;
import com.ahmadinfotech.salaryreport.preferences.ActivityPreferences;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AppUtils {
  public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");
  private static final String TAG = "AppUtils";

  public static boolean isEmailValid(String mEmail) {
    return EMAIL_ADDRESS_PATTERN.matcher(mEmail).matches();
  }

  public static SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat(SalaryReportApp.getPreference().getString(ActivityPreferences.PREF_DATE_FORMAT, "dd/MM/yyyy"));
  }

  public static boolean setImage(ImageView img, byte[] byteArray) {
    if (byteArray == null || byteArray.length == 0) {
      return true;
    }
    Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    if (bmp == null) {
      return false;
    }
    img.setImageBitmap(bmp);
    return false;
  }

  public static final boolean isNetworkAvailable(Context context) {
    return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
  }

  public static String capitalWord(String source) {
    if (TextUtils.isEmpty(source)) {
      return "";
    }
    StringBuffer res = new StringBuffer();
    for (String str : source.split(" ")) {
      char[] stringArray = str.trim().toCharArray();
      stringArray[0] = Character.toUpperCase(stringArray[0]);
      res.append(new String(stringArray)).append(" ");
    }
    return res.toString();
  }

  public static NumberFormat getCurrencyFormatter() {
    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    return new DecimalFormat(((DecimalFormat) nf).toPattern().replace("¤", "").trim());
  }

  public static int getColor(Context context, int id) {
    if (VERSION.SDK_INT >= 23) {
      return context.getColor(id);
    }
    return context.getResources().getColor(id);
  }

  public static void setDrawable(Context context, View layout, int id) {
    if (VERSION.SDK_INT < 16) {
      layout.setBackgroundDrawable(context.getResources().getDrawable(id));
    } else if (VERSION.SDK_INT < 22) {
      layout.setBackground(context.getResources().getDrawable(id));
    } else {
      layout.setBackground(ContextCompat.getDrawable(context, id));
    }
  }

  public static String getValidFileName(String fileName) {
    if (TextUtils.isEmpty(fileName)) {
      return "";
    }
    fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "");
    if (fileName.length() > 15) {
      return fileName.substring(0, 15);
    }
    return fileName;
  }

  public static String getUniqueFileName() {
    return new SimpleDateFormat("_yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
  }

}
