package com.techurity.a27memes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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
import com.techurity.a27memes.adapter.PageAdapter;
import com.techurity.a27memes.adapter.TimelineAdapter;
import com.techurity.a27memes.model.PagePost;
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

public class PageActivity extends AppCompatActivity {

    String page_id, title;

    private AdView mAdView;

    ListView feedList;
    PageAdapter feedAdapter;
    private ProgressDialog pDialog;
    GraphResponse lastResponse = null;
    ArrayList<PagePost> postList = new ArrayList<PagePost>();
    View footer, header;
    Button loadMore;

    Calendar cal;
    String tags, link = null;
    String next = null;
    String main_message = "Presented by: 27Memes";
    boolean page_check;

    String post_image_url, post_id;

    String image_url;
    File input_file;

    GraphRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        MobileAds.initialize(this, "ca-app-pub-2819514375619003~1342858770");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        page_id = intent.getStringExtra(Intent.EXTRA_TEXT);
        title = intent.getStringExtra("PAGE_TITLE");

        getSupportActionBar().setTitle(title);

        feedList = (ListView) findViewById(R.id.pageFeedList);
        feedAdapter = new PageAdapter(this, postList);
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
                    PagePost post = (PagePost) adapterView.getItemAtPosition(position);
                    String url = post.getImage_url();
                    String post_id = post.getPost_id();
                    String external_link = post.getLink();

                    if (external_link != "") {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
                        builder.setExitAnimations(PageActivity.this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(PageActivity.this, Uri.parse(external_link));
                    } else {

                        Intent intent = new Intent(PageActivity.this, TagActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, post_id);
                        intent.putExtra("IMAGE_URL", url);
                        startActivity(intent);
                    }
                } catch (NullPointerException e) {

                }
            }
        });

        updateFeed(page_id, true);

        registerForContextMenu(feedList);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.pageFeedList) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_long_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        PagePost post = (PagePost) feedList.getItemAtPosition(info.position);
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

        postList = new ArrayList<PagePost>();
        feedAdapter = new PageAdapter(getApplicationContext(), postList);
        feedList.setAdapter(feedAdapter);

        feedList.addFooterView(footer);
        cal = Calendar.getInstance();

        Bundle parameters = new Bundle();
        parameters.putString("limit", "5");
        parameters.putString("fields", "full_picture,message,created_time,type,link");

        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + page + "/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                lastResponse = response;
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
                                if (postObj.has("message"))
                                    main_message = postObj.getString("message");
                                String image_url = null;
                                if (postObj.has("full_picture"))
                                    image_url = postObj.getString("full_picture");

                                if (postObj.getString("type").equals("link")) {
                                    tags = "External Link: Click to Visit";
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

                        }
                    }
                });
                newRequest.executeAsync();
            }
        }
        feedList.addFooterView(footer);
        hidePDialog();

    }

    private void hidePDialog() {
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

    public class ImageDownloader extends AsyncTask<String, Integer, String> {

        ProgressDialog pd;
        InputStream is;
        OutputStream os;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(PageActivity.this);
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
                    return "Image Already Exists";
                }

                is.close();
                os.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Image Saved";

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
            startActivity(Intent.createChooser(sendIntent, "Share Image Via"));
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
