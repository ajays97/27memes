package com.techurity.a27memes;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.techurity.a27memes.adapter.PageAdapter;
import com.techurity.a27memes.model.PagePost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TimelineActivity extends AppCompatActivity {

    ArrayList<String> page_names, page_ids;
    ArrayList<GraphResponse> responses = new ArrayList<>();

    ProgressDialog pDialog;
    Button loadMore;

    PageAdapter feedAdapter;
    String page;
    String main_message, post_image_url, tags, link, post_id, title;
    int page_index;
    Calendar cal;

    HashMap<String, GraphResponse> requestHashMap;

    GraphRequest listRequest;
    GraphRequest request = null;

    View footer, header;

    ListView timelineList;
    ListAdapter timelineAdapter;
    ArrayList<PagePost> postList = new ArrayList<PagePost>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        requestHashMap = new HashMap<>();

        timelineList = (ListView) findViewById(R.id.timelineList);
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
                Log.d("OUTPUT", mainObj.toString());
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
            Log.d("RESPONSE", lastResponse.toString());
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
        }else{
            Bundle parameters = new Bundle();
            parameters.putString("limit", "5");
            parameters.putString("fields", "full_picture,message,created_time,type,link");

            GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + page_ids.get(page_index) + "/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    requestHashMap.put(page_ids.get(page_index), response);
                    JSONObject mainObj = response.getJSONObject();

                    try {
                        Log.d("ABSENT", mainObj.toString());
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
}
