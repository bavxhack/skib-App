package com.h2Invent.skibin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ChildListAdapter extends RecyclerView.Adapter<ChildListAdapter.ChildListViewHolder> {
    private Context mContext;
    private ArrayList<ChildListItem> mChildList;
    private OnItemClickListener mListener;
    private RequestQueue requestQueue;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onCheckinClick(int position);
    }

    public void setmListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public ChildListAdapter(Context context, ArrayList<ChildListItem> childList) {
        mContext = context;
        mChildList = childList;
    }

    @NonNull
    @Override
    public ChildListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.childlistelement, parent, false);
        return new ChildListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildListViewHolder holder, int position) {
        ChildListItem currentItem = mChildList.get(position);
        String name = currentItem.getmName();
        String schule = currentItem.getmSchule();
        int klasse = currentItem.getmKlasse();
        int schulId = currentItem.getmSchulId();
        boolean checkin = currentItem.ismCheckin();
        boolean hasBirthday = currentItem.isMhasBirthday();
        holder.textViewname.setText(name);
        holder.textViewschule.setText(schule);

        if (checkin && schulId > -1) {
            holder.linearLayoutIndicator.setBackgroundColor(ContextCompat.getColor(mContext, R.color.backgroundSuccess));
            holder.mCheckinBtn.setVisibility(View.GONE);
            holder.mCheckinBtn.setEnabled(false);
        } else if (!checkin && schulId > -1)  {
            holder.linearLayoutIndicator.setBackgroundColor(ContextCompat.getColor(mContext, R.color.backgroundError));
        }
        if(schulId == -1){
            holder.mCheckinBtn.setVisibility(View.GONE);
            holder.mCheckinBtn.setEnabled(false);
        }
        if (klasse > 0) {
            holder.textViewklasse.setText(String.valueOf(klasse) + ". Klasse");
        } else {
            holder.textViewklasse.setText("");
        }
        if (hasBirthday) {
            holder.hasBirthday.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mChildList.size();
    }

    public class ChildListViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewname;
        public TextView textViewschule;
        public TextView textViewklasse;
        public LinearLayout linearLayoutIndicator;
        public TextView hasBirthday;
        public MaterialButton mCheckinBtn;

        public ChildListViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewname = (TextView) itemView.findViewById(R.id.childElementName);
            textViewschule = (TextView) itemView.findViewById(R.id.childElementSchule);
            textViewklasse = (TextView) itemView.findViewById(R.id.childElementKlasse);
            linearLayoutIndicator = (LinearLayout) itemView.findViewById(R.id.indicatorCheckin);
            hasBirthday = (TextView) itemView.findViewById(R.id.birthdayShow);
            mCheckinBtn = (MaterialButton) itemView.findViewById(R.id.checkinButton);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
            mCheckinBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onCheckinClick(position);
                        }
                    }
                }
            });


        }
    }
}
