package com.techurity.a27memes;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//List Page ID = 1509884089083570

public class ExploreActivity extends AppCompatActivity {

    GraphRequest listRequest;
    ListView pageList;
    ArrayAdapter<String> pageAdapter;

    ArrayList<String> page_names, page_ids;

    ProgressDialog pDialog;

    @Override
    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Explore");
        pageList = (ListView) findViewById(R.id.pageList);
        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Explore");
        pDialog.setMessage("Fetching Explore List...");

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
                    Collections.shuffle(pages_list);
                    ArrayList<String> temp1 = new ArrayList<String>(), temp2 = new ArrayList<String>();
                    for(int i = 0; i< page_list.length; i++){
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

        hidePDialog();

        pageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ExploreActivity.this, PageActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, page_ids.get(i));
                intent.putExtra("PAGE_TITLE", page_names.get(i));
                startActivity(intent);
            }
        });

    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }

    }

    public void setPageDetails(ArrayList<String> names, ArrayList<String> ids){
        page_names = names;
        page_ids = ids;
        setListItems(page_names);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.explore_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pageAdapter.getFilter().filter(searchView.getQuery());
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

    public void setListItems(ArrayList<String> page_list) {
        pageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, page_list);
        pageList.setAdapter(pageAdapter);

    }
}
