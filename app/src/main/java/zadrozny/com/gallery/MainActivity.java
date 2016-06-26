package zadrozny.com.gallery;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = MainActivity.class.getSimpleName();

    public final static String EXTRA_IMAGE = "zadrozny.com.gallery.IMAGE";

    public final static String FLICKR_KEY = "be7d5dea47ca7ab73017513fa63d9c92";
    public final static String ENDPOINT = "https://api.flickr.com/services/rest/?method=";
    public final static String FLICKR_SMALL = "url_t";
    public final static String FLICKR_LARGE = "url_z";

    ProgressDialog pDialog;
    RecyclerView recyclerView;
    GridGalleryAdapter gridAdapter;
    StaggeredGalleryAdapter staggeredAdapter;
    ListGalleryAdapter listAdapter;

    ArrayList<Uri>smallImages = new ArrayList<>();
    ArrayList<Uri>largeImages = new ArrayList<>();
    public ArrayList<String>titles = new ArrayList<>();
    DisplayMetrics metrics = new DisplayMetrics();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        pDialog = new ProgressDialog(this);

        gridAdapter = new GridGalleryAdapter(getApplicationContext(), smallImages, this);
        staggeredAdapter = new StaggeredGalleryAdapter(getApplicationContext(), smallImages, this);
        listAdapter = new ListGalleryAdapter(getApplicationContext(), smallImages, this);

        setGridLayout();
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fetchLocal();
    }

    void fetchLocal () {
        smallImages.clear();
        largeImages.clear();
        titles.clear();
        gridAdapter.notifyDataSetChanged();
        staggeredAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetChanged();

        if(Build.VERSION.SDK_INT >= 23) {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }

        pDialog.setMessage("Scanning directory...");
        pDialog.show();

        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File[] files = directory.listFiles(new ImageFilter());

        pDialog.hide();

        if(files == null || files.length == 0) return;

        for(File file : files) {
            smallImages.add(Uri.fromFile(file));
            largeImages.add(Uri.fromFile(file));
            titles.add(file.getName());
        }

        gridAdapter.notifyDataSetChanged();
        staggeredAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetChanged();
    }

    void fetchFlickr () {
        smallImages.clear();
        largeImages.clear();
        titles.clear();
        gridAdapter.notifyDataSetChanged();
        staggeredAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetChanged();

        pDialog.setMessage("Downloading data...");
        pDialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                CreateFlickrURL("flickr.photos.getRecent", "json", "100", FLICKR_SMALL + "," + FLICKR_LARGE), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.hide();
                        try {
                            JSONObject object = response.getJSONObject("photos");
                            if(!object.has("photo")) return;
                            JSONArray photos = object.getJSONArray("photo");
                            if(photos == null || photos.length() == 0) return;
                            for(int i = 0; i < photos.length(); i++) {
                                JSONObject photo = photos.getJSONObject(i);
                                if(photo.has(FLICKR_SMALL) && photo.has(FLICKR_LARGE)) {
                                    String urlSmall = photo.getString(FLICKR_SMALL);
                                    String urlLarge = photo.getString(FLICKR_LARGE);
                                    Uri uriSmall = Uri.parse(urlSmall);
                                    smallImages.add(uriSmall);
                                    largeImages.add(Uri.parse(urlLarge));
                                    String title = (new File(uriSmall.getPath())).getName();
                                    if(photo.has("title") && !photo.getString("title").trim().isEmpty()) title = photo.getString("title");
                                    titles.add(title);
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Json parsing error: " + e.getMessage());
                        }
                        gridAdapter.notifyDataSetChanged();
                        staggeredAdapter.notifyDataSetChanged();
                        listAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error: " + error.getMessage());
                        pDialog.hide();
                    }
                });
        AppController.getInstance().addToRequestQueue(req);
    }

    public class ImageFilter implements FileFilter {
        private String[] extensions = {".jpg", ".jpeg", ".png"};
        @Override
        public boolean accept (File file) {
            for(String ext : extensions) if(file.getName().toLowerCase().endsWith(ext)) return true;
            return false;
        }
    }

    private static String CreateFlickrURL (final String method, final String format,
                                           final String perPage, final String extras) {
        return ENDPOINT + method + "&api_key=" + FLICKR_KEY + "&format=" + format +
                "&per_page=" + perPage + "&extras=" + extras + "&nojsoncallback=1";
    }

    public void click (int position, View v) {
        Intent intent = new Intent(this, DisplayFullscreen.class);
        intent.putExtra(EXTRA_IMAGE, largeImages.get(position).toString());
        if(Build.VERSION.SDK_INT >= 16) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, v, "image");
            startActivity(intent, options.toBundle());
        }
        else startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.local:
                fetchLocal();
                return true;
            case R.id.flickr:
                fetchFlickr();
                return true;
            case R.id.grid:
                setGridLayout();
                return true;
            case R.id.staggered:
                setStaggeredGridLayout();
                return true;
            case R.id.list:
                setLinearLayout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setStaggeredGridLayout () {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(staggeredAdapter);
    }

    void setGridLayout () {
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        recyclerView.setAdapter(gridAdapter);
    }

    void setLinearLayout () {
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocal();
                } else {
                    fetchFlickr();
                }
                return;
            }
        }
    }

}