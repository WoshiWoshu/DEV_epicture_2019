package com.example.mainactivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ActionMenuView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mainactivity.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.journaldev.searchview.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private OkHttpClient httpClient;
    private Request request;



    /*private static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title;

        public PhotoVH(View itemView) {
            super(itemView);
        }
    }

    public void render(final List<Photo> photos) {
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_of_photos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.item, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                return vh;
            }

            @Override
            public void onBindViewHolder(PhotoVH holder, int position) {
                Picasso.with(MainActivity.this).load("https://i.imgur.com/" +
                        photos.get(position).id + ".jpg").into(holder.photo);
                holder.title.setText(photos.get(position).title);
            }

            @Override
            public int getItemCount() {
                return photos.size();
            }
        };
        rv.setAdapter(adapter);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 16;
            }
        });
    }*/

    public void createRequest() {
        request = new Request.Builder()
                .url("https://api.imgur.com/3/gallery/hot/viral/0.json")
                .header("Authorization", "Client-ID fce6aa2483ed94b")
                .header("User-Agent", "E-picture")
                .build();
    }

    private void fetchData(final ActivityMainBinding activityMainBinding, final List<Photo> photos) {
        httpClient = new OkHttpClient.Builder().build();
        createRequest();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "An error has occurred " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject data = null;
                try {
                    data = new JSONObject(response.body().string());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray items = null;
                try {
                    items = data.getJSONArray("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = null;
                    try {
                        item = items.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Photo photo = new Photo();
                    try {
                        if (item.getBoolean("is_album")) {
                            photo.id = item.getString("cover");
                        } else {
                            photo.id = item.getString("id");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        photo.title = item.getString("title");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    photos.add(photo);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createSearchViewInterfaces(activityMainBinding, photos);
                    }
                });
            }
        });
    }

    public void createSearchViewInterfaces(ActivityMainBinding activityMainBinding,
                                           final List<Photo> photos) {
        final ListAdapter adapter = new ListAdapter(photos);

        activityMainBinding.listView.setAdapter(adapter);
        activityMainBinding.search.setActivated(true);
        activityMainBinding.search.setQueryHint("Type your keyword here");
        activityMainBinding.search.onActionViewExpanded();
        activityMainBinding.search.setIconified(false);
        activityMainBinding.search.clearFocus();
        activityMainBinding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void splitUrl(String url, WebView view) {
        String[] outerSplit = url.split("\\#")[1].split("\\&");
        String username = null;
        String accessToken = null;
        String refreshToken = null;

        int index = 0;

        for (String s : outerSplit) {
            String[] innerSplit = s.split("\\=");

            switch (index) {
                // Access Token
                case 0:
                    accessToken = innerSplit[1];
                    break;

                // Refresh Token
                case 3:
                    refreshToken = innerSplit[1];
                    break;

                // Username
                case 4:
                    username = innerSplit[1];
                    break;
                default:

            }
            index++;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ActivityMainBinding activityMainBinding;
        super.onCreate(savedInstanceState);
        final List<Photo> photos = new ArrayList<Photo>();
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        final WebView mainLayout=(WebView)this.findViewById(R.id.webview);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard/*, R.id.navigation_notifications*/)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        fetchData(activityMainBinding, photos);
        WebView imgurWebView = (WebView) findViewById(R.id.webview);
        imgurWebView.setBackgroundColor(Color.TRANSPARENT);
        imgurWebView.loadUrl("https://api.imgur.com/oauth2/authorize?client_id=fce6aa2483ed94b&response_type=token&state=APPLICATION_STATE");
        imgurWebView.getSettings().setJavaScriptEnabled(true);
        imgurWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("https://api.imgur.com/gallery")) {
                    splitUrl(request.getUrl().toString(), view);
                } else {
                    view.loadUrl("https://api.imgur.com/gallery");
                }
                mainLayout.setVisibility(RelativeLayout.GONE);
                return true;
            }

        });

    }
}
