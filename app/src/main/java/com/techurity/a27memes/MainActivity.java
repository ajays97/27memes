package com.techurity.a27memes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import android.widget.AdapterView;

import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.techurity.a27memes.adapter.TimelineAdapter;
import com.techurity.a27memes.app.AppController;
import com.techurity.a27memes.model.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//Page ID = 1713086835593817
//Runtime ID = 133352060545254

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String mainUrl = "https:graph.facebook.com/v2.9/1713086835593817/feed?fields=full_picture,from,created_time,message&limit=5&access_token=" + AccessToken.getCurrentAccessToken().getToken();

    JsonObjectRequest objectRequest2 = null;
    private AdView mAdView;

    boolean doubleBackToExitPressedOnce = false;

    View footer;
    Button loadMore;

    GraphRequest request;

    Calendar cal;
    String tags;

    ListView feedList;
    TimelineAdapter feedAdapter;
    private ProgressDialog pDialog;
    GraphResponse lastResponse = null;
    ArrayList<Post> postList = new ArrayList<Post>();

    JSONObject nextObj;

    String next = null;
    boolean page_check;

    SwipeRefreshLayout refreshLayout;
    SwipeRefreshLayout.OnRefreshListener refreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MobileAds.initialize(this, "ca-app-pub-2819514375619003~1342858770");

        feedList = (ListView) findViewById(R.id.feedList);
        feedAdapter = new TimelineAdapter(this, postList);

        feedList.setAdapter(feedAdapter);


        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        Bundle b = new Bundle();
        b.putString("limit", "1");
        b.putString("fields", "message");
        request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/133352060545254/posts", b, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                    JSONObject mainObj = response.getJSONObject();
                    JSONArray data = mainObj.getJSONArray("data");
                    String page = data.getJSONObject(0).getString("message");
                    mainUrl = "https:graph.facebook.com/v2.9/" + page + "/feed?fields=full_picture,from,created_time,message&limit=5&access_token=" + AccessToken.getCurrentAccessToken().getToken();
                    if (page.equals("1713086835593817")) {
                        updateFeed();
                        //updateFeed(mainUrl, true);
                    } else
                        updateFeed();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        request.executeAsync();

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
                loadMoreMemes();
            }
        });

        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                postList.clear();
                lastResponse = null;
                next = null;
                feedList.removeFooterView(footer);
                request.executeAsync();
                refreshLayout.setRefreshing(false);
            }
        };

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

/*
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setVisibility(View.GONE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                mAdView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mAdView.getVisibility() == View.GONE)
                    mAdView.setVisibility(View.VISIBLE);
            }

        });
        mAdView.loadAd(adRequest);*/


    }
/*
    public void updateFeed(String page, final boolean check) {

        page_check = check;

        Log.d("Boolean Check", "" + page_check);

        postList = new ArrayList<Post>();
        feedAdapter = new TimelineAdapter(getApplicationContext(), postList);
        feedList.setAdapter(feedAdapter);

        feedList.addFooterView(footer);
        cal = Calendar.getInstance();

        JsonObjectRequest objectRequest = new JsonObjectRequest(page, null, new Response.Listener<JSONObject>() {

            String tags;

            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("MainActivity", "Data Fetched");

                JSONArray jsonArray = null;
                try {
                    jsonArray = jsonObject.getJSONArray("data");

                    nextObj = jsonObject.getJSONObject("paging");
                    next = nextObj.getString("next");
                    for (int i = 0; i < 5; i++) {
                        JSONObject postObj = jsonArray.getJSONObject(i);
                        JSONObject fromObj = postObj.getJSONObject("from");
                        String creator = fromObj.getString("name");
                        String image_url = postObj.getString("full_picture");
                        String post_id = postObj.getString("id");
                        String created_at = postObj.getString("created_time");
                        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                        Date dates = date.parse(created_at);
                        cal.setTimeInMillis(dates.getTime());
                        String created_time = "" + cal.getTime();
                        if (check)
                            tags = postObj.getString("message");
                        else
                            tags = "No Tags";
                        feedAdapter.add(new Post(post_id, image_url, creator,
                                created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                tags));
                        Log.d("Adding", "After Request");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "An Error Occurred", Toast.LENGTH_SHORT).show();
                } catch (ParseException e) {
                    e.printStackTrace();
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

    }*/

    public void updateFeed() {

        postList = new ArrayList<Post>();
        feedAdapter = new TimelineAdapter(getApplicationContext(), postList);
        feedList.setAdapter(feedAdapter);

        feedList.addFooterView(footer);
        cal = Calendar.getInstance();

        Bundle parameters = new Bundle();
        parameters.putString("limit", "5");
        parameters.putString("fields", "full_picture,from,message,created_time");

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/1713086835593817/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                lastResponse = response;
                JSONObject mainObj = response.getJSONObject();
                try {
                    JSONArray jsonArray = mainObj.getJSONArray("data");
                    for (int i = 0; i < 5; i++) {
                        JSONObject postObj = jsonArray.getJSONObject(i);
                        JSONObject fromObj = postObj.getJSONObject("from");
                        String creator = fromObj.getString("name");
                        String image_url = postObj.getString("full_picture");
                        String post_id = postObj.getString("id");
                        String created_at = postObj.getString("created_time");
                        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                        Date dates = date.parse(created_at);
                        cal.setTimeInMillis(dates.getTime());
                        String created_time = "" + cal.getTime();
                        tags = postObj.getString("message");

                        feedAdapter.add(new Post(post_id, image_url, creator,
                                created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                tags));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    feedAdapter.notifyDataSetChanged();
                    hidePDialog();
                }

            }
        });
        request.executeAsync();

    }
