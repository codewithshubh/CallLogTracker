package com.codewithshubh.calllogtracker;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.MyViewHolder> {
    private int px;
    Context context;
    ArrayList<CallLogModel> callLogModelArrayList;

    public CallLogAdapter(Context context, ArrayList<CallLogModel> callLogModelArrayList) {
        this.context = context;
        this.callLogModelArrayList = callLogModelArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Resources r = parent.getResources();
        px = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8,r.getDisplayMetrics()));
        View v = LayoutInflater.from(context).inflate(R.layout.layout_call_log, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int i = position;
        if(i == 0){
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            layoutParams.topMargin = px;
            holder.cardView.requestLayout();
        }

        CallLogModel currentLog = callLogModelArrayList.get(position);
        holder.tv_ph_num.setText(currentLog.getPhNumber());
        holder.tv_contact_name.setText(currentLog.getContactName());
        holder.tv_call_type.setText(currentLog.getCallType());
        holder.tv_call_date.setText(currentLog.getCallDate());
        holder.tv_call_time.setText(currentLog.getCallTime());
        holder.tv_call_duration.setText(currentLog.getCallDuration());
    }

    @Override
    public int getItemCount() {
        return callLogModelArrayList==null ? 0 : callLogModelArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView tv_ph_num, tv_contact_name, tv_call_type, tv_call_date, tv_call_time, tv_call_duration;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_ph_num = itemView.findViewById(R.id.layout_call_log_ph_no);
            tv_contact_name = itemView.findViewById(R.id.layout_call_log_contact_name);
            tv_call_type = itemView.findViewById(R.id.layout_call_log_type);
            tv_call_date = itemView.findViewById(R.id.layout_call_log_date);
            tv_call_time = itemView.findViewById(R.id.layout_call_log_time);
            tv_call_duration = itemView.findViewById(R.id.layout_call_log_duration);
            cardView = itemView.findViewById(R.id.layout_call_log_cardview);
        }
    }
}
