package com.example.demo2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyRecyclerView extends RecyclerView.Adapter<MyRecyclerView.MyViewHolder> {
    private List<Map<String,Object>> mList;
    private LayoutInflater mInflater;

    public void insert(Map<String, Object> reItem) {
        mList.add(0,reItem);
        Log.e("test","title:"+mList.get(0).get(NotePad.Notes.COLUMN_NAME_TITLE));
        notifyItemInserted(0);
    }

    public interface OnItemClickListener {
        void onClick(int position);
        void onLongClick(View view,int position);
    }
    public int getId(int position){
        Log.e("test","position:"+position);
        Log.e("test","id:"+mList.get(position).get(NotePad.Notes._ID));
        return Integer.parseInt(mList.get(position).get(NotePad.Notes._ID).toString());
    }
    private OnItemClickListener listener;
    //第二步， 写一个公共的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public MyRecyclerView(Context context, List<Map<String,Object>> mList){
        this.mInflater = LayoutInflater.from(context);
        this.mList = mList;
    }

    @NonNull
    @Override
    public MyRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("test","createView");
        View itemView = mInflater.inflate(R.layout.item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerView.MyViewHolder holder, final int position) {
        Log.e("test","position:"+position);
        Log.e("test","title:"+mList.get(position).get(NotePad.Notes.COLUMN_NAME_TITLE));
        Log.e("test","onBindViewHolder");
        holder.tv1.setText(mList.get(position).get(NotePad.Notes.COLUMN_NAME_TITLE).toString());
        holder.tv2.setText(mList.get(position).get(NotePad.Notes.COLUMN_NAME_NOTE).toString());
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mList.get(position).get(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
        holder.tv3.setText(time);
        ViewGroup.LayoutParams lp = holder.tv2.getLayoutParams();
        lp.height = Integer.parseInt(mList.get(position).get("high").toString());
        holder.tv2.setLayoutParams(lp);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    listener.onClick(position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(listener != null){
                    listener.onLongClick(view,position);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView tv1 = null;
        private TextView tv2 = null;
        private TextView tv3 = null;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(R.id.text1);
            tv2 = itemView.findViewById(R.id.text2);
            tv3 = itemView.findViewById(R.id.text3);
        }
    }
}
