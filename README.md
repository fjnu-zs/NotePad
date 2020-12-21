## 实现主要功能的逻辑
### 1.数据库的操作
自己自定义一个NotePadrovider去实现ContentProvider
``` java
public class NotePadProvider extends ContentProvider
```
在UriMatcher中添加回传输到NotePadProvider的Uri格式
``` java
matcher = new UriMatcher(UriMatcher.NO_MATCH);
matcher.addURI(NotePad.AUTHORITY,"notes",WORDS);
matcher.addURI(NotePad.AUTHORITY,"notes/#",WORD);
```
在provider中使用SQLiteOpenHleper的oncreate方法实现创建数据库
``` java
public boolean onCreate() {
        myDatabaseHelper = new MyDatabaseHelper(getContext());
        return true;
    }
```
并通过qurey、update、insert、delete实现对数据库的增删改查
然后在AndroidManifest.xml中配置provider
``` java
<provider
            android:name=".NotePadProvider"
            android:authorities="com.google.provider.NotePad"></provider>
```
在activity中通过this.getContextProvider().query、insert、update、delete等方法对数据库进行访问
例：
``` java
Cursor cursor = this.getContentResolver().query(NotePad.Notes.CONTENT_URI, null,
                NotePad.Notes.COLUMN_NAME_TITLE+" LIKE \"%"+selectArg+"%\"",null,null);
```
### 2.activity跳转
本实验主要通过隐式的跳转方式实现界面跳转
首先对跳转到的activity需要在AndroidManifest.xml中注册intent-filter
``` java
<intent-filter>
                <action android:name="android.intent.action.EDIT" />

                <category android:name="android.intent.category.DEFAULT" />

                <data android:mimeType="vnd.android.cursor.dir/vnd.google.note" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.INSERT" />

                <category android:name="android.intent.category.DEFAULT" />

                <data android:mimeType="vnd.android.cursor.item/vnd.google.note" />
            </intent-filter>
```
在本次实验我主要通过有返回值的跳转来进行跳转
在跳转前的界面启动带返回值的跳转
``` java
startActivityForResult(new Intent(Intent.ACTION_INSERT,uri),2);
```
在跳转到的界面运行结束时使用setResult设置返回的标志量
``` java
setResult(result, intent);
```
再回到跳转前的界面时回先运行onActivityResult来获得返回的intent和标志量
再对不同的标志量执行对应的操作
## 基本功能
### 1.显示时间
首先先获得时间戳
![](https://github.com/fjnu-zs/NotePad/blob/master/images/getdate1.jpg)
如图这样设置便可读出一行中全部的属性，若不想全部获得便将此处填入你需要获得的数据项的String[]集合
``` java
public void getData(){
        Cursor cursor = this.getContentResolver().query(NotePad.CONTENT_URI,null,null,null);
        get(cursor);
    }
    public void get(Cursor cursor){
        mList.clear();
        while(cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(NotePad.Notes._ID));
            String title = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE));
            String note = cursor.getString(cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE));
            //此处为获取时间戳
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
```
然后将时间戳格式化并加载进TextView中
``` java
String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mList.get(position).get(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
        holder.tv3.setText(time);
```
此处因为我实现recycleView的原因，所以在自定义recycleView的适配器直接对其进行的格式化
若是使用老师的demo可以使用SimpleCursorAdapter.setViewBinder()来对数据进行格式化或修饰
#### 显示时间演示
![](https://github.com/fjnu-zs/NotePad/blob/master/images/getdate2.jpg)
### 2.搜索功能
首先现在xml中添加SearchView或者EditView来进行搜索输入
``` java
<EditText
        android:layout_margin="3dp"
        android:drawableLeft="@drawable/select"
        android:drawablePadding="10dp"
        android:paddingLeft="10dp"
        android:id="@+id/select"
        android:singleLine="true"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:background="@drawable/select_style"
        app:layout_constraintTop_toBottomOf="@+id/head"
        android:hint="搜索"
        android:textSize="30dp"
        android:cursorVisible="true"
        android:textCursorDrawable="@null"/>
```
然后设置一个该EditView中数据变化的监听器
``` java
editText = findViewById(R.id.select);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!(editText.getText()+"").equals("")){
                    //只要editView中的数据变化就会对数据进行模糊查询
                    String selectArg = editText.getText()+"";
                    Cursor cursor = select(selectArg);
                    //刷新recycleView
                    myRecyclerAdapter.notifyDataSetChanged();
                    get(cursor);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //editView的数据为空的话显示全部数据
                if((editText.getText()+"").equals("")){
                    getData();
                }
            }
        });
```
#### 搜索演示
输入框为空时显示全部数据
![](https://github.com/fjnu-zs/NotePad/blob/master/images/select1.jpg)
输入框有数据后模糊匹配输入框中数据的内容
![](https://github.com/fjnu-zs/NotePad/blob/master/images/select2.jpg)

## 扩展功能
### 1.美化UI
首先可以在style.xml中设置activirty的状态栏和标题栏
``` java
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
```
然后在AndroidManifest.xml的activity的android:theme修改改为@style/AppTheme.NoActionBar，这样状态栏和标题栏就会被隐藏
#### xml项目结构
![](https://github.com/fjnu-zs/NotePad/blob/master/images/xml.png)
且因为是我自己重写整个项目整个项目也会UI也是重中之重,下面展示一下主要的两个界面NoteList和Editor
#### NoteList.xml
``` java
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#E3E2DC"
    tools:context=".NoteList"
    android:padding="10dp">
    <!-- 嵌套一个约束布局来管理两个悬浮按钮 -->
    <TextView
        android:paddingTop="30dp"
        android:id="@+id/head"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="50dp"
        android:text="便签"
        android:layout_marginLeft="3dp"
        tools:ignore="MissingConstraints" />
    <EditText
        android:layout_margin="3dp"
        android:drawableLeft="@drawable/select"
        android:drawablePadding="10dp"
        android:paddingLeft="10dp"
        android:id="@+id/select"
        android:singleLine="true"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:background="@drawable/select_style"
        app:layout_constraintTop_toBottomOf="@+id/head"
        android:hint="搜索"
        android:textSize="30dp"
        android:cursorVisible="true"
        android:textCursorDrawable="@null"/>
    <androidx.recyclerview.widget.RecyclerView
        android:layout_marginTop="5dp"
        app:layout_constraintTop_toBottomOf="@id/select"
        android:id="@+id/recycleView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        tools:ignore="MissingConstraints" />
    <!-- 使用约束布局将该按钮定位到界面右下角 -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab"
        android:layout_width="70dp"
        android:layout_height="70dp"
        android:layout_marginEnd="30dp"
        android:layout_marginBottom="30dp"
        android:clickable="true"
        android:src="@drawable/add"
        app:backgroundTint="#FFC107"
        app:elevation="5dp"
        app:fabCustomSize="70dp"
        app:fabSize="normal"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:maxImageSize="30dp"
        app:rippleColor="#e7d161" />
</androidx.constraintlayout.widget.ConstraintLayout>
```
![](https://github.com/fjnu-zs/NotePad/blob/master/images/getdate2.jpg)
#### Editor.xml
``` java
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".Editor"
    android:padding="10dp"
    android:background="#fff"
    android:orientation="vertical">
    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_marginTop="10dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/button">
        <Button
            android:layout_marginLeft="5dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/button2"
            android:drawableLeft="@drawable/no"
            android:background="@null"
            app:layout_constraintLeft_toLeftOf="parent"
            tools:ignore="MissingConstraints" />
        <Button
            android:layout_marginEnd="20dp"
            android:layout_width="50dp"
            android:layout_height="wrap_content"
            android:id="@+id/button1"
            android:background="@null"
            app:layout_constraintEnd_toEndOf="parent"
            android:drawableRight="@drawable/yes"
            android:layout_marginRight="0dp"
            android:onClick="onClick"

            tools:ignore="MissingConstraints" />
    </androidx.constraintlayout.widget.ConstraintLayout>
    <EditText
        android:id="@+id/editorTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="标题"
        android:textSize="35dp"
        android:background="@null"
        android:textCursorDrawable="@drawable/cursor_color"
        app:layout_constraintTop_toBottomOf="@+id/button1"
        tools:ignore="MissingConstraints" />

    <EditText
        android:id="@+id/editorNote"
        android:layout_marginTop="10dp"
        android:gravity="top"
        android:textCursorDrawable="@drawable/cursor_color"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:singleLine="false"
        android:hint="文本"
        android:background="@null"
        android:textSize="25dp"
        app:layout_constraintTop_toBottomOf="@+id/editorTitle"
        />
</LinearLayout>
```
![](https://github.com/fjnu-zs/NotePad/blob/master/images/editor.jpg)

### 2.实现瀑布流
自定义一个MyRecycleView去继承RecycleView.Adater实现对获得的数据库数据的装填
``` java
public class MyRecyclerView extends RecyclerView.Adapter<MyRecyclerView.MyViewHolder> {
    private List<Map<String,Object>> mList;//需要装填的数据
    private LayoutInflater mInflater;

    public void insert(Map<String, Object> reItem) {
        mList.add(0,reItem);
        Log.e("test","title:"+mList.get(0).get(NotePad.Notes.COLUMN_NAME_TITLE));
        notifyItemInserted(0);
    }
    //创建一个点击事件的接口
    public interface OnItemClickListener {
        void onClick(int position);
        void onLongClick(View view,int position);
    }
    //通过点击获得position获得item对应的_ID
    public int getId(int position){
        Log.e("test","position:"+position);
        Log.e("test","id:"+mList.get(position).get(NotePad.Notes._ID));
        return Integer.parseInt(mList.get(position).get(NotePad.Notes._ID).toString());
    }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
  
    public MyRecyclerView(Context context, List<Map<String,Object>> mList){
        this.mInflater = LayoutInflater.from(context);
        this.mList = mList;
    }

    //创建一个view
    public MyRecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("test","createView");
        View itemView = mInflater.inflate(R.layout.item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }
    //对每个数据进行装填
    @Override
    public void onBindViewHolder(@NonNull MyRecyclerView.MyViewHolder holder, final int position) {
        Log.e("test","position:"+position);
        Log.e("test","title:"+mList.get(position).get(NotePad.Notes.COLUMN_NAME_TITLE));
        Log.e("test","onBindViewHolder");
        holder.tv1.setText(mList.get(position).get(NotePad.Notes.COLUMN_NAME_TITLE).toString());
        holder.tv2.setText(mList.get(position).get(NotePad.Notes.COLUMN_NAME_NOTE).toString());
        //过去note的创建时间戳并对其格式化后装入TextView
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mList.get(position).get(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
        holder.tv3.setText(time);
        ViewGroup.LayoutParams lp = holder.tv2.getLayoutParams();
        lp.height = Integer.parseInt(mList.get(position).get("high").toString());
        holder.tv2.setLayoutParams(lp);
        //对item创建点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    listener.onClick(position);
                }
            }
        });
        //对item创建长按事件
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
```
然后通过适配器对recycleView进行数据装填
后对RecycleView设置线性布局管理器
在此我设置了一行显示两条数据，并因为在onBindViewHolder中设置了每个view的高度，这样就可以实现两列高度跟字数相关的瀑布流式显示了
``` java
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
```

### 3.悬浮按钮
在xml中添加一个FloatingActionButton
``` java
<com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab"
        android:layout_width="70dp"
        android:layout_height="70dp"
        android:layout_marginEnd="30dp"
        android:layout_marginBottom="30dp"
        android:clickable="true"
        android:src="@drawable/add"
        app:backgroundTint="#FFC107"
        app:elevation="5dp"
        app:fabCustomSize="70dp"
        app:fabSize="normal"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:maxImageSize="30dp"
        app:rippleColor="#e7d161" />
```
这样一个不会因为界面滑动而移动的悬浮按钮就设置好了
然后可以在activity中注册悬浮按钮的点击事件
``` java
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
```
这样我就可以通过这个悬浮按钮跳转editor新建note



