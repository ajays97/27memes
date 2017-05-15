package com.techurity.a27memes.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.techurity.a27memes.R;
import com.techurity.a27memes.app.AppController;
import com.techurity.a27memes.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajay Srinivas on 5/11/2017.
 */

public class TimelineAdapter extends ArrayAdapter<Post> {

    private LayoutInflater inflater;

    TextView creator, created_at, tags;

    private List<Post> posts;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public TimelineAdapter(Context context, ArrayList<Post> posts) {
        super(context, R.layout.feed_row, posts);
        this.posts = posts;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Post getItem(int i) {
        return posts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = LayoutInflater.from(getContext());
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        NetworkImageView feedImage = (NetworkImageView) convertView
                .findViewById(R.id.feed_image);
        creator = (TextView) convertView.findViewById(R.id.creator);
        created_at = (TextView) convertView.findViewById(R.id.created_at);
        tags = (TextView) convertView.findViewById(R.id.tags);

        Post post = posts.get(position);

        creator.setText(post.getCreator());
        created_at.setText(post.getCreated_at());
        tags.setText(post.getTags());

        feedImage.setImageUrl(post.getImage_url(), imageLoader);

        return convertView;
    }
}
