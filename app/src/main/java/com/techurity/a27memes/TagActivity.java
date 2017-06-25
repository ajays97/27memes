package com.techurity.a27memes;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.techurity.a27memes.adapter.CommentAdapter;
import com.techurity.a27memes.app.AppController;
import com.techurity.a27memes.model.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TagActivity extends AppCompatActivity {

    NetworkImageView main_image;
    String post_id, image_url;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    ArrayList<Comment> commentsList = new ArrayList<Comment>();
    CommentAdapter commentAdapter;
    ListView commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        Intent intent = getIntent();
        post_id = intent.getStringExtra(Intent.EXTRA_TEXT);
        image_url = intent.getStringExtra("IMAGE_URL");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View commentHeader = inflater.inflate(R.layout.comment_header, null);
        main_image = (NetworkImageView) commentHeader.findViewById(R.id.com_image);
        main_image.setDrawingCacheEnabled(true);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        Log.d("Image URL", image_url);
        main_image.setImageUrl(image_url, imageLoader);

        commentList = (ListView) findViewById(R.id.commentslist);
        commentAdapter = new CommentAdapter(this, commentsList);
        commentList.setAdapter(commentAdapter);

        commentList.addHeaderView(commentHeader);

        updateComments(post_id);

    }

    public void updateComments(String post_id) {

        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + post_id + "/comments",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONArray mainArray = null;
                        JSONArray commentArray = null;
                        try {
                            JSONObject mainObj = response.getJSONObject();
                            mainArray = mainObj.getJSONArray("data");
                            JSONObject commentObj = mainArray.getJSONObject(1).getJSONObject("comments");
                            commentArray = commentObj.getJSONArray("data");

                            JSONObject comment;
                            for (int i = 0; i < 5; i++) {
                                comment = commentArray.getJSONObject(i);
                                JSONObject fromObj = comment.getJSONObject("from");
                                commentsList.add(new Comment(fromObj.getString("name"), comment.getString("message")));
                                Log.d("Output", comment.toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "comments.limit(5){from,message}");
        request.setParameters(parameters);
        request.executeAsync();
        ;
    }

}
