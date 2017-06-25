package com.techurity.a27memes.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.techurity.a27memes.R;
import com.techurity.a27memes.model.Comment;
import com.techurity.a27memes.model.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ajay Srinivas on 6/25/2017.
 */

public class CommentAdapter extends ArrayAdapter<Comment> {

    private List<Comment> comments;
    private LayoutInflater inflater;

    TextView creator, message;

    public CommentAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        super(context, R.layout.comment_row, comments);
        this.comments = comments;
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Nullable
    @Override
    public Comment getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (inflater == null)
            inflater = LayoutInflater.from(getContext());
        if (convertView == null)
            convertView = inflater.inflate(R.layout.comment_row, null);

        creator = (TextView) convertView.findViewById(R.id.comCreator);
        message = (TextView) convertView.findViewById(R.id.comDescription);

        Comment comment = comments.get(position);
        creator.setText("->" + comment.getCreator());
        message.setText(comment.getMessage());

        return convertView;

    }
}
