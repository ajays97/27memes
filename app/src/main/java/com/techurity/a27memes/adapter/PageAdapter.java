package com.techurity.a27memes.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.ads.NativeExpressAdView;
import com.techurity.a27memes.PageActivity;
import com.techurity.a27memes.R;
import com.techurity.a27memes.app.AppController;
import com.techurity.a27memes.model.PagePost;
import com.techurity.a27memes.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajay Srinivas on 6/26/2017.
 */

public class PageAdapter extends ArrayAdapter<PagePost>{


    private LayoutInflater inflater;

    TextView creator, created_at, tags, message;

    private List<PagePost> posts;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public PageAdapter(Context context, ArrayList<PagePost> posts) {
        super(context, R.layout.feed_row, posts);
        this.posts = posts;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public PagePost getItem(int i) {
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
        feedImage.setDefaultImageResId(R.drawable.loading);
        creator = (TextView) convertView.findViewById(R.id.creator);
        created_at = (TextView) convertView.findViewById(R.id.created_at);
        tags = (TextView) convertView.findViewById(R.id.tags);
        message = (TextView) convertView.findViewById(R.id.message);

        PagePost post = posts.get(position);

        creator.setText(post.getCreator());
        created_at.setText(post.getCreated_at());
        tags.setText(post.getTags());
        message.setText(post.getMessage());

        if(post.getTags().equals("Click to Play Video"))
            feedImage.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.playoverlay));
        else
            feedImage.setForeground(null);

        feedImage.setImageUrl(post.getImage_url(), imageLoader);

        return convertView;
    }
}
