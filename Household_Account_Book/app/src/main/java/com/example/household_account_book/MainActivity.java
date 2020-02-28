package com.example.household_account_book;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Calendar;

import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_CLASS_TEXT;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private EditText editTextDate, editTextMoney,editTextValue;
    private DataBaseHelper helper;
    private SQLiteDatabase db;
    private TextView textView;
    private String[] result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextDate   = findViewById(R.id.insertdate);
        editTextMoney  = findViewById(R.id.insertmoney);
        editTextMoney.setInputType(TYPE_CLASS_NUMBER);
        editTextValue  = findViewById(R.id.insertvalue);
        editTextValue.setInputType(TYPE_CLASS_TEXT);

        listView  = findViewById(R.id.listView);
        textView  = findViewById(R.id.textView_ErrMessage);

        //登録日付入力押すと、カレンダーより日付を選択
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar date = Calendar.getInstance();

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                editTextDate.setText(String.format("%d/%02d/%02d", year, month+1, dayOfMonth));
                            }
                        },
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DATE)
                );
                datePickerDialog.show();
            }
        });


        //DB読込ボタン押す
        Button ReadButton = findViewById(R.id.readDate);
        ReadButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                readData();
            }
        });


        //登録ボタン押す
        Button insertButton = findViewById(R.id.insertbutton);
        insertButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {

                if(helper == null){
                    helper = new DataBaseHelper(getApplicationContext());
                }

                if(db == null){
                    db = helper.getWritableDatabase();
                }

                String date  = editTextDate.getText().toString();
                String value = editTextValue.getText().toString();
                String money = editTextMoney.getText().toString();

                if(date.length()==0 || value.length()==0 || money.length()==0) {
                    textView.setText("登録日付、登録内容、金額を入力してから登録ボタンを押して下さい");
                    textView.setTextColor(Color.RED);
                } else{
                    textView.setText("登録データ1件");
                    textView.setTextColor(Color.BLACK);
                    insertData(db, date,value,money);
                    editTextDate.getEditableText().clear();
                    editTextValue.getEditableText().clear();
                    editTextMoney.getEditableText().clear();
                }
            }
        });

        //昇順ボタン押す
        Button upListButton = findViewById(R.id.upList);
        upListButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                upListReadData();
            }
        });

        //降順ボタン押す
        Button downListButton = findViewById(R.id.downList);
        downListButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                downListReadData();
            }
        });

    }

        private void insertData(SQLiteDatabase db, String date, String value,String money){
            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("value", value);
            values.put("money", money);
            db.insert("housedb", null, values);
            readData();
        }

    //DB読み込み
    private void readData(){
        if(helper == null){
            helper = new DataBaseHelper(getApplicationContext());
        }

        if(db == null){
            db = helper.getReadableDatabase();
        }
        Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "housedb",
                new String[] { "date", "value","money" },
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        StringBuilder sbuilder = new StringBuilder();

        if(cursor.getCount()==0){
            textView.setText("データ登録0件");
            textView.setTextColor(Color.RED);
            return;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append("_");
            sbuilder.append(cursor.getString(1));
            sbuilder.append("(");
            sbuilder.append(cursor.getString(2)).append("円");
            sbuilder.append(")");
            sbuilder.append(" ");
            cursor.moveToNext();
        }
        
        cursor.close();
        Log.d("debug","**********"+sbuilder.toString());

        result = sbuilder.toString().split(" ");

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, result);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(result[position].length()==0){
                    textView.setText("登録データ0件");
                    textView.setTextColor(Color.RED);
                    return;
                }
                    final String selectDate  = result[position].substring(0, result[position].indexOf("_"));
                    final String selectValue = result[position].substring(result[position].indexOf("_")+1, result[position].lastIndexOf("("));
                    final String selectMoney = result[position].substring(result[position].indexOf("(")+1, result[position].lastIndexOf("円"));

                if(selectDate.length()!=0){
                    alertCheck(selectDate, selectValue, selectMoney);
                }

            }
        });
    }

    //降順DB読み込み
    private void downListReadData(){
        if(helper == null){
            helper = new DataBaseHelper(getApplicationContext());
        }

        if(db == null){
            db = helper.getReadableDatabase();
        }
        Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "housedb",
                new String[] { "date", "value","money" },
                null,
                null,
                null,
                null,
                "date ASC"
        );

        cursor.moveToFirst();
        StringBuilder sbuilder = new StringBuilder();

        if(cursor.getCount()==0 || cursor.getCount()==1){
            textView.setText("データ登録1件以下の為、降順できません。");
            textView.setTextColor(Color.RED);
            return;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append("_");
            sbuilder.append(cursor.getString(1));
            sbuilder.append("(");
            sbuilder.append(cursor.getString(2)).append("円");
            sbuilder.append(")");
            sbuilder.append(" ");
            cursor.moveToNext();
        }

        cursor.close();
        Log.d("debug","**********"+sbuilder.toString());

        result = sbuilder.toString().split(" ");

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, result);
        listView.setAdapter(arrayAdapter);
    }

    //昇順DB読み込み
    private void upListReadData(){
        if(helper == null){
            helper = new DataBaseHelper(getApplicationContext());
        }

        if(db == null){
            db = helper.getReadableDatabase();
        }
        Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "housedb",
                new String[] { "date", "value","money" },
                null,
                null,
                null,
                null,
                "date DESC"
        );

        cursor.moveToFirst();

        if(cursor.getCount()==0 || cursor.getCount()==1){
            textView.setText("データ登録1件以下の為、昇順できません。");
            textView.setTextColor(Color.RED);
            return;
        }

        StringBuilder sbuilder = new StringBuilder();

        for (int i = 0; i < cursor.getCount(); i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append("_");
            sbuilder.append(cursor.getString(1));
            sbuilder.append("(");
            sbuilder.append(cursor.getString(2)).append("円");
            sbuilder.append(")");
            sbuilder.append(" ");
            cursor.moveToNext();
        }

        cursor.close();
        Log.d("debug","**********"+sbuilder.toString());

        result = sbuilder.toString().split(" ");

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, result);
        listView.setAdapter(arrayAdapter);
    }


    private void alertCheck(final String selectDate, final String selectValue, final String selectMoney) {
        String[] alert_menu = {"更新", "削除", "キャンセル"};
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setItems(alert_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int idx) {
                // リストアイテムを選択したときの処理
                // 更新
                if (idx == 0) {
                    // ダイアログの設定
                    alertDialog.setTitle("データ更新");
                    alertDialog.setMessage("現在選択しているデータを更新しますか?");

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            String updateDate  = editTextDate.getText().toString();
                            String updateValue = editTextValue.getText().toString();
                            String updateMoney = editTextMoney.getText().toString();

                            if(updateDate.length()==0 || updateValue.length()==0 || updateMoney.length()==0){
                                textView.setText("登録日付、登録内容、金額を入力してからデータ更新をして下さい。");
                                textView.setTextColor(Color.RED);
                                return;
                            }

                            updateData(selectDate,selectValue,selectMoney,updateDate,updateValue,updateMoney);
                            Log.d("AlertDialog", "Positive which :" + which);
                        }
                    });
                    alertDialog.setNegativeButton("NG", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("AlertDialog", "Negative which :" + which);
                        }
                    });
                }
                // 削除
                else if (idx == 1) {
                    // ダイアログの設定
                    alertDialog.setTitle("データ削除");
                    alertDialog.setMessage("現在選択しているデータを削除しますか?");

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteData(selectDate,selectValue,selectMoney);
                            Log.d("AlertDialog", "Positive which :" + which);
                        }
                    });
                    alertDialog.setNegativeButton("NG", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("AlertDialog", "Negative which :" + which);
                        }
                    });
                }
                else {
                    textView.setText("");
                    Log.d("debug", "cancel");
                    return;
                }
                alertDialog.show();
            }
        });
        alertDialog.show();
    }


    //DB削除処理
    private void deleteData(String deleteDate,String deleteValue,String deleteMoney) {
        try {
            db.delete("housedb", "date=? AND value=? AND money=?", new String[]{deleteDate,deleteValue,deleteMoney});
            readData();
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }
    }

    //DB更新処理
    private void updateData(String selectDate,String selectValue,String selectMoney,String updateDate,String updateValue,String updateMoney) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("date", updateDate);
            cv.put("value", updateValue);
            cv.put("money", updateMoney);
            db.update("housedb", cv, "date=? AND value=? AND money=?", new String[]{selectDate,selectValue,selectMoney});
            readData();
        } catch (SQLException e) {
            Log.e("ERROR", e.toString());
        }
    }
}
