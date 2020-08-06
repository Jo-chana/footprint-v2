package com.chana.footprint.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.chana.footprint.R;

import java.util.ArrayList;


public class GridAdapter extends BaseAdapter {

    ArrayList<Bitmap> images;
    Context context;
    boolean canRevise = true;
    View.OnClickListener listener;

    public GridAdapter(Context context, ArrayList<Bitmap> images, boolean canRevise){
        this.context = context;
        this.images = images;
        this.canRevise = canRevise;
    }

    public void setCanRevise(boolean canRevise){
        this.canRevise = canRevise;
    }

    public void setListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Bitmap image = images.get(position);
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.image);
        imageView.setImageBitmap(image);
        imageView.setClipToOutline(true);

        if(canRevise && position==0){
            imageView.setOnClickListener(listener);
        }

        return convertView;
    }
}
