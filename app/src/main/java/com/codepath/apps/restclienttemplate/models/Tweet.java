package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public String body;
    public String createdAt;
    public String timeAgo;
    public User user;
    public String imageURL;
    public long ID;

    // empty constructor for parceler
    public Tweet() {}

    /**
     * Takes a JSONObject and gets the necessary information to make a Tweet object
     * Returns the Tweet object that was created
     * @param jsonObject
     * @return Tweet object
     * @throws JSONException
     */

    public static Tweet tweetFromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        if(jsonObject.has("full_text")) {
            tweet.body = jsonObject.getString("full_text");
        } else {
            tweet.body = jsonObject.getString("text");
        }

        tweet.createdAt = jsonObject.getString("created_at");
        tweet.timeAgo = "· "+ tweet.getRelativeTimeAgo(tweet.createdAt);
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.imageURL = getImageURL(jsonObject);
        tweet.ID = jsonObject.getLong("id");

        return tweet;
    }

    /**
     * Returns the URL for the first image in a tweet if it exists
     * If there are no images in the tweet, return a String that expresses that there are no
     * images in the tweet
     * @param jsonObject
     * @return String that either represents the url of the image in the tweet or represents that
     * there are no images in the tweet
     * @throws JSONException
     */
    public static String getImageURL(JSONObject jsonObject) throws JSONException {
        JSONObject entities = jsonObject.getJSONObject("entities");
        if (entities.has("media")){
            JSONArray mediaArray = entities.getJSONArray("media");
            JSONObject media = (JSONObject) mediaArray.get(0);
            String url = media.getString("media_url_https");
            return url;
        }
        return "no url found";
    }

    /**
     * Creates a List of Tweet objects from a JSONArray of JSONObjects (which was pulled from Twitter API)
     * Uses helper method fromJson to get information from a JSONObject to make a Tweet
     * @param jsonArray
     * @return List of Tweet objects
     * @throws JSONException
     */
    public static List<Tweet> getTweetsFromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            tweets.add(tweetFromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    /**
    Returns a string representing how long it's been since a tweet was posted (ex. 2 d, 5 m, just now)
    @param rawJsonDate date pulled from Twitter API
    @return relative timestamp string
     */
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            Log.i("Tweet", "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }
}
