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

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.adaptor.AccountDetailAdapter;
import com.ahmadinfotech.salaryreport.adaptor.TransactionAdapter;
import com.ahmadinfotech.salaryreport.adaptor.TransactionAdaptor;
import com.ahmadinfotech.salaryreport.dao.Balance;
import com.ahmadinfotech.salaryreport.dao.Transaction;
import com.ahmadinfotech.salaryreport.db.FetchData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 6/7/18.
 */

public class TransactionViewFragment  extends Fragment {

    TransactionAdapter transactionAdapter;
    private Balance mBalance;
    private List<Transaction> mTransactions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        FetchData fetchData = new FetchData();
        ArrayList list = fetchData.getAllTransactions(getContext(), null, 0, true);
        Log.d(getClass().getSimpleName(), "Total trnsactions : "+((ArrayList) list.get(0)).size());
        this.mBalance = (Balance) list.get(1);
        this.mTransactions = (ArrayList) list.get(0);

        transactionAdapter = new TransactionAdapter(getContext(), null, this.mBalance, this.mTransactions, false);

        RecyclerView rv = new RecyclerView(getContext());//(RecyclerView) container.findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(transactionAdapter);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        getActivity().setTitle("Transactions");

        transactionAdapter.notifyDataSetChanged();

        return  rv;
    }
}
