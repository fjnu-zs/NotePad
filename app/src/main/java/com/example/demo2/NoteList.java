package com.example.demo2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
public class NoteList extends AppCompatActivity
{
    private Map<String,Object> reItem = new HashMap<>();
    private int rePosition = -1;
    private int resultCode = -1;
    private List<Map<String,Object>> mList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MyRecyclerView myRecyclerAdapter;
    private boolean isShow = false;
    private EditText editText;
    private int deletePosition = -1;
    private FloatingActionButton fab;
    private AlertDialog alertDialog;
    public void myView(View view){
        ConstraintLayout tableLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.activity_delete_tip,null);
        alertDialog=new AlertDialog.Builder(this).setView(tableLayout).create();
        alertDialog.show();
    }
    public void delete(View view){
        alertDialog.dismiss();
        if(deletePosition >= 0){
            int deleteID = myRecyclerAdapter.getId(deletePosition);
            int count = this.getContentResolver().delete(NotePad.Notes.CONTENT_URI,"_id="+deleteID,null);
            if (count>0){
                myRecyclerAdapter.notifyDataSetChanged();
                mList.clear();
                getData();
            }
        }
    }
    public void getData(){
        Cursor cursor = this.getContentResolver().query(NotePad.CONTENT_URI,null,null,null);
        get(cursor);
    }
    public void get(Cursor cursor){
        Log.e("test ", "count=" + cursor.getCount());
        mList.clear();
        while(cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(NotePad.Notes._ID));
            String title = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE));
            String note = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE));
            long modify = cursor.getLong(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
            Map<String,Object> map = new HashMap<>();
            map.put(NotePad.Notes._ID,id);
            map.put(NotePad.Notes.COLUMN_NAME_TITLE,title);
            map.put(NotePad.Notes.COLUMN_NAME_NOTE,note);
            map.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,modify);
            map.put("high",note.length()*7+100);
            mList.add(map);
        }
        cursor.close();

    }
    public Cursor select(String selectArg){
        Cursor cursor = this.getContentResolver().query(NotePad.Notes.CONTENT_URI, null,
                NotePad.Notes.COLUMN_NAME_TITLE+" LIKE \"%"+selectArg+"%\"",null,null);
        return cursor;
    }
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if(intent.getData()==null){
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recycleView);
        //创建线性布局管理器
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        getData();
        myRecyclerAdapter = new MyRecyclerView(this,mList);
        myRecyclerAdapter.setOnItemClickListener(new MyRecyclerView.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                rePosition = position;
                Uri uri = ContentUris.withAppendedId(getIntent().getData(),myRecyclerAdapter.getId(position));
                startActivityForResult(new Intent(Intent.ACTION_EDIT,uri),1);
            }

            @Override
            public void onLongClick(View view, int position) {
                deletePosition = position;
                myView(view);
            }
        });
        recyclerView.setAdapter(myRecyclerAdapter);
        // 定义事件监听器
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.fab:
                        Uri uri = getIntent().getData();
                        startActivityForResult(new Intent(Intent.ACTION_INSERT,uri),2);
                }
            }
        };
        // 为悬浮按钮绑定事件处理监听器
        fab.setOnClickListener(listener);

        editText = findViewById(R.id.select);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!(editText.getText()+"").equals("")){
                    String selectArg = editText.getText()+"";
                    Cursor cursor = select(selectArg);
                    myRecyclerAdapter.notifyDataSetChanged();
                    get(cursor);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if((editText.getText()+"").equals("")){
                    getData();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 || requestCode == 2){
            if(data == null){
                return;
            }
            this.resultCode = resultCode;
            reItem.clear();
            reItem.put(NotePad.Notes._ID,data.getStringExtra(NotePad.Notes._ID));
            reItem.put(NotePad.Notes.COLUMN_NAME_TITLE,data.getStringExtra(NotePad.Notes.COLUMN_NAME_TITLE));
            reItem.put(NotePad.Notes.COLUMN_NAME_NOTE,data.getStringExtra(NotePad.Notes.COLUMN_NAME_NOTE));
            reItem.put("high",data.getStringExtra(NotePad.Notes.COLUMN_NAME_NOTE).length()*7+100);
            reItem.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,Long.parseLong(data.getStringExtra(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myRecyclerAdapter.notifyDataSetChanged();
        mList.clear();
        getData();
    }
}