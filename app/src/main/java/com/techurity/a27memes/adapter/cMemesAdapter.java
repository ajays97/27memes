package com.techurity.a27memes.adapter;

import android.content.Context;
import android.media.Image;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.techurity.a27memes.R;

import org.w3c.dom.Text;

/**
 * Created by Ajay Srinivas on 5/26/2017.
 */

public class cMemesAdapter extends ArrayAdapter {

    private Context context;
    LayoutInflater inflater;
    TextView catTitle;
    ImageView catImage;
/*

    public Integer[] categories = {
            R.mipmap.cat_programmer,
            R.mipmap.cat_teen,
            R.mipmap.cat_bvg,
            R.mipmap.cat_boys,
            R.mipmap.cat_girls,
            R.mipmap.cat_adult,
            R.mipmap.cat_kids,
            R.mipmap.cat_suggest
    };
*/

    public Integer[] categories = {
            R.drawable.nav_dp,
            R.drawable.nav_dp,
            R.drawable.nav_dp,
            R.drawable.nav_dp,
            R.drawable.nav_dp,
            R.drawable.nav_dp,
            R.drawable.nav_dp,
            R.drawable.nav_dp
    };

    public String[] categs = {
            "Programmer/Dev",
            "Teen",
            "Boys V/S Girls",
            "Boys",
            "Girls",
            "Adult/18+",
            "Kids",
            "Suggest a Category"
    };

    public cMemesAdapter(@NonNull Context context) {
        super(context, R.layout.category_row);
    }

    @Override
    public int getCount() {
        return categories.length;
    }

    @Override
    public Object getItem(int position) {
        return categories[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = LayoutInflater.from(getContext());
        if (convertView == null)
            convertView = inflater.inflate(R.layout.category_row, null);

        catImage = (ImageView) convertView.findViewById(R.id.catImage);
        catTitle = (TextView) convertView.findViewById(R.id.catTitle);

//        catImage.setImageResource(categories[position]);
        catImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        catTitle.setText(categs[position]);
        return convertView;
    }

}
