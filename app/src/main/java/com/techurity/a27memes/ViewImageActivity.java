package com.techurity.a27memes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.techurity.a27memes.app.AppController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Manifest;

public class ViewImageActivity extends AppCompatActivity {

    NetworkImageView image;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    LinearLayout layout;
    String image_url;
    ImageRequest request;
    PhotoViewAttacher attacher;

    File input_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_view_image);

        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        bar.setTitle(null);

        layout = (LinearLayout) findViewById(R.id.linearlayout);
        layout.setFitsSystemWindows(true);

        Intent i = getIntent();
        image_url = i.getStringExtra(Intent.EXTRA_TEXT);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Image...");
        progressDialog.show();

        image = (NetworkImageView) findViewById(R.id.view_image);
        image.setDrawingCacheEnabled(true);
        image.setFitsSystemWindows(true);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        image.setImageUrl(image_url, imageLoader);
        attacher = new PhotoViewAttacher(image);

        attacher.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                fullScreen();
            }
        });

        progressDialog.dismiss();

    }

    public void fullScreen() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("FullScreen", "Turning immersive mode mode off. ");
        } else {
            Log.i("FullScreen", "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.mShare) {
            new ShareCreator().execute(image_url);

        } else if (item.getItemId() == R.id.mDownload) {

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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case 200:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new ImageDownloader().execute(image_url);
                } else {

                }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public class ImageDownloader extends AsyncTask<String, Integer, String> {

        ProgressDialog pd;
        InputStream is;
        OutputStream os;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(ViewImageActivity.this);
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
                        Log.d("File Present", "" + input_file.exists());
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
