package com.techurity.a27memes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.techurity.a27memes.adapter.TimelineAdapter;
import com.techurity.a27memes.app.AppController;
import com.techurity.a27memes.model.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//Page ID = 1713086835593817
//Sarcasm ID = 1515871602074952

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    JsonObjectRequest objectRequest2 = null;

    View footer;
    Button loadMore;

    ListView feedList;
    TimelineAdapter feedAdapter;
    private ProgressDialog pDialog;
    GraphResponse lastResponse = null;
    ArrayList<Post> postList = new ArrayList<Post>();

    JSONObject nextObj;

    String next = null;

    SwipeRefreshLayout refreshLayout;
    SwipeRefreshLayout.OnRefreshListener refreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        feedList = (ListView) findViewById(R.id.feedList);
        feedAdapter = new TimelineAdapter(this, postList);

        feedList.setAdapter(feedAdapter);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer = inflater.inflate(R.layout.feed_footer, null);
        loadMore = (Button) footer.findViewById(R.id.load_more);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoreMemes(next);
            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                lastResponse = null;
                next = null;
                feedList.removeFooterView(footer);
                updateFeed();
                refreshLayout.setRefreshing(false);
            }
        };

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                lastResponse = null;
                next = null;
                feedList.removeFooterView(footer);
                updateFeed();
                refreshLayout.setRefreshing(false);
            }
        });

        feedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Post post = (Post) adapterView.getItemAtPosition(position);
                String url = post.getImage_url();

                Intent intent = new Intent(MainActivity.this, ViewImageActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(intent);
            }
        });

        updateFeed();

    }

    public void updateFeed() {
        postList = new ArrayList<Post>();
        feedAdapter = new TimelineAdapter(getApplicationContext(), postList);
        feedList.setAdapter(feedAdapter);

        feedList.addFooterView(footer);


        JsonObjectRequest objectRequest = new JsonObjectRequest("https:graph.facebook.com/v2.9/1515871602074952/feed?fields=full_picture&limit=5&access_token=" + AccessToken.getCurrentAccessToken().getToken(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = jsonObject.getJSONArray("data");
                            nextObj = jsonObject.getJSONObject("paging");
                            next = nextObj.getString("next");
                            for (int i = 0; i < 5; i++) {
                                JSONObject postObj = jsonArray.getJSONObject(i);
                                String image_url = postObj.getString("full_picture");
                                String post_id = postObj.getString("id");
                                feedAdapter.add(new Post(post_id, image_url));
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "An Error Occurred", Toast.LENGTH_SHORT).show();
                        } finally {
                            feedAdapter.notifyDataSetChanged();
                            hidePDialog();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.v("Output", volleyError.toString());
            }
        });

        AppController.getInstance().addToRequestQueue(objectRequest);

    }

    private void loadMoreMemes(String nextUrl) {

        feedList.removeFooterView(footer);

        if (nextUrl != null) {
            objectRequest2 = new JsonObjectRequest(next, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {

                            try {
                                JSONArray jsonArray = jsonObject.getJSONArray("data");
                                nextObj = jsonObject.getJSONObject("paging");
                                for (int i = 0; i < 5; i++) {
                                    JSONObject postObj = jsonArray.getJSONObject(i);
                                    String image_url = postObj.getString("full_picture");
                                    String post_id = postObj.getString("id");
                                    if (image_url != null)
                                        feedAdapter.add(new Post(post_id, image_url));
                                }
                                next = nextObj.getString("next");
                            } catch (JSONException e) {
                                Log.d("LOADMORE", e.toString());
                                next = null;
                            } finally {
                                feedAdapter.notifyDataSetChanged();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.v("Output", volleyError.toString());

                }
            });
            AppController.getInstance().addToRequestQueue(objectRequest2);
        }
        feedList.addFooterView(footer);

    }

    @Override
    protected void onStop() {
        super.onStop();
        AppController.getInstance().getRequestQueue().cancelAll("MyTag");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
        AppController.getInstance().getRequestQueue().cancelAll("MyTag");
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.mRefresh) {
            refreshListener.onRefresh();
        } else if (id == R.id.mLogout) {
            LoginManager.getInstance().logOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
/*
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else */
        if (id == R.id.nav_logout) {
            LoginManager.getInstance().logOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
