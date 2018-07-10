package com.ahmadinfotech.salaryreport.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.db.FetchData;
import com.ahmadinfotech.salaryreport.export.ReportGenerator;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.ahmadinfotech.salaryreport.R.color.month;
import static com.ahmadinfotech.salaryreport.R.id.txtFromDate;
import static com.ahmadinfotech.salaryreport.R.id.txtToDate;

/**
 * Created by root on 10/7/18.
 */

public class DatewiseReport extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_datewise_report,container,false);

        final EditText txtFromDate = (EditText) view.findViewById(R.id.txtFromDate);
        final EditText txtToDate = (EditText) view.findViewById(R.id.txtToDate);
        Button btnShowReport = (Button) view.findViewById(R.id.btnShowReport);

        btnShowReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtFromDate.getText().length() == 0){
                    Toast.makeText(getContext(),"Please select from date", Toast.LENGTH_LONG);
                }
                else if(txtToDate.getText().length() == 0){
                    Toast.makeText(getContext(),"Please select to date", Toast.LENGTH_LONG);
                }
                else{
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try{
                        String fromDate = txtFromDate.getText().toString();
                        String toDate = txtToDate.getText().toString();
                        double openingBal = new FetchData().getOpeningBalance(txtFromDate.getText().toString());
                        Log.d("DatewiseReport :: ","Opening Balance :: "+openingBal);
                        export(openingBal, fromDate, toDate);
                    }
                    catch(Exception e){
                        Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_LONG);
                    }
                }
            }
        });

        return  view;
    }

    public void export(double openingBal, String fromDate, String toDate){
        Toast.makeText(getContext(),"Opening Balance = "+openingBal, Toast.LENGTH_LONG);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try{
            new ReportGenerator(getContext(), fromDate, toDate).pdf((Activity) getContext());
        }
        catch(Exception e){
            Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    public int monthsBetweenDates(Date startDate, Date endDate){

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);

        Calendar end = Calendar.getInstance();
        end.setTime(endDate);

        int monthsBetween = 0;
        int dateDiff = end.get(Calendar.DAY_OF_MONTH)-start.get(Calendar.DAY_OF_MONTH);

        if(dateDiff<0) {
            int borrrow = end.getActualMaximum(Calendar.DAY_OF_MONTH);
            dateDiff = (end.get(Calendar.DAY_OF_MONTH)+borrrow)-start.get(Calendar.DAY_OF_MONTH);
            monthsBetween--;

            if(dateDiff>0) {
                monthsBetween++;
            }
        }
        else {
            monthsBetween++;
        }
        monthsBetween += end.get(Calendar.MONTH)-start.get(Calendar.MONTH);
        monthsBetween  += (end.get(Calendar.YEAR)-start.get(Calendar.YEAR))*12;
        return monthsBetween;
    }
}
