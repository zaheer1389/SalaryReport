package com.ahmadinfotech.salaryreport.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.dao.Balance;
import com.ahmadinfotech.salaryreport.dao.PdfDao;
import com.ahmadinfotech.salaryreport.dao.Transaction;
import com.ahmadinfotech.salaryreport.db.FetchData;
import com.ahmadinfotech.salaryreport.export.GeneratePdf;
import com.ahmadinfotech.salaryreport.utils.AppConstants;
import com.ahmadinfotech.salaryreport.utils.SessionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import static android.R.attr.country;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_view_data){
            Intent intent = new Intent(getApplicationContext(), AccountDetailActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_import_csv_data) {
            // Handle the camera action
            importCSVData();
        }
        else if (id == R.id.nav_ledger_report) {
            export(0);
        }
        else if (id == R.id.nav_datewise_report) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void importCSVData(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/comma-separated-values");
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            readFile(new File(data.getData().getPath()));
        }
    }

    public void readFile(File csvFile){
        new FetchData().clearTableData("Transection");

        try {
            // Delete everything above here since we're reading from the File we already have
            ContentValues cv = new ContentValues();
            // reading CSV and writing table
            CSVReader dataRead = new CSVReader(new FileReader(csvFile)); // <--- This line is key, and why it was reading the wrong file

            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";
            try {
                br = new BufferedReader(new FileReader(csvFile));
                int index = 0;
                while ((line = br.readLine()) != null) {
                    Transaction transaction = new Transaction();
                    // use comma as separator
                    String[] cols = line.split(cvsSplitBy);
                    Log.d("TAGGGGGGGGGGGGGG", Arrays.toString(cols));
                    if(index < 3){
                        index++;
                        continue;

                    }
                    int colIndex = 0;
                    for(String s : cols){
                        //System.out.print(s+",");
                        s = s.replace("\"", "");
                        if(colIndex == 0){

                            double amount = Double.parseDouble(s);
                            if(amount >= 0){
                                transaction.setTransactionType("CREDIT");
                                transaction.setCraditAmount(amount);
                                transaction.setDr_cr(1);
                            }
                            else{
                                transaction.setTransactionType("DEBIT");
                                transaction.setDebitAmount(amount);
                                transaction.setDr_cr(0);
                            }
                        }
                        if(colIndex == 3){
                            SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.DateFormat.DB_DATE);
                            Date dt = new SimpleDateFormat("dd/MM/yyyy").parse(s);
                            transaction.setDate(sdf.format(dt));
                            //transaction.setTransactionDate(new Timestamp(dt.getTime()));
                        }
                        if(colIndex == 4){
                            transaction.setNarration(s);
                        }

                        colIndex++;
                    }
                    index++;
                    //System.out.println();
                    new FetchData().insertTransactionDetail(transaction);

                }

                Toast.makeText(getApplicationContext(), "Data imported successfully", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e("TAG",e.toString());
                    }
                }
            }

        } catch (Exception e) {
            Log.e("TAG",e.toString());

        }
    }

    private void export(int index) {
        FetchData fetchData = new FetchData();
        ArrayList list = fetchData.getAllTransactions(this, null, 0, true);
        Balance mBalance = (Balance) list.get(1);
        ArrayList<Transaction> mTransactions = (ArrayList) list.get(0);

        ArrayList<String> mColumns = new ArrayList();
        mColumns.add(getResources().getString(R.string.txt_Date));
        mColumns.add(getResources().getString(R.string.txt_Credit));
        mColumns.add(getResources().getString(R.string.txt_Debit));
        mColumns.add(getResources().getString(R.string.txt_balance_amount));
        mColumns.add(getResources().getString(R.string.txt_Narration));
        PdfDao header = new PdfDao();
        header.setFirst("Salary Report");
        header.setSecond(mBalance.getCredit());
        header.setThird(mBalance.getDebit());
        header.setFour(mBalance.getBalance());
        header.setFive("9987854684");
        header.setSix("zaheerkhorajiya13@gmail.com");
        List<Transaction> trans = mTransactions;
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
}
