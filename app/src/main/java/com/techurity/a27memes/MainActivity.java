package com.techurity.a27memes;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v4.widget.SwipeRefreshLayout;

import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.view.Window;
import android.view.WindowManager;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.techurity.a27memes.adapter.TimelineAdapter;
import com.techurity.a27memes.app.AppController;
import com.techurity.a27memes.model.Post;
import com.techurity.a27memes.receiver.NotificationReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


//Page ID = 1713086835593817
//Runtime ID = 133352060545254
//App Page = 414941838890340
//Notification Page ID = 429615974063759

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int JOB_ID = 1;
    String mainUrl = "https:graph.facebook.com/v2.9/1713086835593817/feed?fields=full_picture,from,created_time,message&limit=5&access_token=" + AccessToken.getCurrentAccessToken().getToken();

    JsonObjectRequest objectRequest2 = null;
    private AdView mAdView;

    private PendingIntent pendingIntent;

    boolean doubleBackToExitPressedOnce = false;

    String image_url;

    View footer, header;
    Button loadMore;

    GraphRequest request;

    File input_file;

    Calendar cal;
    String tags;

    ListView feedList;
    TimelineAdapter feedAdapter;
    private ProgressDialog pDialog;
    GraphResponse lastResponse = null;
    ArrayList<Post> postList = new ArrayList<Post>();

    JSONObject nextObj;

    String next = null;
    String main_message;
    boolean page_check;

    SwipeRefreshLayout refreshLayout;
    SwipeRefreshLayout.OnRefreshListener refreshListener;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MobileAds.initialize(this, "ca-app-pub-2819514375619003~1342858770");

        feedList = (ListView) findViewById(R.id.feedList);
        feedAdapter = new TimelineAdapter(this, postList);
        LayoutInflater inflaterer = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        header = inflaterer.inflate(R.layout.header_margin, null);

        feedList.setAdapter(feedAdapter);
        feedList.addHeaderView(header);

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
                    if (page.equals("414941838890340")) {
                        updateFeed(page, true);
                        //updateFeed(mainUrl, true);
                    } else
                        updateFeed(page, false);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                    pDialog.hide();
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
                loadMoreMemes(page_check);
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

                try {
                    Post post = (Post) adapterView.getItemAtPosition(position);
                    String url = post.getImage_url();

                    Intent intent = new Intent(MainActivity.this, ViewImageActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    startActivity(intent);
                } catch (NullPointerException e) {

                }
            }
        });

        registerForContextMenu(feedList);


        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        /* Retrieve a PendingIntent that will perform a broadcast */


//        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        int interval = 8000;
//
//        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

        //To cancel the alarm
