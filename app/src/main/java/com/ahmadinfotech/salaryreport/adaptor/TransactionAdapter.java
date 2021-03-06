package com.ahmadinfotech.salaryreport.adaptor;

import android.accounts.Account;
import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.dao.Balance;
import com.ahmadinfotech.salaryreport.dao.Transaction;
import com.ahmadinfotech.salaryreport.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends Adapter<ViewHolder> {
  private static final int FIRST_ROW = 2;
  private static final int HEADER_VIEW = 1;
  private boolean isSearching;
  private boolean isTabullar = true;
  private Balance mBalance;
  private Context mContext;
  public List<Transaction> mTransactions;
  public List<Transaction> selected_usersList = new ArrayList();

  public interface OnTransactionListListener {
    void onItemClicked(Transaction transaction);

    void onItemLongClicked(Transaction transaction);
  }

  public class NormalViewHolder extends ViewHolder {
    private View mLayout;
    public Transaction transaction;
    public final TextView tvCity;
    public final TextView tvDate;
    public final TextView tvEmail;
    public final TextView tvMobile;
    public final TextView tvName;
    public final TextView tvProduct;
    public final TextView tvRemark;

    public NormalViewHolder(View view) {
      super(view);
      this.mLayout = view.findViewById(R.id.ll_top);
      this.tvName = (TextView) view.findViewById(R.id.txt_sname);
      this.tvEmail = (TextView) view.findViewById(R.id.txt_email);
      this.tvCity = (TextView) view.findViewById(R.id.txt_city);
      this.tvMobile = (TextView) view.findViewById(R.id.txt_phone_no);
      this.tvRemark = (TextView) view.findViewById(R.id.txt_remark);
      this.tvDate = (TextView) view.findViewById(R.id.txt_date);
      this.tvProduct = (TextView) view.findViewById(R.id.txt_product);
      this.itemView.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          //NormalViewHolder.this.transaction.setAccName(AccountDetailAdapter.this.mAccount.getName());
          //((OnTransactionListListener) AccountDetailAdapter.this.mContext).onItemClicked(NormalViewHolder.this.transaction);
        }
      });
      this.itemView.setOnLongClickListener(new OnLongClickListener() {
        public boolean onLongClick(View view) {
         // ((OnTransactionListListener) AccountDetailAdapter.this.mContext).onItemLongClicked(NormalViewHolder.this.transaction);
          return true;
        }
      });
    }
  }

  public class VerticalItemHolder extends ViewHolder {
    private TextView mBalance;
    private TextView mCredit;
    private TextView mDate;
    private TextView mDebit;
    private View mLayout;
    private TextView mNarration;
    public Transaction transaction;

    public VerticalItemHolder(View itemView) {
      super(itemView);
      this.mDate = (TextView) itemView.findViewById(R.id.text_date);
      this.mCredit = (TextView) itemView.findViewById(R.id.text_credit);
      this.mDebit = (TextView) itemView.findViewById(R.id.text_debit);
      this.mBalance = (TextView) itemView.findViewById(R.id.text_balance);
      this.mNarration = (TextView) itemView.findViewById(R.id.text_narration);
      this.mLayout = itemView.findViewById(R.id.layout_row);
      itemView.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          //VerticalItemHolder.this.transaction.setAccName(AccountDetailAdapter.this.mAccount.getName());
          //((OnTransactionListListener) AccountDetailAdapter.this.mContext).onItemClicked(VerticalItemHolder.this.transaction);
        }
      });
      itemView.setOnLongClickListener(new OnLongClickListener() {
        public boolean onLongClick(View view) {
          //((OnTransactionListListener) AccountDetailAdapter.this.mContext).onItemLongClicked(VerticalItemHolder.this.transaction);
          return true;
        }
      });
    }
  }

  public TransactionAdapter(Context context, Account account, Balance balance, List<Transaction> transactions, boolean isTabullar) {
    this.mBalance = balance;
    this.mTransactions = transactions;
    this.mContext = context;
    this.isTabullar = isTabullar;
  }

  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (this.isTabullar) {
      return new VerticalItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_match_item_, parent, false));
    }
    return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_transaction, parent, false));
  }

  public void onBindViewHolder(ViewHolder vh, int position) {
    try {

        Transaction transaction;
        if (this.isSearching) {
          transaction = (Transaction) this.mTransactions.get(position);
        } else {
          transaction = (Transaction) this.mTransactions.get(position - 1);
        }
        if (this.isTabullar) {
          VerticalItemHolder holder2 = (VerticalItemHolder) vh;
          holder2.transaction = transaction;
          holder2.mDate.setText(transaction.getDate());
          holder2.mCredit.setText("" + transaction.getCraditAmount());
          holder2.mDebit.setText("" + transaction.getDebitAmount());
          holder2.mBalance.setText(transaction.getBalance());
          holder2.mNarration.setText(transaction.getNarration());
          if (this.selected_usersList.contains(transaction)) {
            holder2.mLayout.setBackgroundColor(AppUtils.getColor(this.mContext, R.color.green_google));
            return;
          } else if (transaction.getDr_cr() == 1) {
            holder2.mLayout.setBackgroundColor(AppUtils.getColor(this.mContext, R.color.red));
            return;
          } else {
            holder2.mLayout.setBackgroundColor(AppUtils.getColor(this.mContext, R.color.dark_blue));
            return;
          }
        }
        NormalViewHolder holder3 = (NormalViewHolder) vh;
        holder3.transaction = transaction;
        if (transaction.getDr_cr() == 1) {
          holder3.tvName.setText("Credit");
          holder3.tvName.setTextColor(AppUtils.getColor(this.mContext, R.color.red_dark));
          holder3.tvEmail.setText("" + transaction.getCraditAmount());
        } else {
          holder3.tvName.setText("Debit");
          holder3.tvEmail.setText("" + transaction.getDebitAmount());
          holder3.tvName.setTextColor(AppUtils.getColor(this.mContext, R.color.s_name_color));
        }
        holder3.tvCity.setText(transaction.getBalance());
        holder3.tvDate.setText(" :" + transaction.getDate());
        holder3.tvProduct.setText(" :" + transaction.getNarration());
        holder3.tvRemark.setText(" :" + transaction.getRemark());
        if (this.selected_usersList.contains(transaction)) {
          AppUtils.setDrawable(this.mContext, holder3.mLayout, R.drawable.row_radius_selected);
        } else {
          AppUtils.setDrawable(this.mContext, holder3.mLayout, R.drawable.row_radius);
        }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getItemCount() {
    if (this.mTransactions == null) {
      return 0;
    }
    if (this.isSearching) {
      return this.mTransactions.size();
    }
    if (this.mTransactions.size() == 0) {
      return 1;
    }
    return this.mTransactions.size() + 1;
  }

  public int getItemViewType(int position) {
    if (this.isSearching || position != 0) {
      return super.getItemViewType(position);
    }
    if (this.isTabullar) {
      return 2;
    }
    return 1;
  }

  public void setFilter(List<Transaction> countryModels, boolean isDone) {
    this.isSearching = isDone;
    this.mTransactions = countryModels;
    notifyDataSetChanged();
  }
}
