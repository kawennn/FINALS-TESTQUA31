package com.example.phonebookdatabase;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText search;
    ListView lv;
    Dialog dialog;
    MyDB db;

    ArrayList<Student> list = new ArrayList<>();
    ArrayList<Student> filteredlist = new ArrayList<Student>();
    CustomAdapter adapter;
    AdapterView.AdapterContextMenuInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        search = findViewById(R.id.txtSearch);
        lv = findViewById(R.id.listView1);

        db = new MyDB(this);

        adapter = new CustomAdapter(this, filteredlist, this);
        lv.setAdapter(adapter);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filteredlist.clear();
                String regex = charSequence.toString();
                Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                for (Student per : list) {
                    Matcher m = p.matcher(per.getName());
                    if (m.find()) {
                        filteredlist.add(per);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) { }});

        registerForContextMenu(lv);
        list = db.getAllContact();
        refreshFilter();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0){
                Bundle b = data.getExtras();
                Uri uri = b.getParcelable("uri");
                String name = b.getString("name");
                String phone = b.getString("phone");

                Student contact = new Student(uri, name, phone, convertImageToByte(uri));
                db.addContact(contact);
                list = db.getAllContact();

                refreshFilter();
                Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show();
            } else if (requestCode == 1) { // for updating
                Bundle b = data.getExtras();
                if (b != null) {
                    int contactId = b.getInt("id");
                    Uri uri = b.getParcelable("uri");
                    String name = b.getString("name");
                    String phone = b.getString("phone");
                    byte[] by = b.getByteArray("imgbyte");

                    Student contact = new Student(contactId, uri, name, phone, by);

                    db.updateContact(contact, contactId);
                    list = db.getAllContact();

                    refreshFilter();
                    Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    public void refreshFilter() {
        String regex = "";
        filteredlist.clear();
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        //cnt = 0;
        for (Student contact : list) {
            Matcher m = p.matcher(contact.getName());
            if (m.find()) {
                filteredlist.add(contact);
                //cnt++;
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.optionmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(this, Add_Activity.class);
        startActivityForResult(intent,0);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.mymenu,menu);
        info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(filteredlist.get(info.position).getName().toUpperCase());
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String phone = filteredlist.get(info.position).getPhone();
        if (id == R.id.call) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            }else startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phone)));
        }else if (id == R.id.sms){
            try{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms: " + phone));
                startActivity(intent);
            }catch (Exception e){
                Toast.makeText(this, ""+e, Toast.LENGTH_LONG).show();
            }
        }else if(id == R.id.delete){
            int contactId = filteredlist.get(info.position).getId();
            int result = db.deleteContact(contactId);
            String message = (result>0)?"Contact Deleted": "Error Deleting Item";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            list = db.getAllContact();
            refreshFilter();
        }else if(id == R.id.update){
            Intent intent = new Intent(this, Add_Activity.class);
            intent.putExtra("id", filteredlist.get(info.position).getId());
            intent.putExtra("image", filteredlist.get(info.position).getImg());
            intent.putExtra("name", filteredlist.get(info.position).getName());
            intent.putExtra("phone", filteredlist.get(info.position).getPhone());
            intent.putExtra("imgbyte", filteredlist.get(info.position).by);
            startActivityForResult(intent,1);
           // adapter.notifyDataSetChanged();
        }
        refreshFilter();
        adapter.notifyDataSetChanged();
        return super.onContextItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+filteredlist.get(info.position).getPhone())));
            }else Toast.makeText(this, "Permission declined", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public byte[] convertImageToByte(Uri uri){
        byte[] data = null;
        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            data = baos.toByteArray();
        }catch ( FileNotFoundException e){
            e.printStackTrace();
        }
        return  data;
    }
    public Bitmap convertByteArrayToBitmap(byte[] by){
        return BitmapFactory.decodeByteArray(by, 0, by.length);
    }
    public static byte[] convertBitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,0,stream);
        return  stream.toByteArray();
    }
}