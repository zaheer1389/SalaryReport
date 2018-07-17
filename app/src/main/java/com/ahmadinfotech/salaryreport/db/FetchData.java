package com.ahmadinfotech.salaryreport.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.app.SalaryReportApp;
import com.ahmadinfotech.salaryreport.dao.Balance;
import com.ahmadinfotech.salaryreport.dao.Transaction;
import com.ahmadinfotech.salaryreport.utils.AppConstants;
import com.ahmadinfotech.salaryreport.utils.AppUtils;
import com.ahmadinfotech.salaryreport.utils.SessionManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static android.R.attr.data;
import static android.R.attr.format;
import static java.lang.Double.parseDouble;

public class FetchData {
  private static String TAG = "FetchData";
  private DecimalFormat df = new DecimalFormat("#.##");
  private DataBaseHandler handler = SalaryReportApp.getDBHandler();
  private NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
  private String pattern = ((DecimalFormat) this.nf).toPattern();
  private String newPattern = this.pattern.replace("¤", "").trim();
  private NumberFormat newFormat = new DecimalFormat(this.newPattern);

  public FetchData() {
    this.df.setMinimumFractionDigits(2);
    this.df.setMaximumFractionDigits(2);
  }

  public void clearTableData(String tableName){
    this.handler.getReadableDatabase().execSQL("DELETE FROM "+tableName);
  }

  public boolean countTotalAccount() {
    Cursor c = this.handler.getReadableDatabase().rawQuery("SELECT AID FROM Account", null);
    if (c.moveToFirst()) {
      int id = c.getCount();
      c.close();
      if (id > 20) {
        return false;
      }
      return true;
    }
    c.close();
    return true;
  }


  public void insertTransactionDetail(Transaction transaction) {
    SimpleDateFormat format1 = new SimpleDateFormat(AppConstants.DateFormat.DB_DATE);
    SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy");
    try {
      SQLiteDatabase db = this.handler.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put("AID", Integer.valueOf(transaction.getAccountId()));
      values.put("UserID", Integer.valueOf(transaction.getUserId()));
      values.put("Credit_Amount", Double.valueOf(transaction.getCraditAmount()));
      values.put("Debit_Amount", Double.valueOf(transaction.getDebitAmount()));
      values.put("dr_cr", Integer.valueOf(transaction.getDr_cr()));
      values.put("Remark", transaction.getRemark());
      values.put("Narration", transaction.getNarration());
      values.put("EntryDate", transaction.getDate());
      values.put("TransactionType", transaction.getTransactionType());
      values.put("TransactionDate", transaction.getDate());
      try {
        Date date = format1.parse(transaction.getDate());
        values.put("Date", format2.format(date));
        values.put("LongDate", Long.valueOf(date.getTime()));
      } catch (Exception e) {
      }
      values.put("Image", transaction.getImage());
      db.insert("Transection", null, values);
    } catch (Exception e2) {
      e2.printStackTrace();
    }
  }

