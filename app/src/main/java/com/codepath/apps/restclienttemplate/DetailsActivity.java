package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

public class DetailsActivity extends AppCompatActivity {
    private Tweet tweet;
    private ImageView ivProfileImage;
    private TextView tvBody;
    private TextView tvScreenName;
    private ImageView ivTweetImage;
    private TextView tvTime;
    private TextView tvName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);

        // get tweet from parcel
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("TWEET"));

        ivProfileImage = (ImageView) findViewById(R.id.ivProfile);
        tvBody = (TextView) findViewById(R.id.tvBody);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        tvName = (TextView) findViewById(R.id.tvName);
        ivTweetImage = (ImageView) findViewById(R.id.ivTweetImage);
//        tvTime = itemView.findViewById(R.id.tvTime);
        Log.d("DetailsActivity",tweet.user.profileImageUrl);

        tvScreenName.setText(tweet.user.screenName);
        tvName.setText(tweet.user.name);
        tvBody.setText(tweet.body);
        Glide.with(this).load(tweet.user.profileImageUrl).circleCrop().into(ivProfileImage);
        if(tweet.imageURL == "no url found") {
            ivTweetImage.setVisibility(View.GONE);
        }
        else {
            ivTweetImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(tweet.imageURL).into(ivTweetImage);
        }
    }
}
