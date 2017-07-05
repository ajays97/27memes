package com.techurity.a27memes;

import android.app.ProgressDialog;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    ArrayList<String> page_names, page_ids;
    ArrayList<GraphResponse> responses = new ArrayList<>();

    ProgressDialog pDialog;

    String page;

    HashMap<String, GraphResponse> requestHashMap;

    GraphRequest listRequest;
    GraphRequest request = null;

    ListView timelineList;
    ListAdapter timelineAdapter;
    ArrayList<String> mainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);



        requestHashMap = new HashMap<>();

        timelineList = (ListView) findViewById(R.id.timelineList);

        mainList = new ArrayList<String>();

        loadList();



    }

    public void loadList(){

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

    public void updateFeed(ArrayList<String> page_list, ArrayList<String> page_id_list) {

        Bundle parameters = new Bundle();
        parameters.putString("limit", "1");
        parameters.putString("fields", "full_picture,message,created_time,type,link");

        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Timeline");
        pDialog.setMessage("Loading fun...");
        pDialog.show();

        for(int i = 0; i< page_list.size(); i++){

            page = page_id_list.get(i);
            request = new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + page + "/feed", parameters, HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    responses.add(response);
                }
            });

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            request.executeAndWait();

        }
        pDialog.dismiss();
        Log.d("RESPONSES", responses.toString());
    }

    public void setPageDetails(ArrayList<String> names, ArrayList<String> ids) {
        page_names = names;
        page_ids = ids;

        timelineAdapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, page_names);
        timelineList.setAdapter(timelineAdapter);



        updateFeed(page_names, page_ids);


    }
}
