package com.ahmadinfotech.salaryreport.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.adaptor.MonthViewAdapter;
import com.ahmadinfotech.salaryreport.adaptor.TransactionAdapter;
import com.ahmadinfotech.salaryreport.dao.Balance;
import com.ahmadinfotech.salaryreport.db.FetchData;

import java.util.ArrayList;

/**
 * Created by root on 6/7/18.
 */

public class MonthViewFragment extends Fragment{

    Button btnMonthYear;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_import_data, container, false);

        FetchData fetchData = new FetchData();
        ArrayList list = fetchData.getAllTransactions(getContext(), null, 0, true);
        Log.d(getClass().getSimpleName(), "Total trnsactions : "+((ArrayList) list.get(0)).size());

        MonthViewAdapter adapter = new MonthViewAdapter((Balance) list.get(1), (ArrayList)list.get(0));

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.listData);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        getActivity().setTitle("Transactions");

        adapter.notifyDataSetChanged();

        return view;
    }
}
