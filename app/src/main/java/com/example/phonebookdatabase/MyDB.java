package com.example.phonebookdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;

public class MyDB extends SQLiteOpenHelper {

    static String DATABASE = "phonebook_db";
    static  String TBL_PHONE = "tbl_phonebook";
    Context context;

    public MyDB(Context context) {
        super(context, DATABASE, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TBL_PHONE + " (id integer primary key autoincrement, uriimage varchar(50), name varchar(25), phone varchar(25), imgbyte blob)";
        db.execSQL(sql);
    }

    public ArrayList<Student> getAllContact(){
        ArrayList<Student> list = new ArrayList<Student>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TBL_PHONE, null,null,null,null,null,"name");

        c.moveToFirst();
        while(!c.isAfterLast()){
            int id = c.getInt(c.getColumnIndex("id"));
            Uri uriimage = Uri.parse(c.getString(c.getColumnIndex("uriimage")) );
            String name = c.getString(c.getColumnIndex("name"));
            String phone = c.getString(c.getColumnIndex("phone"));
            byte[] by = c.getBlob(c.getColumnIndex("imgbyte"));
            list.add(new Student(id, uriimage, name, phone, by));

            c.moveToNext();
        }

        db.close(); // close database connection
        return list;
    }

    public long addContact(Student student) {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("uriimage", student.getImg().toString());
        cv.put("name", student.getName());
        cv.put("phone", student.getPhone());
        cv.put("imgbyte", student.by);
        result = db.insert(TBL_PHONE, null, cv);

        db.close();// close database connection

        return result;
    }

    public int deleteContact(int id){
        int result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        result = db.delete(TBL_PHONE, "id=?", new String[]{id+""});
        db.close();
        return result;
    }

    public long updateContact(Student student, int contactId) {
        long result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("uriimage", student.getImg().toString());
        cv.put("name", student.getName());
        cv.put("phone", student.getPhone());
        cv.put("imgbyte", student.by);

        result = db.update(TBL_PHONE, cv,"id="+contactId, null);

        db.close();// close database connection

        return result;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}