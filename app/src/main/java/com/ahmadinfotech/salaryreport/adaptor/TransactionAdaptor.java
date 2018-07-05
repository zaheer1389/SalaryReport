package com.ahmadinfotech.salaryreport.adaptor;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ahmadinfotech.salaryreport.dao.Transaction;

/**
 * Created by root on 30/6/18.
 */

public class TransactionAdaptor extends RecyclerView.Adapter {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder{

        public TransactionViewHolder(View view){
            super(view);
        }
    }
}
