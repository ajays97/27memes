package com.techurity.a27memes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.techurity.a27memes.adapter.TimelineAdapter;
import com.techurity.a27memes.model.Post;

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

public class CategoryMemes extends AppCompatActivity {

    String title, mainUrl, page_id;

    private AdView mAdView;

    ListView feedList;
    TimelineAdapter feedAdapter;
    private ProgressDialog pDialog;
    GraphResponse lastResponse = null;
    ArrayList<Post> postList = new ArrayList<Post>();
    View footer, header;
    Button loadMore;

    Calendar cal;
    String tags;
    String next = null;
    String main_message;
    boolean page_check;

    String image_url;
    File input_file;

    GraphRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_memes);
        MobileAds.initialize(this, "ca-app-pub-2819514375619003~1342858770");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        page_id = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        switch (page_id) {

            case "1305194836265253":
                title = "Programmer";
                break;
            case "1361941223896408":
                title = "Teen";
                break;
            case "1246010548857961":
                title = "Boys Vs Girls";
                break;
            case "133914500500747":
                title = "Boys";
                break;
            case "1548599595182486":
                title = "Girls";
                break;
            case "814916718680335":
                title = "Adult/18+";
                break;
            case "445829182452540":
                title = "Kids";
                break;
            case "":
                title = "Coming Soon";
        }

        getSupportActionBar().setTitle(title);

        feedList = (ListView) findViewById(R.id.catFeedList);
        feedAdapter = new TimelineAdapter(this, postList);
        LayoutInflater inflaterer = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        header = inflaterer.inflate(R.layout.header_margin, null);

        feedList.setAdapter(feedAdapter);
        feedList.addHeaderView(header);


        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();


        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer = inflater.inflate(R.layout.feed_footer, null);
        loadMore = (Button) footer.findViewById(R.id.load_more);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMoreMemes(page_check);
            }
        });

        feedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                try {
                    Post post = (Post) adapterView.getItemAtPosition(position);
                    String url = post.getImage_url();

                    Intent intent = new Intent(CategoryMemes.this, ViewImageActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    startActivity(intent);
                } catch (NullPointerException e) {

                }
            }
        });



        mAdView = (AdView) findViewById(R.id.catAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        updateFeed(page_id, true);
        registerForContextMenu(feedList);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.catFeedList) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_main, menu);
        return super.onCreateOptionsMenu(menu);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }else if (id == R.id.cRefresh){
            postList.clear();
            lastResponse = null;
            next = null;
            feedList.removeFooterView(footer);
            updateFeed(page_id, true);
        }

        return super.onOptionsItemSelected(item);
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    public class ImageDownloader extends AsyncTask<String, Integer, String> {

        ProgressDialog pd;
        InputStream is;
        OutputStream os;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(CategoryMemes.this);
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
