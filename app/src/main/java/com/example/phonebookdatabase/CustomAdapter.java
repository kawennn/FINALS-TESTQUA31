package com.example.phonebookdatabase;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Student> list;
    private LayoutInflater inflater;
    public MainActivity ma;

    public CustomAdapter(Context context, ArrayList<Student> list, MainActivity ma) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
        this.ma = ma;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ItemHolder handler = null;
        if(view == null){
            view = inflater.inflate(R.layout.list, null);
            handler = new ItemHolder();
            handler.img = view.findViewById(R.id.imageView1);
            handler.name = view.findViewById(R.id.lblName);
            handler.phone = view.findViewById(R.id.lblNum);
            view.setTag(handler);
        }else handler = (ItemHolder) view.getTag();

        handler.img.setImageBitmap(BitmapFactory.decodeByteArray(list.get(i).by, 0, list.get(i).by.length));
        //handler.img.setImageURI(list.get(i).getImg());
        handler.name.setText(list.get(i).getName());
        handler.phone.setText(list.get(i).getPhone());

        return view;
    }

    static class ItemHolder{
        ImageView img;
        TextView name, phone;
    }
}
