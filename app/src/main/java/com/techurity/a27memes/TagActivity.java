package com.techurity.a27memes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
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
    GraphResponse lastResponse = null;

    Button loadmore;
    View commentFooter;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        post_id = intent.getStringExtra(Intent.EXTRA_TEXT);
        image_url = intent.getStringExtra("IMAGE_URL");

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View commentHeader = inflater.inflate(R.layout.comment_header, null);
        main_image = (NetworkImageView) commentHeader.findViewById(R.id.com_image);
        main_image.setDrawingCacheEnabled(true);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        commentFooter = inflater.inflate(R.layout.comment_footer, null);

        loadmore = (Button) commentFooter.findViewById(R.id.load_more_comments);

        main_image.setImageUrl(image_url, imageLoader);

        commentList = (ListView) findViewById(R.id.commentslist);
        commentAdapter = new CommentAdapter(this, commentsList);
        commentList.setAdapter(commentAdapter);

        commentList.addHeaderView(commentHeader);

        loadmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreComments();
            }
        });

        main_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(TagActivity.this, ViewImageActivity.class);
                intent1.putExtra(Intent.EXTRA_TEXT, image_url);
                startActivity(intent1);
            }
        });

        updateComments(post_id);

    }

    public void updateComments(String post_id) {

        Bundle params = new Bundle();
        params.putString("limit", "10");
        params.putString("order", "reverse_chronological");

        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + post_id + "/comments", params, HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        lastResponse = response;

                        JSONArray mainArray = null;

                        try {
                            JSONObject mainObj = response.getJSONObject();
                            mainArray = mainObj.getJSONArray("data");

                            for (int i = 0; i < 10; i++) {
                                JSONObject commentObj = mainArray.getJSONObject(i);
                                JSONObject fromObj = commentObj.getJSONObject("from");
                                commentsList.add(new Comment(fromObj.getString("name"), commentObj.getString("message")));
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error fetching comments", Toast.LENGTH_SHORT).show();
                        } finally {
                            commentAdapter.notifyDataSetChanged();
                        }
                    }
                });
        request.executeAsync();

        commentList.addFooterView(commentFooter);

    }

    public void loadMoreComments() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching More Memes...");
        pDialog.show();
        commentList.removeFooterView(commentFooter);

        if (lastResponse != null) {
            GraphRequest newRequest = lastResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
            if (newRequest != null) {
                newRequest.setCallback(new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        lastResponse = response;
                        JSONArray mainArray = null;

                        try {
                            JSONObject mainObj = response.getJSONObject();

                            mainArray = mainObj.getJSONArray("data");

                            for (int i = 0; i < 10; i++) {
                                JSONObject commentObj = mainArray.getJSONObject(i);
                                JSONObject fromObj = commentObj.getJSONObject("from");
                                commentsList.add(new Comment(fromObj.getString("name"), commentObj.getString("message")));
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error fetching comments", Toast.LENGTH_SHORT).show();
                        } finally {
                            commentAdapter.notifyDataSetChanged();
                        }
                    }
                });
                newRequest.executeAsync();
            }
        }
        commentList.addFooterView(commentFooter);
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

}
