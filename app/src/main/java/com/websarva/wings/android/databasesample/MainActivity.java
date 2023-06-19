package com.websarva.wings.android.databasesample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    /**
     * 選択されたカクテルの主キーIDを表すフィールド。
     */
    private int _cocktailId = -1;//変更。

    /**
     * 選択されたカクテル名を表すフィールド。
     */
    private String _cocktailName = "";//変更。
    /**
     * データベースヘルパーオブジェクト
     */
    private DatabaseHelper _helper;//変更。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //カクテル名を表示するTextViewを取得。
        ListView lvCocktail = findViewById(R.id.lvCocktail);//変更。
        //lvCocktailにリスナを登録。
        lvCocktail.setOnItemClickListener(new ListItemClickListener());

        //DBヘルパーオブジェクトを生成生成
        _helper = new DatabaseHelper(MainActivity.this);

    }

    @Override
    protected void onDestroy() {
        //DBヘルパーオブジェクトの解放
        _helper.close();
        super.onDestroy();
    }

    /**
     * ［保存］ボタンがタップされたときの処理。
     */
    public void onSaveButtonClick(View view) {
        //感想欄を取得。
        EditText etNote = findViewById(R.id.etNote);
        String note = etNote.getText().toString();

        //データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得。
        SQLiteDatabase db = _helper.getWritableDatabase();
        //まず、リストで選択されたカクテルのメモデータを削除。その後インサートを行う。
        //削除用SQL文字列を用意。
        String sqlDelete = "DELETE FROM cocktailmemos WHERE _id = ?";
        //SQL文字列をもとにプリペアードステートメントを取得。
        SQLiteStatement stmt = db.compileStatement(sqlDelete);
        //変数のバインド。
        stmt.bindLong(1, _cocktailId);
        //削除SQLの実行。
        stmt.executeUpdateDelete();
        //インサート用SQL文字列の用意。
        String sqlInsert = "INSERT INTO cocktailmemos (_id,name,note) VALUES (?,?,?)";
        //SQL文字列をもとにプリペアードステートメントを取得。
        stmt = db.compileStatement(sqlInsert);
        //変数のバインド。
        stmt.bindLong(1, _cocktailId);
        stmt.bindString(2, _cocktailName);
        stmt.bindString(3, note);
        //インサートSQLの実行。
        stmt.executeInsert();

        //カクテル名を［未選択］に変更。
        TextView tvCocktailName = findViewById(R.id.tvCocktailName);
        tvCocktailName.setText(getString(R.string.tv_name));
        //感想欄の入力値を消去。
        etNote.setText("");
        //［保存］ボタンをタップできないように変更。
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
    }

    /**
     * リストがタップされたときの処理が記述されたメンバクラス。
     */
    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //タップされた行番号をフィールドの主キーIDに代入。
            _cocktailId = position;
            //タップされた行のデータを取得。これがカクテル名となるのでフィールドに代入。
            _cocktailName = (String) parent.getItemAtPosition(position);
            //カクテル名を表示するTextViewに表示カクテル名を設定。
            TextView tvCockTailName = findViewById(R.id.tvCocktailName);
            tvCockTailName.setText(_cocktailName);
            //［保存］ボタンをタップできるように設定。
            Button btnSave = findViewById(R.id.btnSave);
            btnSave.setEnabled(true);

            //データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得。
            SQLiteDatabase db = _helper.getWritableDatabase();
            //主キーによる検索SQL文字列の用意。
            String sql = "SELECT * FROM cocktailmemos WHERE _id =" + _cocktailId;
            //SQLの実行。
            Cursor cursor = db.rawQuery(sql, null);
            //データベースから取得した値を格納する変数の用意。データがなかったときのための初期値も用意。
            String note = "";
            //SQLの実行の戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得。
            while (cursor.moveToNext()) {
                //カラムのインデックス値を取得。
                int idxNote = cursor.getColumnIndex("note");
                //カラムのインデックス値をもとに実際のデータを取得。
                note = cursor.getString(idxNote);
            }
            //感想のEditTextの各画面部品を取得しデータベースの値を反映。
            EditText etNote = findViewById(R.id.etNote);
            etNote.setText(note);
        }
    }
}