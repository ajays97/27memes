package com.techurity.a27memes.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.techurity.a27memes.CategoryMemes;
import com.techurity.a27memes.R;
import com.techurity.a27memes.adapter.cMemesAdapter;

import java.util.ArrayList;

/**
 * Created by Ajay Srinivas on 5/18/2017.
 */

public class MemesFragment extends Fragment {

    ListView catList;
    private InterstitialAd interstitialAd;

    String[] pages = {
            "1305194836265253",
            "1361941223896408",
            "1246010548857961",
            "133914500500747",
            "1548599595182486",
            "814916718680335",
            "445829182452540",
            ""
    };

    public MemesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        interstitialAd = new InterstitialAd(getContext());
        interstitialAd.setAdUnitId("ca-app-pub-2819514375619003/5816647177");
        Log.d("Ad Loading", "Loaded");
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("F46B367C9316B954ABD72A81A27387F0").build();
        interstitialAd.loadAd(request);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_memes, container, false);

        catList = (ListView) rootView.findViewById(R.id.catList);
        catList.setAdapter(new cMemesAdapter(getActivity()));

        catList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (interstitialAd.isLoaded())
                    interstitialAd.show();

                if (!(i == pages.length - 1)) {
                    Intent intent = new Intent(getActivity(), CategoryMemes.class);
                    intent.putExtra(Intent.EXTRA_TEXT, pages[i]);
                    startActivity(intent);
                }else{
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "the27memes@gmail.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Category Suggestion");
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getActivity(), "No Compatible App", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return rootView;
    }
}
