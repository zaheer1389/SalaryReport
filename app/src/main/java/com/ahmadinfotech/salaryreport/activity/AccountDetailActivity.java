package com.ahmadinfotech.salaryreport.activity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.app.SalaryReportApp;
import com.ahmadinfotech.salaryreport.adaptor.AccountDetailAdapter;
import com.ahmadinfotech.salaryreport.dao.Balance;
import com.ahmadinfotech.salaryreport.dao.PdfDao;
import com.ahmadinfotech.salaryreport.dao.Transaction;
import com.ahmadinfotech.salaryreport.db.FetchData;
import com.ahmadinfotech.salaryreport.export.GeneratePdf;
import com.ahmadinfotech.salaryreport.preferences.ActivityPreferences;
import com.ahmadinfotech.salaryreport.tabullar.FixedGridLayoutManager;
import com.ahmadinfotech.salaryreport.tabullar.InsetDecoration;
import com.ahmadinfotech.salaryreport.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint({"NewApi"})
public class AccountDetailActivity extends MainActivity implements AccountDetailAdapter.OnTransactionListListener, OnQueryTextListener {
  private static int REQUEST_EDIT_ACCOUNT = 5;
  private static final int SETTLE_ACCOUNT = 456;
  private static final int SETTLE_ACCOUNT_BALANCE = 423;
  private boolean isListTablular;
  private AccountDetailAdapter mAdapter;
  private Balance mBalance;
  private RecyclerView mListTransactions;
  private List<Transaction> mTransactions;
  private ViewStub mViewStub;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    /*if (SalaryReportApp.getPreference().getBoolean(ActivityPreferences.PREF_LIST_TYPE, true)) {
      setContentView((int) R.layout.activity_account_ledger__);
      this.mViewStub = (ViewStub) findViewById(R.id.view_stub);
      this.mViewStub.setLayoutResource(R.layout.item_header_account_detail);
      this.mViewStub.inflate();
      this.isListTablular = true;
      //findViewById(R.id.lout_account_detail).setOnClickListener(this);
    } else {
      setContentView((int) R.layout.activity_account_ledger);
    }*/
      setContentView((int) R.layout.activity_account_ledger);
    getViews();
    loadData();
    setListAdapter();
    setAdapter();
  }

  protected void populateSetDate(int id, int mYear2, int i, int mDay2) {
  }


  private void setAdapter() {
    if (this.isListTablular) {
      this.mListTransactions.setLayoutManager(FixedGridLayoutManager.newInstance());
      this.mListTransactions.addItemDecoration(new InsetDecoration(this));
      this.mListTransactions.getItemAnimator().setAddDuration(1000);
      this.mListTransactions.getItemAnimator().setChangeDuration(1000);
      this.mListTransactions.getItemAnimator().setMoveDuration(1000);
      this.mListTransactions.getItemAnimator().setRemoveDuration(1000);
      return;
    }
    this.mListTransactions.setLayoutManager(new GridLayoutManager(this, 1));
    this.mListTransactions.setItemAnimator(new DefaultItemAnimator());
    this.mListTransactions.setHasFixedSize(true);
  }

  private void setListAdapter() {
    this.mAdapter = new AccountDetailAdapter(this, null, this.mBalance, this.mTransactions, this.isListTablular);
    this.mListTransactions.setAdapter(this.mAdapter);
    this.mAdapter.notifyDataSetChanged();
  }

  private void loadData() {
    FetchData fetchData = new FetchData();
    //this.mAccount = fetchData.getAccount(getIntent().getStringExtra(EXTRA.SELECTED_ACCOUNT_NAME));
    ArrayList list = fetchData.getAllTransactions(this, null, 0, true);
      Log.d(getClass().getSimpleName(), "Total trnsactions : "+((ArrayList) list.get(0)).size());
    this.mBalance = (Balance) list.get(1);
    this.mTransactions = (ArrayList) list.get(0);
    setListAdapter();
    if (this.isListTablular) {
      setText();
    }
  }

  private void setText() {
    TextView mTvDebit = (TextView) findViewById(R.id.txt_total_debit);
    TextView mTvBalance = (TextView) findViewById(R.id.txt_total_balance);
    ((TextView) findViewById(R.id.txt_total_credit)).setText(this.mBalance.getCredit());
    mTvDebit.setText(this.mBalance.getDebit());
    mTvBalance.setText(this.mBalance.getBalance());
  }

  private void getViews() {
    this.mListTransactions = (RecyclerView) findViewById(R.id.list);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    String mRsSymbol = SessionManager.getCurrency(getApplicationContext());
    switch (item.getItemId()) {
      case R.id.menu_export_exel:
        export(1);
        break;
      case R.id.menu_export_pdf:
        export(0);
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return false;
  }

  public void onClick(View v) {
    SessionManager.incrementInteractionCount();
    Intent intent;
    switch (v.getId()) {
      case R.id.btn_email:
        sendEmail();
        return;
      case R.id.btn_sms:
        if (isStoragePermissionGranted()) {
          sendSms();
          return;
        } else {
          Toast.makeText(this, R.string.permission_storage, Toast.LENGTH_LONG).show();
          return;
        }
      default:
        //super.onClick(v);
        return;
    }
  }

  private void sendEmail() {
    String email = "zaheerkhorajiya13@gmail.com";
    if (email == null || email.equalsIgnoreCase("")) {
      Toast.makeText(getApplicationContext(), "Email Id not exists of this account.", Toast.LENGTH_SHORT).show();
      return;
    }
    try {
      Intent emailIntent = new Intent("android.intent.action.SEND");
      emailIntent.setFlags(268435456);
      emailIntent.setType("plain/text");
      emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
      emailIntent.putExtra("android.intent.extra.EMAIL", new String[]{email});
      emailIntent.putExtra("android.intent.extra.SUBJECT", "");
      emailIntent.putExtra("android.intent.extra.TEXT", "");
      startActivity(emailIntent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
    }
  }

  private void sendSms() {
    String mobile = "9987854684";
    if (mobile == null || mobile.equalsIgnoreCase("")) {
      Toast.makeText(getApplicationContext(), "Mobile Number not exists of this account.", Toast.LENGTH_SHORT).show();
      return;
    }
    Intent smsVIntent = new Intent("android.intent.action.VIEW");
    smsVIntent.setType("vnd.android-dir/mms-sms");
    smsVIntent.putExtra("address", mobile);
    smsVIntent.putExtra("sms_body", " \nVia:Simple Accounting Android App \ndownload this app \nhttp://bit.ly/1LGUjOE");
    try {
      startActivity(smsVIntent);
    } catch (Exception ex) {
      SmsManager.getDefault().sendTextMessage("PhoneNumber-example:" + mobile, null, " \nVia:Simple Accounting Android App \ndownload this app \nhttp://bit.ly/1LGUjOE", null, null);
      Toast.makeText(this, "Your sms has failed...", Toast.LENGTH_LONG).show();
      ex.printStackTrace();
    }
  }

  public boolean isStoragePermissionGranted() {
    if (ContextCompat.checkSelfPermission(this, "android.permission.SEND_SMS") == 0) {
      return true;
    }
    ActivityCompat.requestPermissions(this, new String[]{"android.permission.SEND_SMS"}, 1);
    return false;
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults[0] == 0) {
      sendSms();
    }
  }

  protected void setImage(Bitmap thumbnail) {
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_account_ledger, menu);
    menu.removeItem(R.id.menu_date);
    MenuItem menuItem = menu.findItem(R.id.menu_search);
    SearchView searchView = (SearchView) menuItem.getActionView();
    searchView.setSearchableInfo(((SearchManager) getSystemService(SEARCH_SERVICE)).getSearchableInfo(getComponentName()));
    searchView.setOnQueryTextListener(this);
    menuItem.setOnActionExpandListener(new OnActionExpandListener() {
      public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
      }

      public boolean onMenuItemActionCollapse(MenuItem item) {
        AccountDetailActivity.this.searchComplete();
        return true;
      }
    });
    return true;
  }

  public boolean onQueryTextChange(String newText) {
    if (this.mViewStub != null) {
      this.mViewStub.setVisibility(View.GONE);
    }
    this.mAdapter.setFilter(filter(this.mTransactions, newText), true);
    return false;
  }

  private List<Transaction> filter(List<Transaction> models, String newText) {
    newText = newText.toLowerCase();
    List<Transaction> filteredModelList = new ArrayList();
    for (Transaction model : models) {
      if (model.toString().toLowerCase().contains(newText)) {
        filteredModelList.add(model);
      }
    }
    return filteredModelList;
  }

  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  private void searchComplete() {
    this.mAdapter.setFilter(this.mTransactions, false);
    if (this.mViewStub != null) {
      this.mViewStub.setVisibility(View.VISIBLE);
    }
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 78 && resultCode == -1) {
      loadData();
    } else if (requestCode != REQUEST_EDIT_ACCOUNT) {
    } else {
      if (resultCode == -1) {
        //this.mAccount = (Account) data.getSerializableExtra(AppConstants.ACCOUNT_SELECTED);
        if (this.isListTablular) {
          //setAccountDetail();
          return;
        }
        this.mAdapter = new AccountDetailAdapter(this, null, this.mBalance, this.mTransactions, this.isListTablular);
        this.mListTransactions.setAdapter(this.mAdapter);
        this.mAdapter.notifyDataSetChanged();
        setResult(REQUEST_EDIT_ACCOUNT);
      } else if (resultCode == 1) {
        finish();
      }
    }
  }

  private void export(int index) {
    ArrayList<String> mColumns = new ArrayList();
    mColumns.add(getResources().getString(R.string.txt_Date));
    mColumns.add(getResources().getString(R.string.txt_Credit));
    mColumns.add(getResources().getString(R.string.txt_Debit));
    mColumns.add(getResources().getString(R.string.txt_balance_amount));
    mColumns.add(getResources().getString(R.string.txt_Narration));
    PdfDao header = new PdfDao();
    header.setFirst("Salary Report");
    header.setSecond(this.mBalance.getCredit());
    header.setThird(this.mBalance.getDebit());
    header.setFour(this.mBalance.getBalance());
    header.setFive("9987854684");
    header.setSix("zaheerkhorajiya13@gmail.com");
    List<Transaction> trans = this.mTransactions;
    ArrayList<PdfDao> mValues = new ArrayList();
    if (!SessionManager.getListOrder()) {
      Collections.reverse(trans);
    }
    for (Transaction dao : trans) {
      PdfDao pdf = new PdfDao();
      pdf.setFirst(dao.getDate());
      pdf.setSecond("" + dao.getCraditAmount());
      pdf.setThird("" + dao.getDebitAmount());
      pdf.setFour(dao.getBalance());
      pdf.setFive(dao.getNarration());
      mValues.add(pdf);
    }
    switch (index) {
      case 0:
        new GeneratePdf(getApplicationContext(), header, mColumns, mValues, 1).pdf(this);
        return;
      case 1:
        //new GenerateExcel(getApplicationContext(), header, mColumns, mValues, 1).excel(this);
        return;
      default:
        return;
    }
  }

  public void refreshAdapterData() {
    this.mAdapter.mTransactions = this.mTransactions;
    this.mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onItemLongClicked(Transaction transaction) {

  }

  @Override
  public void onItemClicked(Transaction transaction) {

  }
}