  public void insertCategory(String category) {
    try {
      SQLiteDatabase db = this.handler.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put("TypeName", category);
      db.insert("AccountType", null, values);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void updateAutoBackupDate(long mDate) {
    try {
      SQLiteDatabase db = this.handler.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put("BackupDate", Long.valueOf(mDate));
      db.update("Login", values, "UserID = 1", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean isDBExists() {
    try {
      Cursor c = this.handler.getReadableDatabase().rawQuery("SELECT count(UserID), (SELECT count(AID) FROM Account), (SELECT count(TID) FROM Transection) FROM Login", null);
      if (c.moveToFirst()) {
        boolean s;
        if (c.getInt(0) > 0 || c.getInt(1) > 0 || c.getInt(2) > 0) {
          s = true;
        } else {
          s = false;
        }
        c.close();
        return s;
      }
      c.close();
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean isUserExists() {
    try {
      Cursor c = this.handler.getReadableDatabase().rawQuery("SELECT count(Name) FROM Login", null);
      if (c.moveToFirst()) {
        boolean s;
        if (c.getInt(0) > 0) {
          s = true;
        } else {
          s = false;
        }
        c.close();
        return s;
      }
      c.close();
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public ArrayList<Long> countAccountByBal() {
    ArrayList<Long> results = new ArrayList();
    long crAccounts = 0;
    long drAccounts = 0;
    long balAccounts = 0;
    try {
      Cursor c = this.handler.getReadableDatabase().rawQuery("SELECT AID, (select (Sum(Transection.Credit_Amount) - Sum(Transection.Debit_Amount)) FROM Transection where Transection.AID = Account.AID) AS bal FROM Account", null);
      while (c.moveToNext()) {
        double bal;
        try {
          bal = parseDouble(c.getString(c.getColumnIndex("bal")));
        } catch (Exception e) {
          bal = 0.0d;
        }
        if (bal > 0.0d) {
          crAccounts++;
        } else if (0.0d > bal) {
          drAccounts++;
        } else {
          balAccounts++;
        }
      }
      c.close();
    } catch (SQLiteException e2) {
      e2.printStackTrace();
      Log.e(getClass().getSimpleName(), "Could not create or Open the database");
    }
    results.add(Long.valueOf(crAccounts));
    results.add(Long.valueOf(drAccounts));
    results.add(Long.valueOf(balAccounts));
    results.add(Long.valueOf((crAccounts + drAccounts) + balAccounts));
    return results;
  }

  public ArrayList<Transaction> getTransactions(Context context, String fromDate, String toDate, boolean checkOrder){
    ArrayList<Transaction> transactions = new ArrayList();
    SQLiteDatabase db = this.handler.getReadableDatabase();
    String query = "select * from Transection where EntryDate between Date('" + fromDate + "') and Date('" + toDate + "') order by EntryDate asc, dr_cr desc";
    Log.d(getClass().getSimpleName(), query);
    Cursor c = db.rawQuery(query, null);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(AppConstants.DateFormat.DB_DATE);
    SimpleDateFormat sdf1 = AppUtils.getDateFormat();
    double totalCredit = 0.0d;
    double totalDebit = 0.0d;
    NumberFormat format = AppUtils.getCurrencyFormatter();
    String mRsSymbol = SessionManager.getCurrency(context);

    while (c.moveToNext()) {
      Log.d(getClass().getSimpleName(), "Transaction");
      Transaction transaction = new Transaction();
      transaction.setDr_cr(c.getInt(c.getColumnIndex("dr_cr")));
      String date = c.getString(c.getColumnIndex("EntryDate"));
      try {
        transaction.setDate(sdf1.format(simpleDateFormat.parse(date)));
      } catch (Exception e) {
        transaction.setDate(date);
      }
      transaction.setNarration(c.getString(c.getColumnIndex("Narration")));
      transaction.setRemark(c.getString(c.getColumnIndex("Remark")));
      try {
        transaction.setCraditAmount(parseDouble(c.getString(c.getColumnIndex("Credit_Amount"))));
        try {
          transaction.setDebitAmount(parseDouble(c.getString(c.getColumnIndex("Debit_Amount"))));
        } catch (Exception e2) {
          transaction.setDebitAmount(0.0d);
        }
        transaction.setAId(c.getInt(c.getColumnIndex("AID")));
        transaction.setTransactionId(c.getInt(c.getColumnIndex("TID")));
        transaction.setImage(c.getBlob(c.getColumnIndex("Image")));
        if (transaction.getDr_cr() == 1) {
          totalCredit += transaction.getCraditAmount();
        } else {
          totalDebit += transaction.getDebitAmount();
        }
        if (totalCredit > totalDebit) {
          transaction.setBalance(mRsSymbol + format.format(totalCredit + totalDebit));
        } else if (totalDebit > totalCredit) {
          transaction.setBalance(mRsSymbol + format.format(totalDebit + totalCredit));
        } else {
          transaction.setBalance(mRsSymbol + "0.00/-");
        }
        transactions.add(transaction);
      } catch (Exception e3) {
        Log.e(getClass().getSimpleName(), "Could not create or Open the database");
      }
    }
    c.close();
    if (checkOrder && !SessionManager.getListOrder()) {
      Collections.reverse(transactions);
    }

    return transactions;
  }


  /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
  public ArrayList<Transaction> getAllTransactions(Context context, String query, int aId, boolean checkOrder) {
    Cursor c;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(AppConstants.DateFormat.DB_DATE);
    SimpleDateFormat sdf1 = AppUtils.getDateFormat();
    ArrayList<Transaction> transactions = new ArrayList();
    double totalCredit = 0.0d;
    double totalDebit = 0.0d;
    NumberFormat format = AppUtils.getCurrencyFormatter();
    String mRsSymbol = SessionManager.getCurrency(context);
    SQLiteDatabase db = this.handler.getReadableDatabase();
    if (query != null) {
      c = db.rawQuery(query, new String[]{String.valueOf(aId)});
    } else if (aId == 0) {
      c = db.rawQuery("SELECT * FROM Transection order by AID, EntryDate", null);
    } else {
      c = db.rawQuery("SELECT * FROM Transection where AID = ? order by EntryDate", new String[]{String.valueOf(aId)});
    }
    while (c.moveToNext()) {
      Log.d(getClass().getSimpleName(), "Transaction");
      Transaction transaction = new Transaction();
      transaction.setDr_cr(c.getInt(c.getColumnIndex("dr_cr")));
      String date = c.getString(c.getColumnIndex("EntryDate"));
      try {
        transaction.setDate(sdf1.format(simpleDateFormat.parse(date)));
      } catch (Exception e) {
        transaction.setDate(date);
      }
      transaction.setNarration(c.getString(c.getColumnIndex("Narration")));
      transaction.setRemark(c.getString(c.getColumnIndex("Remark")));
      try {
        transaction.setCraditAmount(parseDouble(c.getString(c.getColumnIndex("Credit_Amount"))));
        try {
          transaction.setDebitAmount(parseDouble(c.getString(c.getColumnIndex("Debit_Amount"))));
        } catch (Exception e2) {
          transaction.setDebitAmount(0.0d);
        }
        transaction.setAId(c.getInt(c.getColumnIndex("AID")));
        transaction.setTransactionId(c.getInt(c.getColumnIndex("TID")));
        transaction.setImage(c.getBlob(c.getColumnIndex("Image")));
        if (transaction.getDr_cr() == 1) {
          totalCredit += transaction.getCraditAmount();
        } else {
          totalDebit += transaction.getDebitAmount();
        }
        if (totalCredit > totalDebit) {
          transaction.setBalance(mRsSymbol + format.format(totalCredit - totalDebit) + "/-Cr");
        } else if (totalDebit > totalCredit) {
          transaction.setBalance(mRsSymbol + format.format(totalDebit - totalCredit) + "/-Db");
        } else {
          transaction.setBalance(mRsSymbol + "0.00/-");
        }
        transactions.add(transaction);
      } catch (Exception e3) {
        Log.e(getClass().getSimpleName(), "Could not create or Open the database");
      }
    }
    c.close();
    if (checkOrder && !SessionManager.getListOrder()) {
      Collections.reverse(transactions);
    }
    Balance balance = getBalanceFormatted(context, totalCredit, totalDebit);
    ArrayList list = new ArrayList();
    list.add(transactions);
    list.add(balance);
    return list;
  }

  public boolean checkSignInRequest(String uname, String pass) {
    try {
      String str = "Login";
      Cursor cursor = this.handler.getReadableDatabase().query(str, new String[]{"UserID"}, "username=?  COLLATE NOCASE AND password=?  COLLATE NOCASE", new String[]{uname, pass}, null, null, null);
      int count = cursor.getCount();
      cursor.close();
      if (count > 0) {
        return true;
      }
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public int changepassword(int id, String pass) {
    try {
      SQLiteDatabase db = this.handler.getReadableDatabase();
      ContentValues updateCountry = new ContentValues();
      updateCountry.put("Password", pass);
      int updateStatus = db.update("Login", updateCountry, "UserID=" + String.valueOf(id), null);
      Log.d("cursor", String.valueOf(updateStatus));
      if (updateStatus != 0) {
        return 1;
      }
    } catch (Exception e) {
    }
    return 0;
  }

  public void settleAccount(int id) {
    this.handler.getWritableDatabase().delete("Transection", "AID=" + id, null);
  }

  public ArrayList<Double> getTransactionTotal(int aId) {
    ArrayList<Double> amount = new ArrayList();
    double s1 = 0.0d;
    double s2 = 0.0d;
    SQLiteDatabase db = this.handler.getReadableDatabase();
    try {
      Cursor cursor;
      db.execSQL("DELETE FROM Transection WHERE AID NOT IN (SELECT AID FROM Account)");
      if (aId == 0) {
        cursor = db.rawQuery(" SELECT  sum(Transection.Debit_Amount) AS SumOfDRAmount, sum(Transection.Credit_Amount) AS SumOfCRAmount FROM Transection", null);
      } else {
        cursor = db.rawQuery(" SELECT  sum(Transection.Debit_Amount) AS SumOfDRAmount, sum(Transection.Credit_Amount) AS SumOfCRAmount FROM Transection WHERE (((Transection.AID)=?))", new String[]{String.valueOf(aId)});
      }
      Log.d(TAG, "getTransectionTotal cursor =" + cursor);
      if (cursor.moveToFirst()) {
        try {
          s1 = parseDouble(cursor.getString(1));
        } catch (Exception e) {
        }
        try {
          s2 = parseDouble(cursor.getString(0));
        } catch (Exception e2) {
        }
      }
      cursor.close();
    } catch (Exception e3) {
      e3.printStackTrace();
    }
    amount.add(Double.valueOf(s1));
    amount.add(Double.valueOf(s2));
    return amount;
  }

  public ArrayList<ArrayList<Transaction>> getAccountGlance(boolean isSortByName) {
    String query = "SELECT Account.AID, Account.PersonName, (Sum(Transection.Credit_Amount) - Sum(Transection.Debit_Amount)) AS balance\nFROM Account INNER JOIN [Transection] ON Account.AID = Transection.AID\nGROUP BY Account.AID ORDER BY Account.PersonName COLLATE NOCASE";
    ArrayList<Transaction> listCr = new ArrayList();
    ArrayList<Transaction> listDr = new ArrayList();
    double totalCr = 0.0d;
    double totalDr = 0.0d;
    try {
      Cursor c = this.handler.getReadableDatabase().rawQuery(query, null);
      if (c.moveToFirst()) {
        do {
          Transaction account = new Transaction();
          //String pname = c.getString(c.getColumnIndex(ACCOUNT.NAME));
          String strBalance = c.getString(c.getColumnIndex("balance"));
          //account.setAccName(pname);
          double bal = parseDouble(strBalance);
          if (bal > 0.0d) {
            account.setCraditAmount(bal);
            totalCr += bal;
            listCr.add(account);
          } else if (0.0d > bal) {
            bal *= -1.0d;
            totalDr += bal;
            account.setDebitAmount(bal);
            listDr.add(account);
          }
        } while (c.moveToNext());
      }
      c.close();
    } catch (SQLiteException e) {
      Log.e(getClass().getSimpleName(), "Could not create or Open the database");
    }
    ArrayList<Transaction> total = new ArrayList();
    Transaction dao = new Transaction();
    dao.setCraditAmount(totalCr);
    dao.setDebitAmount(totalDr);
    total.add(dao);
    if (!isSortByName) {
      Collections.sort(listCr);
      Collections.sort(listDr);
    }
    ArrayList<ArrayList<Transaction>> result = new ArrayList();
    result.add(listCr);
    result.add(listDr);
    result.add(total);
    return result;
  }

  public Transaction getTodayTransactions(String fromDate, String toDate) {
    String query = "SELECT sum(Transection.Debit_Amount) AS SumOfDRAmount, sum(Transection.Credit_Amount) AS SumOfCRAmount, Transection.EntryDate FROM Transection\nWHERE (AID != 0) AND Transection.EntryDate BETWEEN Date('" + fromDate + "') AND Date('" + toDate + "') GROUP BY Transection.EntryDate";
    Transaction data = new Transaction();
    try {
      Cursor cursor = this.handler.getReadableDatabase().rawQuery(query, null);
      Log.d("getDayTransaction " + cursor.getCount(), "" + query);
      if (cursor.moveToFirst()) {
        int clmDate = cursor.getColumnIndex("EntryDate");
        int damt = cursor.getColumnIndex("SumOfDRAmount");
        int camt = cursor.getColumnIndex("SumOfCRAmount");
        String debit = cursor.getString(damt);
        try {
          data.setCraditAmount(parseDouble(cursor.getString(camt)));
        } catch (Exception e) {
          data.setCraditAmount(0.0d);
        }
        try {
          data.setDebitAmount(parseDouble(debit));
        } catch (Exception e2) {
          data.setDebitAmount(0.0d);
        }
        if (data.getCraditAmount() > data.getDebitAmount()) {
          data.setBalance((data.getCraditAmount() - data.getDebitAmount()) + "/-Cr");
        } else if (data.getDebitAmount() > data.getCraditAmount()) {
          data.setBalance((data.getDebitAmount() - data.getCraditAmount()) + "/-Dr");
        } else {
          data.setBalance("0.00");
        }
        data.setDate(cursor.getString(clmDate));
        cursor.close();
      }
    } catch (Exception e3) {
      e3.printStackTrace();
      Log.e(getClass().getSimpleName(), "Could not create or Open the database");
    }
    return data;
  }

  public double getOpeningBalance(String toDate) {
    String query = "SELECT (sum(Transection.Credit_Amount)+sum(Transection.Debit_Amount)) as bal FROM Transection\nWHERE Transection.EntryDate < Date('" + toDate + "')";
    double openingBal = 0;
    try {
      Cursor cursor = this.handler.getReadableDatabase().rawQuery(query, null);
      Log.d("getDayTransaction " + cursor.getCount(), "" + query);
      if (cursor.moveToFirst()) {
        int clmDate = cursor.getColumnIndex("bal");
        String debit = cursor.getString(clmDate);
        try {
          openingBal = Double.parseDouble(cursor.getString(clmDate));
        } catch (Exception e) {
          openingBal = 0.0d;
        }
        cursor.close();
      }
    } catch (Exception e3) {
      e3.printStackTrace();
      Log.e(getClass().getSimpleName(), "Could not create or Open the database");
    }
    return openingBal;
  }

  public String getTotalBalance(int aId) {
    String balance = "0.00";
    Cursor cur = this.handler.getReadableDatabase().rawQuery("SELECT  AID, sum(Transection.Debit_Amount) AS SumOfDRAmount, sum(Transection.Credit_Amount) AS SumOfCRAmount FROM Transection  WHERE (((Transection.AID)=?)) ", new String[]{String.valueOf(aId)});
    if (cur != null) {
      cur.moveToFirst();
      int sno = cur.getColumnIndex("AID");
      int damt = cur.getColumnIndex("SumOfDRAmount");
      int camt = cur.getColumnIndex("SumOfCRAmount");
      if (cur.moveToFirst()) {
        do {
          int snos = cur.getInt(sno);
          Log.d("getTotalBalance = ", "id = " + snos);
          if (snos != 0) {
            double dr;
            double cr;
            String dates = cur.getString(damt);
            String camts = cur.getString(camt);
            try {
              dr = parseDouble(dates);
            } catch (Exception e) {
              e.printStackTrace();
              dr = 0.0d;
            }
            try {
              cr = parseDouble(camts);
            } catch (Exception e2) {
              e2.printStackTrace();
              cr = 0.0d;
            }
            if (cr > dr) {
              try {
                balance = "" + this.newFormat.format(Double.valueOf(cr - dr).doubleValue()) + "/-Cr";
              } catch (Exception e22) {
                e22.printStackTrace();
                System.out.println("Data not insert");
              }
            } else if (dr > cr) {
              balance = "" + this.newFormat.format(Double.valueOf(dr - cr).doubleValue()) + "/-Db";
            } else {
              balance = "0.00";
            }
          }
        } while (cur.moveToNext());
      }
      cur.close();
    }
    return balance;
  }

  private Balance getBalanceFormatted(Context context, double cr, double dr) {
    Balance dao = new Balance();
    NumberFormat format = AppUtils.getCurrencyFormatter();
    String mRsSymbol = SessionManager.getCurrency(context);
    if (cr > dr) {
      dao.setBalance(mRsSymbol + format.format(cr - dr) + "/-" + context.getResources().getString(R.string.txt_Credit));
    } else if (dr > cr) {
      dao.setBalance(mRsSymbol + format.format(dr - cr) + "/-" + context.getResources().getString(R.string.txt_Debit));
    } else {
      dao.setBalance(mRsSymbol + "0.00/-");
    }
    dao.setCredit(mRsSymbol + format.format(cr) + "/-");
    dao.setDebit(mRsSymbol + format.format(dr) + "/-");
    return dao;
  }

  public Balance getAccountBalance(int aId, Context context, String tillDate) {
    Balance dao = new Balance();
    try {
      String select;
      SQLiteDatabase db = this.handler.getReadableDatabase();
      if (tillDate != null) {
        select = "SELECT  AID, sum(Debit_Amount) AS SumOfDRAmount, sum(Credit_Amount) AS SumOfCRAmount FROM Transection  WHERE AID=? AND EntryDate < '" + tillDate + "'";
      } else {
        select = "SELECT  AID, sum(Debit_Amount) AS SumOfDRAmount, sum(Credit_Amount) AS SumOfCRAmount FROM Transection  WHERE AID=? ";
      }
      Cursor cur = db.rawQuery(select, new String[]{String.valueOf(aId)});
      int damt = cur.getColumnIndex("SumOfDRAmount");
      int camt = cur.getColumnIndex("SumOfCRAmount");
      if (cur.moveToFirst()) {
        double dr;
        double cr;
        String dates = cur.getString(damt);
        String camts = cur.getString(camt);
        try {
          dr = parseDouble(dates);
        } catch (Exception e) {
          e.printStackTrace();
          dr = 0.0d;
        }
        try {
          cr = parseDouble(camts);
        } catch (Exception e2) {
          e2.printStackTrace();
          cr = 0.0d;
        }
        if (context != null) {
          dao = getBalanceFormatted(context, cr, dr);
        } else {
          dao.setCredit("" + cr);
          dao.setDebit("" + dr);
        }
      }
      cur.close();
    } catch (Exception e22) {
      e22.printStackTrace();
      System.out.println("Data not insert");
    }
    return dao;
  }

  public ArrayList<Transaction> getCategoryBalance(String category) {
    String query;
    if (category == null) {
      query = "SELECT Account.AID, Account.PersonName, Account.PersonEmail, Account.PersonMobile, Sum(Transection.Credit_Amount) AS SumOfCRAmount, Sum(Transection.Debit_Amount) AS SumOfDRAmount, Account.TypeName\nFROM Account INNER JOIN [Transection] ON Account.AID = Transection.AID\nGROUP BY Account.AID, Account.TypeName\nORDER BY PersonName COLLATE NOCASE;";
    } else {
      query = "SELECT Account.AID, Account.PersonName, Account.PersonEmail, Account.PersonMobile, Sum(Transection.Credit_Amount) AS SumOfCRAmount, Sum(Transection.Debit_Amount) AS SumOfDRAmount, Account.TypeName\nFROM Account INNER JOIN [Transection] ON Account.AID = Transection.AID\nGROUP BY Account.AID, Account.TypeName\nHAVING (((Account.TypeName)=\"" + category + "\")) ORDER BY PersonName COLLATE NOCASE;";
    }
    ArrayList<Transaction> results = new ArrayList();
    try {
      Cursor c = this.handler.getReadableDatabase().rawQuery(query, null);
      if (c.moveToFirst()) {
        do {
          Transaction account = new Transaction();
          //String pname = c.getString(c.getColumnIndex(ACCOUNT.NAME));
          int aId = c.getInt(c.getColumnIndex("AID"));
          String credit = c.getString(c.getColumnIndex("SumOfCRAmount"));
          String debit = c.getString(c.getColumnIndex("SumOfDRAmount"));
          String mobile = c.getString(c.getColumnIndex("PersonMobile"));
          String email = c.getString(c.getColumnIndex("PersonEmail"));
          try {
            String type = c.getString(c.getColumnIndex("TypeName"));
            if (type == null || type.trim().equalsIgnoreCase("")) {
              type = "Individual";
            }
            account.setType(type);
          } catch (Exception e) {
          }
          //account.setAccName(pname);
          account.setRemark(mobile);
          account.setNarration(email);
          account.setAId(aId);
          account.setCraditAmount(parseDouble(credit));
          account.setDebitAmount(parseDouble(debit));
          results.add(account);
        } while (c.moveToNext());
      }
      c.close();
    } catch (SQLiteException e2) {
      Log.e(getClass().getSimpleName(), "Could not create or Open the database");
    }
    return results;
  }

  public int updateTransaction(Transaction transaction) {
    SimpleDateFormat format1 = new SimpleDateFormat(AppConstants.DateFormat.DB_DATE);
    SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy");
    int updateStatus = 0;
    try {
      SQLiteDatabase db = this.handler.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put("Credit_Amount", Double.valueOf(transaction.getCraditAmount()));
      values.put("Debit_Amount", Double.valueOf(transaction.getDebitAmount()));
      values.put("dr_cr", Integer.valueOf(transaction.getDr_cr()));
      values.put("Remark", transaction.getRemark());
      values.put("Narration", transaction.getNarration());
      values.put("EntryDate", transaction.getDate());
      try {
        Date date = format1.parse(transaction.getDate());
        values.put("Date", format2.format(date));
        values.put("LongDate", Long.valueOf(date.getTime()));
      } catch (Exception e) {
      }
      values.put("AID", Integer.valueOf(transaction.getAccountId()));
      values.put("Image", transaction.getImage());
      updateStatus = db.update("Transection", values, "TID=" + transaction.getTransactionId(), null);
    } catch (Exception e2) {
      e2.printStackTrace();
    }
    return updateStatus;
  }
}
