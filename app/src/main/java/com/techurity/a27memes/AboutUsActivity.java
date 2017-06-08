package com.techurity.a27memes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class AboutUsActivity extends AppCompatActivity implements RewardedVideoAdListener{

    Button watchAd;
    private RewardedVideoAd mAd;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        MobileAds.initialize(this, "ca-app-pub-2819514375619003~1342858770");

        watchAd = (Button) findViewById(R.id.supportBtn);
        adView = (AdView) findViewById(R.id.supportAd);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        mAd.loadAd("ca-app-pub-2819514375619003/2128150773", new AdRequest.Builder().build());

        watchAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAd.isLoaded())
                    mAd.show();
            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    // Required to reward the user.
    @Override
    public void onRewarded(RewardItem reward) {
        // Reward the user.
    }

    // The following listener methods are optional.
    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        Toast.makeText(this, "Thank you for your support", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        watchAd.setText("Could not load video");
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        watchAd.setText("Watch A Video");
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    protected void onResume() {
        mAd.resume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mAd.pause(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mAd.destroy(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
