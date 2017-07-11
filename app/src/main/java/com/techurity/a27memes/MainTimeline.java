package com.techurity.a27memes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.techurity.a27memes.adapter.PageAdapter;
import com.techurity.a27memes.app.AppController;
import com.techurity.a27memes.model.PagePost;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class MainTimeline extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<String> page_names, page_ids;
    ArrayList<GraphResponse> responses = new ArrayList<>();

    ProgressDialog pDialog;
    Button loadMore;

    PageAdapter feedAdapter;
    String page;
    String image_url;
    String main_message, post_image_url, tags, link, post_id, title;
    int page_index;
    Calendar cal;
    File input_file;

    HashMap<String, GraphResponse> requestHashMap;
    private AdView mAdView;

    private PendingIntent pendingIntent;

    boolean doubleBackToExitPressedOnce = false;

    GraphRequest listRequest;
    GraphRequest request = null;

    View footer, header;

    ListView timelineList;
    ListAdapter timelineAdapter;
    ArrayList<PagePost> postList = new ArrayList<PagePost>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        requestHashMap = new HashMap<>();

        timelineList = (ListView) findViewById(R.id.feedList);
        postList = new ArrayList<PagePost>();
        feedAdapter = new PageAdapter(this, postList);

        LayoutInflater inflaterer = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        header = inflaterer.inflate(R.layout.header_margin, null);
        footer = inflaterer.inflate(R.layout.feed_footer, null);
        loadMore = (Button) footer.findViewById(R.id.load_more);

        timelineList.setAdapter(feedAdapter);
        timelineList.addHeaderView(header);

        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreMemes();
            }
        });

        loadList();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        timelineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                try {
                    PagePost post = (PagePost) adapterView.getItemAtPosition(position);
                    String url = post.getImage_url();
                    String post_id = post.getPost_id();
                    String external_link = post.getLink();

                    if (external_link != "") {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                        builder.setExitAnimations(MainTimeline.this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(MainTimeline.this, Uri.parse(external_link));
                    } else {
                        Intent intent = new Intent(MainTimeline.this, ViewImageActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, url);
                        startActivity(intent);
                    }
                } catch (NullPointerException e) {

                }
            }
        });

        registerForContextMenu(timelineList);


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
                new Intent(MainTimeline.this, NotificationReceiver.class),
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
        PagePost post = (PagePost) timelineList.getItemAtPosition(info.position);
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

        Intent alarmIntent = new Intent(MainTimeline.this, NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainTimeline.this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 60);
        long interval = 60 * 1000 * 30;

        Log.d("Setting Alarm Main", "" + System.nanoTime());
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                interval, pendingIntent);
    }

    public void loadList() {

        Bundle params = new Bundle();
        params.putString("limit", "1");
        params.putString("fields", "message");
        listRequest = new GraphRequest(AccessToken.getCurrentAccessToken(), "/1509884089083570/posts", params, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject mainObj = response.getJSONObject();
                try {
                    JSONObject dataObj = mainObj.getJSONArray("data").getJSONObject(0);
                    String[] page_list = dataObj.getString("message").split("\n");
                    ArrayList<String> pages_list = new ArrayList<String>(Arrays.asList(page_list));
                    ArrayList<String> temp1 = new ArrayList<String>(), temp2 = new ArrayList<String>();
                    for (int i = 0; i < page_list.length; i++) {
                        temp1.add(pages_list.get(i).split("-")[0]);
                        temp2.add(pages_list.get(i).split("-")[1]);
                    }
                    setPageDetails(temp1, temp2);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        listRequest.executeAsync();

    }

    public void updateFeed(final ArrayList<String> page_list, ArrayList<String> page_id_list) {

        cal = Calendar.getInstance();
        Bundle parameters = new Bundle();
        parameters.putString("limit", "5");
        parameters.putString("fields", "full_picture,message,created_time,type,link");

        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Timeline");
        pDialog.setMessage("Loading fun...");
        pDialog.show();

        page_index = new Random().nextInt(page_id_list.size());

        page = page_id_list.get(page_index);

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + page + "/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                requestHashMap.put(page, response);

                title = page_list.get(page_index);
                JSONObject mainObj = response.getJSONObject();
                try {
                    JSONArray jsonArray = mainObj.getJSONArray("data");
                    for (int i = 0; i < 5; i++) {
                        JSONObject postObj = jsonArray.getJSONObject(i);

                        if (postObj.has("message"))
                            main_message = postObj.getString("message");
                        else
                            main_message = "Presented By 27Memes";

                        if (postObj.has("full_picture"))
                            post_image_url = postObj.getString("full_picture");

                        if (postObj.getString("type").equals("link")) {
                            tags = "External Link: Click to Visit";
                            main_message = main_message + "\nClick to read full article.";
                            link = postObj.getString("link");
                        } else if (postObj.getString("type").equals("video")) {
                            tags = "Click to Play Video";
                            link = postObj.getString("link");
                        } else {
                            tags = "";
                            link = "";
                        }
                        post_id = postObj.getString("id");
                        String created_at = "";
                        if (postObj.has("created_time"))
                            created_at = postObj.getString("created_time");
                        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                        Date dates = date.parse(created_at);
                        cal.setTimeInMillis(dates.getTime());
                        String created_time = "" + cal.getTime();

                        feedAdapter.add(new PagePost(post_id, post_image_url, title,
                                created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                main_message, tags, link));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.d("ERROR", e.getMessage());
                    Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                } finally {
                    feedAdapter.notifyDataSetChanged();
                    hidePDialog();
                }

            }
        });
        request.executeAsync();

        pDialog.dismiss();
        timelineList.addFooterView(footer);
    }

    public void loadMoreMemes() {


        timelineList.removeFooterView(footer);

        page_index = new Random().nextInt(page_ids.size());
        GraphResponse lastResponse = requestHashMap.get(page_ids.get(page_index));

        title = page_names.get(page_index);

        if (lastResponse != null) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Fetching More Memes...");
            pDialog.show();

            GraphRequest newRequest = lastResponse.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
            if (newRequest != null) {
                newRequest.setCallback(new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        requestHashMap.put(page_ids.get(page_index), graphResponse);
                        JSONObject mainObj = graphResponse.getJSONObject();
                        try {
                            JSONArray jsonArray = mainObj.getJSONArray("data");

                            for (int i = 0; i < 5; i++) {
                                JSONObject postObj = jsonArray.getJSONObject(i);
                                if (postObj.has("message"))
                                    main_message = postObj.getString("message");
                                else
                                    main_message = "Presented By 27Memes.";
                                String image_url = null;
                                if (postObj.has("full_picture"))
                                    image_url = postObj.getString("full_picture");

                                if (postObj.getString("type").equals("link")) {
                                    tags = "External Link: Click to Visit";
                                    main_message = main_message + "\nClick to read full article.";
                                    link = postObj.getString("link");
                                } else if (postObj.getString("type").equals("video")) {
                                    tags = "Click to Play Video";
                                    link = postObj.getString("link");
                                } else {
                                    tags = "";
                                    link = "";
                                }
                                String post_id = postObj.getString("id");

                                String created_at = "";
                                if (postObj.has("created_time"))
                                    created_at = postObj.getString("created_time");
                                SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                                Date dates = date.parse(created_at);
                                cal.setTimeInMillis(dates.getTime());
                                String created_time = "" + cal.getTime();

                                feedAdapter.add(new PagePost(post_id, image_url, title,
                                        created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                        main_message, tags, link));
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
                            hidePDialog();
                        }
                    }
                });
                newRequest.executeAsync();
            }
        } else {
            Bundle parameters = new Bundle();
            parameters.putString("limit", "5");
            parameters.putString("fields", "full_picture,message,created_time,type,link");

            GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + page_ids.get(page_index) + "/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    requestHashMap.put(page_ids.get(page_index), response);
                    JSONObject mainObj = response.getJSONObject();

                    try {
                        JSONArray jsonArray = mainObj.getJSONArray("data");
                        for (int i = 0; i < 5; i++) {
                            JSONObject postObj = jsonArray.getJSONObject(i);

                            if (postObj.has("message"))
                                main_message = postObj.getString("message");
                            else
                                main_message = "Presented By 27Memes";

                            if (postObj.has("full_picture"))
                                post_image_url = postObj.getString("full_picture");

                            if (postObj.getString("type").equals("link")) {
                                tags = "External Link: Click to Visit";
                                main_message = main_message + "\nClick to read full article.";
                                link = postObj.getString("link");
                            } else if (postObj.getString("type").equals("video")) {
                                tags = "Click to Play Video";
                                link = postObj.getString("link");
                            } else {
                                tags = "";
                                link = "";
                            }
                            post_id = postObj.getString("id");
                            String created_at = "";
                            if (postObj.has("created_time"))
                                created_at = postObj.getString("created_time");
                            SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
                            Date dates = date.parse(created_at);
                            cal.setTimeInMillis(dates.getTime());
                            String created_time = "" + cal.getTime();

                            feedAdapter.add(new PagePost(post_id, post_image_url, title,
                                    created_time.substring(0, created_time.indexOf(':') + 3) + created_time.substring(created_time.lastIndexOf(' '), created_time.lastIndexOf(' ') + 5),
                                    main_message, tags, link));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        Log.d("ERROR", e.getMessage());
                        Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                    } finally {
                        feedAdapter.notifyDataSetChanged();
                        hidePDialog();
                    }

                }
            });
            request.executeAsync();
        }


        timelineList.addFooterView(footer);

    }

    public void setPageDetails(ArrayList<String> names, ArrayList<String> ids) {
        page_names = names;
        page_ids = ids;

        updateFeed(page_names, page_ids);
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
            timelineList.removeFooterView(footer);
            updateFeed(page_names, page_ids);

        } else if (id == R.id.mLogout) {
            LoginManager.getInstance().logOut();
            startActivity(new Intent(MainTimeline.this, LoginActivity.class));
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
            startActivity(new Intent(MainTimeline.this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_explore) {
            startActivity(new Intent(MainTimeline.this, ExploreActivity.class));

       /*     AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
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
            alert11.show();*/

        } else if (id == R.id.nav_categories) {
            startActivity(new Intent(MainTimeline.this, CategoriesMain.class));
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
            startActivity(new Intent(MainTimeline.this, AboutUsActivity.class));
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

            pd = new ProgressDialog(MainTimeline.this);
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
}
