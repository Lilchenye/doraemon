package com.example.doraemon.Note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doraemon.MainActivity;
import com.example.doraemon.R;

import java.util.ArrayList;
import java.util.List;

public class MyNote extends AppCompatActivity {

    DBService myDb;
    private Button mBtnAdd;
    private Button bcBtn;
    private ListView lv_note;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_main);
        //实例化自己创建的数据库类
        myDb = new DBService(this);
        //初始化函数
        init();
    }

    public void init(){

        Adapter adapter1;
        mBtnAdd = findViewById(R.id.btn_add);
        bcBtn = findViewById(R.id.btn_backmenu);
        lv_note = findViewById(R.id.lv_note);
        //创建Values类型的list保存数据库中的数据
        List<Values> valuesList = new ArrayList<>();
        //获得可读SQLiteDatabase对象
        SQLiteDatabase db = myDb.getReadableDatabase();

        //查询数据库中的数据
        Cursor cursor = db.query(DBService.TABLE,null,null,
                null,null,null,null);
        if(cursor.moveToFirst()){
            Values values;
            while (!cursor.isAfterLast()){
                //实例化values对象
                values = new Values();

                //把数据库中的一个表中的数据赋值给values
                values.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(DBService.ID))));
                values.setTitle(cursor.getString(cursor.getColumnIndex(DBService.TITLE)));
                values.setContent(cursor.getString(cursor.getColumnIndex(DBService.CONTENT)));
                values.setTime(cursor.getString(cursor.getColumnIndex(DBService.TIME)));

                //将values对象存入list对象数组中
                valuesList.add(values);
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();

        //设置list组件adapter
        final MyBaseAdapter myBaseAdapter = new MyBaseAdapter(valuesList,this,R.layout.note_item);
        lv_note.setAdapter(myBaseAdapter);

        //按钮点击事件
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyNote.this, EditActivity.class);
                startActivity(intent);
            }
        });
        bcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyNote.this, MainActivity.class);
                startActivity(intent);
            }
        });



        //单击修改
        lv_note.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyNote.this, ShowActivity.class);
                Values values = (Values) lv_note.getItemAtPosition(position);
                intent.putExtra(DBService.TITLE,values.getTitle().trim());
                intent.putExtra(DBService.CONTENT,values.getContent().trim());
                intent.putExtra(DBService.TIME,values.getTime().trim());
                intent.putExtra(DBService.ID,values.getId().toString().trim());
                startActivity(intent);
            }
        });


        //长按删除
        lv_note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Values values = (Values) lv_note.getItemAtPosition(position);
                new AlertDialog.Builder(MyNote.this)
                        .setTitle("提示框")
                        .setMessage("是否删除?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SQLiteDatabase db = myDb.getWritableDatabase();
                                        db.delete(DBService.TABLE,DBService.ID+"=?",new String[]{String.valueOf(values.getId())});
                                        db.close();
                                        myBaseAdapter.removeItem(position);
                                        lv_note.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                myBaseAdapter.notifyDataSetChanged();
                                            }
                                        });
                                        //MainActivity.this.onResume();
                                    }
                                })
                        .setNegativeButton("no",null).show();
                return true;
            }
        });
    }

    //创建继承自BaseAdapter的实现类进行ListView的展示
    class MyBaseAdapter extends BaseAdapter{

        private List<Values> valuesList;
        private Context context;
        private int layoutId;

        public MyBaseAdapter(List<Values> valuesList, Context context, int layoutId) {
            this.valuesList = valuesList;
            this.context = context;
            this.layoutId = layoutId;
        }

        @Override
        public int getCount() {
            if (valuesList != null && valuesList.size() > 0)
                return valuesList.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            if (valuesList != null && valuesList.size() > 0)
                return valuesList.get(position);
            else
                return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(
                        getApplicationContext()).inflate(R.layout.note_item, parent,
                        false);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.content = convertView.findViewById(R.id.tv_content);
                viewHolder.time = (TextView) convertView.findViewById(R.id.tv_time);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String title = valuesList.get(position).getTitle();
            String content = valuesList.get(position).getContent();
            viewHolder.title.setText(title);
            viewHolder.content.setText(content);
            viewHolder.time.setText(valuesList.get(position).getTime());
            return convertView;
        }
        public void removeItem(int position){
            this.valuesList.remove(position);
        }

    }
    class ViewHolder{
        TextView title;
        TextView content;
        TextView time;
    }
}