//        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        manager.cancel(pendingIntent);
//        Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();

        boolean alarmUp = (PendingIntent.getBroadcast(this, 0,
                new Intent(MainActivity.this, NotificationReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp)
            startAlarm();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.feedList) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_long_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Post post = (Post) feedList.getItemAtPosition(info.position);
        image_url = post.getImage_url();

        switch (item.getItemId()) {
            case R.id.mSave:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new ImageDownloader().execute(image_url);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
                    }

                } else {
                    new ImageDownloader().execute(image_url);
                }
                return true;

            case R.id.mLongShare:
                new ShareCreator().execute(image_url);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    public void startAlarm() {

        Intent alarmIntent = new Intent(MainActivity.this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 60);
        long interval = 60 * 1000 * 30;

        Log.d("Setting Alarm Main", "" + System.nanoTime());
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                interval, pendingIntent);
    }

    public void updateFeed(String page, boolean check) {

        page_check = check;

        postList = new ArrayList<Post>();
        feedAdapter = new TimelineAdapter(getApplicationContext(), postList);
        feedList.setAdapter(feedAdapter);

        feedList.addFooterView(footer);
        cal = Calendar.getInstance();

        Bundle parameters = new Bundle();
        parameters.putString("limit", "5");
        parameters.putString("fields", "full_picture,message,created_time");

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + page + "/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                lastResponse = response;
                JSONObject mainObj = response.getJSONObject();
                try {
                    JSONArray jsonArray = mainObj.getJSONArray("data");
                    for (int i = 0; i < 5; i++) {
                        JSONObject postObj = jsonArray.getJSONObject(i);
                        main_message = postObj.getString("message");
                        String creator = main_message.substring(0, main_message.indexOf("Mes"));
                        String message = main_message.substring(main_message.indexOf("Message:") + 8, main_message.indexOf("Tags:"));
                        String tags = main_message.substring(main_message.indexOf("Tags:") + 5, main_message.length());
                        String image_url = postObj.getString("full_picture");
                        String post_id = postObj.getString("id");
                        String created_at = postObj.getString("created_time");
                        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                        Date dates = date.parse(created_at);
                        cal.setTimeInMillis(dates.getTime());
                        String created_time = "" + cal.getTime();

                        feedAdapter.add(new Post(post_id, image_url, creator,
                                created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                message, tags));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                } finally {
                    feedAdapter.notifyDataSetChanged();
                    hidePDialog();
                }

            }
        });
        request.executeAsync();

    }


    public void loadMoreMemes(boolean check) {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching More Memes...");
        pDialog.show();
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
                                main_message = postObj.getString("message");
                                String creator = main_message.substring(0, main_message.indexOf("Mes"));
                                String message = main_message.substring(main_message.indexOf("Message:") + 8, main_message.indexOf("Tags:"));
                                String tags = main_message.substring(main_message.indexOf("Tags:") + 5, main_message.length());
                                String image_url = postObj.getString("full_picture");
                                String post_id = postObj.getString("id");
                                String created_at = postObj.getString("created_time");
                                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                                Date dates = date.parse(created_at);
                                cal.setTimeInMillis(dates.getTime());
                                String created_time = "" + cal.getTime();

                                feedAdapter.add(new Post(post_id, image_url, creator,
                                        created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                        message, tags));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "No More Memes", Toast.LENGTH_SHORT).show();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_SHORT).show();
                        } finally {
                            feedAdapter.notifyDataSetChanged();

                        }
                    }
                });
                newRequest.executeAsync();
            }
        }
        feedList.addFooterView(footer);
        hidePDialog();

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
            updateFeed("414941838890340", true);

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

        if (id == R.id.nav_logout) {
            LoginManager.getInstance().logOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_explore) {
//            startActivity(new Intent(MainActivity.this, ExploreActivity.class));

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Explore Coming Soon...");
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "I'm Waiting",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();

        } else if (id == R.id.nav_categories) {
            startActivity(new Intent(MainActivity.this, CategoriesMain.class));
        } else if (id == R.id.nav_signup) {

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "the27memes@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Become a Memeist - Get featured on 27Memes.");
                intent.putExtra(Intent.EXTRA_TEXT, "I have attached my meme to go on 27Memes.\nName:\nMessage:\nTags:");
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "No Compatible App", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_likeus) {
            try {
                getPackageManager().getPackageInfo("com.facebook.katana", 0);
                Intent fb = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1713086835593817"));
                startActivity(fb);
            } catch (Exception e) {
                Intent fb = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/27memes"));
                startActivity(fb);
            }
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class ImageDownloader extends AsyncTask<String, Integer, String> {

        ProgressDialog pd;
        InputStream is;
        OutputStream os;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("Downloading Meme...");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(100);
            pd.setProgress(0);
            pd.show();

        }

        @Override
        protected void onPostExecute(String result) {
            pd.dismiss();
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pd.setProgress(values[0]);
        }

        @Override
        protected String doInBackground(String... params) {

            String path = params[0];

            String file_name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf(".jpg"));

            int file_length = 0;
            try {
                URL url = new URL(path);
                URLConnection connection = url.openConnection();
                connection.connect();
                file_length = connection.getContentLength();
                File app_folder = new File("sdcard/DCIM/27Memes");

                if (!app_folder.exists())
                    app_folder.mkdir();

                input_file = new File(app_folder, file_name + ".jpg");

                if (!input_file.exists()) {
                    is = new BufferedInputStream(url.openStream(), 8192);
                    byte[] data = new byte[1024];
                    int total = 0;
                    int count = 0;
                    os = new FileOutputStream(input_file);
                    while ((count = is.read(data)) != -1) {
                        total += count;
                        os.write(data, 0, count);
                        int progress = (int) total * 100 / file_length;
                        publishProgress(progress);
                    }
                } else {
                    return "MEME Already Exists";
                }

                is.close();
                os.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "MEME Saved";

        }
    }

    public class ShareCreator extends AsyncTask<String, Integer, String> {

        ProgressDialog pd;
        InputStream is;
        OutputStream os;

        @Override
        protected void onPostExecute(String s) {
            String file_name = image_url.substring(image_url.lastIndexOf('/') + 1, image_url.lastIndexOf(".jpg") + 4);
            Uri picUri = Uri.parse("sdcard/DCIM/27Memes/" + file_name);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, picUri);
            sendIntent.setType("image/jpg");
            startActivity(Intent.createChooser(sendIntent, "Share MEME Via"));
        }

        @Override
        protected String doInBackground(String... params) {

            String path = params[0];

            String file_name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf(".jpg"));

            int file_length = 0;
            try {
                URL url = new URL(path);
                URLConnection connection = url.openConnection();
                connection.connect();
                file_length = connection.getContentLength();
                File app_folder = new File("sdcard/DCIM/27Memes");

                if (!app_folder.exists())
                    app_folder.mkdir();

                input_file = new File(app_folder, file_name + ".jpg");

                if (!input_file.exists()) {
                    is = new BufferedInputStream(url.openStream(), 8192);
                    byte[] data = new byte[1024];
                    int total = 0;
                    int count = 0;
                    os = new FileOutputStream(input_file);
                    while ((count = is.read(data)) != -1) {
                        total += count;
                        os.write(data, 0, count);
                        int progress = (int) total * 100 / file_length;
                        publishProgress(progress);
                    }
                } else {
                    return null;
                }

                is.close();
                os.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

    }
}