/*
    private void loadMoreMemes(String nextUrl, final boolean page_check) {


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
                                    JSONObject fromObj = postObj.getJSONObject("from");
                                    String creator = fromObj.getString("name");
                                    String image_url = postObj.getString("full_picture");
                                    String post_id = postObj.getString("id");
                                    String created_at = postObj.getString("created_time");
                                    SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                                    Date dates = date.parse(created_at);
                                    cal.setTimeInMillis(dates.getTime());
                                    String created_time = "" + cal.getTime();
                                    if (page_check)
                                        tags = postObj.getString("message");
                                    else
                                        tags = "No Tags";
                                    feedAdapter.add(new Post(post_id, image_url, creator,
                                            created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                            tags));
                                }
                                next = nextObj.getString("next");
                            } catch (JSONException e) {
                                Log.d("LOADMORE", e.toString());
                                next = null;
                            } catch (ParseException e) {
                                e.printStackTrace();
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

    }*/

    public void loadMoreMemes() {

        feedList.removeFooterView(footer);

        if (lastResponse != null) {
            GraphRequest newRequest = lastResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
            if (newRequest != null) {
                newRequest.setCallback(new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        lastResponse = graphResponse;
                        JSONObject mainObj = graphResponse.getJSONObject();
                        try {
                            JSONArray jsonArray = mainObj.getJSONArray("data");

                            for (int i = 0; i < 5; i++) {
                                JSONObject postObj = jsonArray.getJSONObject(i);
                                JSONObject fromObj = postObj.getJSONObject("from");
                                String creator = fromObj.getString("name");
                                String image_url = postObj.getString("full_picture");
                                String post_id = postObj.getString("id");
                                String created_at = postObj.getString("created_time");
                                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                                Date dates = date.parse(created_at);
                                cal.setTimeInMillis(dates.getTime());
                                String created_time = "" + cal.getTime();
                                tags = postObj.getString("message");

                                feedAdapter.add(new Post(post_id, image_url, creator,
                                        created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                        tags));
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "No More Memes", Toast.LENGTH_SHORT).show();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } finally {
                            feedAdapter.notifyDataSetChanged();
                            hidePDialog();
                        }
                    }
                });
                newRequest.executeAsync();
            }
        }
        feedList.addFooterView(footer);

    }

    @Override
    protected void onStop() {
        super.onStop();
        AppController.getInstance().cancelPendingRequests("MyTag");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
        AppController.getInstance().cancelPendingRequests("MyTag");
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
        } else if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press Again to Exit 27Memes", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

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
            postList.clear();
            lastResponse = null;
            next = null;
            feedList.removeFooterView(footer);
            request.executeAsync();

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
        } else if (id == R.id.nav_explore) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Explore Coming Soon...");
            builder.setCancelable(true);

            builder.setPositiveButton("I'm Waiting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else if (id == R.id.nav_categories) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Categories Coming Soon...");
            builder.setCancelable(true);

            builder.setPositiveButton("I'm Waiting", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else if (id == R.id.nav_signup) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Memeists Feature Coming Soon...");
            builder.setCancelable(true);

            builder.setPositiveButton("I'm Excited", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setMainUrl(String pageId) {
        mainUrl = "https:graph.facebook.com/v2.9/" + pageId + "/feed?fields=full_picture,from,created_time,message&limit=5&access_token=" + AccessToken.getCurrentAccessToken().getToken();
        Log.v("Main URL", mainUrl);
        Toast.makeText(getApplicationContext(), mainUrl, Toast.LENGTH_LONG).show();
    }

}
