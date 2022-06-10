package com.codepath.apps.restclienttemplate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeContainer;

    private EndlessRecyclerViewScrollListener scrollListener;

    public static final String TAG = "TimelineActivity";

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // lookup swipe container view
        swipeContainer = findViewById(R.id.swipeContainer);

        // setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> fetchTimelineAsync(0));

        client = TwitterApp.getRestClient(this);

        // Find the RecyclerView
        rvTweets = findViewById(R.id.rvTweets);

        // initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        // RecyclerView setup: layout manager and adapter
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(adapter);

        // for infinite scroll
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        rvTweets.addOnScrollListener(scrollListener);

        // temp comment out for inf scroll
         populateHomeTimeline();
    }

    public void loadNextDataFromApi(int offset) {
        long maxID = tweets.get(tweets.size() - 1).ID;
        client.getNextTweets(maxID, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("addTweets", "letsgoo added tweet");

                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.getTweetsFromJsonArray(jsonArray));
                    // 25 is number of tweets pulled from client each time
                    adapter.notifyItemInserted(offset * 25);
                } catch (JSONException e) {
                    Log.d("addTweets", "shid tweet not added", e);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("addTweets", "Add new tweets error: " + throwable);
            }
        });
    }

    public void fetchTimelineAsync(int page) {
        /*
        Sends the network request to fetch the updated data
         */
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                adapter.clear();
                populateHomeTimeline();
                swipeContainer.setRefreshing(false);
                scrollListener.resetState();
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error: " + throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
            // Compose icon is tapped
            //Navigate to the compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
//            startActivityForResult(intent, REQUEST_CODE);
            editActivityResultLauncher.launch(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ActivityResultLauncher<Intent> editActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // If user comes back to activity
                    // with no error or cancel
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Get data passed from composed tweet
                        Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

                        // Update the RV with the tweet
                        // Modify the data source of tweets
                        tweets.add(0, tweet);
                        // Update the adapter
                        adapter.notifyItemInserted(0);
                        rvTweets.smoothScrollToPosition(0);
                    }
                }
            }
    );

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.getTweetsFromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure!!", throwable);
            }
        });
    }

    public void onLogoutButton(View view) {
        finish();

        // forget who's logged in
        TwitterApp.getRestClient(this).clearAccessToken();

        // navigate backwards to login screen
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //makes sure back button doesn't unlog out
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}