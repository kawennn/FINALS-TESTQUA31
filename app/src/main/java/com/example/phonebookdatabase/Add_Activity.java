package com.example.phonebookdatabase;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Add_Activity extends AppCompatActivity implements View.OnClickListener {
    EditText name, phone;
    ImageButton iv, ib1, ib2;

    private Uri uri;
    private String n, p;
    private int contactId, updateContact, pSpc5 = 1, pSpc8 = 1;
    private byte[] by;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_);

        setTitle("Add a Contact");

        dialog = new Dialog(this);
        updateContact = 0;

        iv = findViewById(R.id.ivImage);
        name = findViewById(R.id.etName);
        phone = findViewById(R.id.etPhone);
        ib1 = findViewById(R.id.btnAdd);
        ib2 = findViewById(R.id.btnCancel);

        ib1.setOnClickListener(this);
        ib2.setOnClickListener(this);

        iv.setOnClickListener(this);

        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            contactId = b.getInt("id");
            uri = b.getParcelable("uri");
            n = b.getString("name");
            p = b.getString("phone");
            by = b.getByteArray("imgbyte");

            Bitmap bm = BitmapFactory.decodeByteArray(by, 0, by.length);
            this.iv.setImageBitmap(bm);
            this.name.setText(n);
            this.phone.setText(p);

            updateContact = 1;

            setTitle("Edit Contact");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        n = name.getText().toString();
        p = phone.getText().toString();
        if (id == R.id.btnAdd) {
            if (!n.equals("") && !p.equals("") && uri != null) {
                Intent intent = new Intent();
                if (updateContact == 1) {
                    intent.putExtra("id", contactId);
                    //intent.putExtra("phone", p);
                    intent.putExtra("imgbyte", by);
                }
                intent.putExtra("uri", uri);
                intent.putExtra("name", n);
                intent.putExtra("phone", p);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else Toast.makeText(this, "Please provide the fields and image", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.ivImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 11);
        } else if (id == R.id.btnCancel) {
            if (updateContact == 1) {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 11){
                uri = data.getData();
                iv.setImageURI(uri);

                by = convertImageToByte(uri);
            }
        }
    }
    private byte[] convertImageToByte(Uri uri) {
        byte[] data = null;
        try{
            ContentResolver cr =getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            data = baos.toByteArray();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return data;
    }
}