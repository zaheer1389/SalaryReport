package com.ahmadinfotech.salaryreport.adaptor;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.dao.Balance;
import com.ahmadinfotech.salaryreport.dao.Transaction;
import com.ahmadinfotech.salaryreport.utils.AppUtils;

import java.util.List;

/**
 * Created by root on 6/7/18.
 */

public class MonthViewAdapter extends RecyclerView.Adapter<MonthViewAdapter.TransactionViewHolder> {

    Balance balance;
    List<Transaction> transactions;

    public MonthViewAdapter(Balance balance, List<Transaction> transactions){
        this.balance = balance;
        this.transactions = transactions;
        Log.d("MonthViewAdapter", "Transactions : "+transactions.size());
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder2, int position) {
        Transaction transaction = transactions.get(position);
        holder2.mDate.setText(transaction.getDate());
        holder2.mCredit.setText("" + transaction.getCraditAmount());
        holder2.mDebit.setText("" + transaction.getDebitAmount());
        holder2.mBalance.setText(transaction.getBalance());
        holder2.mNarration.setText(transaction.getNarration());
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder{

        public TextView mBalance;
        public TextView mCredit;
        public TextView mDate;
        public TextView mDebit;
        public View mLayout;
        public TextView mNarration;
        public Transaction transaction;

        public TransactionViewHolder(View itemView){
            super(itemView);

            mDate = (TextView) itemView.findViewById(R.id.txt_date);
            Log.d("mDate", mDate+"");
            mCredit = (TextView) itemView.findViewById(R.id.text_credit);
            mDebit = (TextView) itemView.findViewById(R.id.text_debit);
            mBalance = (TextView) itemView.findViewById(R.id.text_balance);
            mNarration = (TextView) itemView.findViewById(R.id.text_narration);
            mLayout = itemView.findViewById(R.id.layout_row);
            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //VerticalItemHolder.this.transaction.setAccName(AccountDetailAdapter.this.mAccount.getName());
                    //((OnTransactionListListener) AccountDetailAdapter.this.mContext).onItemClicked(VerticalItemHolder.this.transaction);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    //((OnTransactionListListener) AccountDetailAdapter.this.mContext).onItemLongClicked(VerticalItemHolder.this.transaction);
                    return true;
                }
            });
        }

    }
}
