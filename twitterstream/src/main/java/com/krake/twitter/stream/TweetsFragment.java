package com.krake.twitter.stream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.krake.core.OrchardError;
import com.krake.core.app.OrchardDataModelFragment;
import com.krake.core.data.DataModel;
import com.krake.core.widget.ObjectsRecyclerViewAdapter;
import com.krake.core.widget.RecycleViewSnapScrollListener;
import com.krake.twitter.R;
import com.krake.twitter.adapter.TweetAdapter;
import com.krake.twitter.adapter.holder.TweetHolder;
import com.krake.twitter.model.TweetsLoadingConfiguration;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * OrchardDataFragment che mostra i tweet caricati in base alle configurazioni di lato Orchard.
 * La classe dato passata come Argument per OrchardDataFragment.ARG_DATA_CLASS deve implementare l'interfaccia
 * {@link TweetsLoadingConfiguration} normalmente assegnato alla classe ConfigurazioneTweet.
 * <p>
 * I tweet caricati sono anche cachati per evitare di sovracaricare di chiamate Twitter.
 * La cache viene mantenuta valida in base a R.integer.tweet_cache_validity
 */
public class TweetsFragment extends OrchardDataModelFragment implements ObjectsRecyclerViewAdapter.ClickReceiver<Tweet> {

    private static final String PREF_KEY_TWEET = "Tweets";
    private static final String PREF_KEY_DATE = "Date";

    private TextView mTweetsLoadingErrorTextView;
    private ProgressBar mLoadingTweetsProgress;

    private TweetsLoadingConfiguration mTweetConfiguration;

    private TwitterApiClient twitterAPICLient;

    private ObjectsRecyclerViewAdapter<Tweet, TweetHolder> mAdapter;

    private TweetLoadCallback mCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tweets, container, false);

        mTweetsLoadingErrorTextView = view.findViewById(android.R.id.text1);
        mLoadingTweetsProgress = view.findViewById(android.R.id.progress);

        RecyclerView tweetList = view.findViewById(android.R.id.list);
        tweetList.addOnScrollListener(new RecycleViewSnapScrollListener());

        mAdapter = new TweetAdapter(getActivity(), null);
        mAdapter.setDefaultClickReceiver(this);

        tweetList.setAdapter(mAdapter);

        if (!loadSavedTweets()) {
            loadTweets();
            TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
            if (session != null) {
                twitterAPICLient = new TwitterApiClient(session);
            } else {
                loadTweetsWithGuestSession();
            }
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallback = null;
        mTweetConfiguration = null;
    }

    private void loadTweetsWithGuestSession() {
        twitterAPICLient = new TwitterApiClient();
        loadTweets();
    }

    @Override
    public void onDataModelChanged(@Nullable DataModel dataModel) {
        if (dataModel != null) {
            if (dataModel.getListData().size() > 0) {
                mTweetConfiguration = (TweetsLoadingConfiguration) dataModel.getListData().get(0);
                loadTweets();
            }
        }
    }

    @Override
    public void onDataLoadingError(@NotNull OrchardError orchardError) {
        showLoadingOfTweetsFailed(getString(R.string.error_loading_tweets));
    }

    private void showLoadingOfTweetsFailed(String message) {
        if (mAdapter.getItemCount() == 0) {
            mTweetsLoadingErrorTextView.setText(message);
            mTweetsLoadingErrorTextView.setVisibility(View.VISIBLE);
            mLoadingTweetsProgress.setVisibility(View.GONE);
        }
    }

    private void loadTweets() {
        if (mTweetConfiguration != null && twitterAPICLient != null) {
            mCallback = new TweetLoadCallback(this);

            twitterAPICLient.getSearchService().tweets(generateTweetQuery(), null, null, null, "recent", 20, null, null, null, true).enqueue(mCallback);
        }
    }

    private String generateTweetQuery() {
        String filters = mTweetConfiguration.getFiltroValue();

        StringTokenizer tokenizer = new StringTokenizer(filters, ",");

        StringBuilder fromQuery = new StringBuilder();
        StringBuilder hashTagQuery = new StringBuilder();

        StringBuilder additionalFilters = new StringBuilder();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();

            if (token.startsWith("@")) {
                if (fromQuery.length() > 0) {
                    fromQuery.append(" OR ");
                }
                fromQuery.append("from:");
                fromQuery.append(token.substring(1));
            } else if (token.startsWith("#")) {
                if (hashTagQuery.length() > 0) {
                    hashTagQuery.append(" OR ");
                }
                hashTagQuery.append(token);
            } else if (token.startsWith("-")) {
                additionalFilters.append(" ");
                additionalFilters.append(token);
            }
        }

        return fromQuery + " " + hashTagQuery.toString() + " " + additionalFilters;
    }

    private SharedPreferences openPrefs() {
        return getActivity().getSharedPreferences("Twitter", Context.MODE_PRIVATE);
    }

    private boolean loadSavedTweets() {
        SharedPreferences prefs = openPrefs();

        boolean valid = false;
        if (prefs.contains(PREF_KEY_TWEET)) {
            List<Tweet> saved = new Gson().fromJson(prefs.getString(PREF_KEY_TWEET, null),
                    new TypeToken<List<Tweet>>() {
                    }.getType());
            mAdapter.swapList(saved, true);
            mAdapter.notifyDataSetChanged();
            Date date = new Date(prefs.getLong(PREF_KEY_DATE, 0));

            valid = date.after(new Date(new Date().getTime() - 1000 * getActivity().getResources().getInteger(R.integer.tweet_cache_validity)));
            valid = valid && saved.size() > 0;
            if (saved.size() > 0) {
                mLoadingTweetsProgress.setVisibility(View.GONE);
            }
        }

        return valid;
    }

    @SuppressLint("ApplySharedPref")
    private void saveTweets(List<Tweet> mTweets) {
        openPrefs().edit()
                .putLong(PREF_KEY_DATE, new Date().getTime())
                .putString(PREF_KEY_TWEET, new Gson().toJson(mTweets))
                .commit();
    }

    @Override
    public void onViewClicked(@NotNull RecyclerView recyclerView, @NotNull View view, int position, Tweet tweet) {
        // set permalink if tweet id and screen name are available
        if (tweet != null && tweet.id > 0 && tweet.user != null && !TextUtils.isEmpty(tweet.user.screenName)) {
            String twitterPermalinkFormat = "https://twitter.com/%s/status/%d?ref_src=twsrc%%5Etwitterkit";
            Uri permalink = Uri.parse(String.format(Locale.US, twitterPermalinkFormat, tweet.user.screenName, tweet.id));
            final Intent intent = new Intent(Intent.ACTION_VIEW, permalink);
            IntentUtils.safeStartActivity(getContext(), intent);
        }
    }

    private static class TweetLoadCallback extends Callback<Search> {
        WeakReference<TweetsFragment> mFragment;

        TweetLoadCallback(TweetsFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void success(Result<Search> searchResult) {

            final TweetsFragment tweetFragment = mFragment.get();
            if (tweetFragment != null && tweetFragment.getActivity() != null) {
                List<Tweet> tweets = searchResult.data.tweets;
                tweetFragment.mAdapter.swapList(tweets, true);
                tweetFragment.mTweetsLoadingErrorTextView.setVisibility(View.GONE);
                tweetFragment.saveTweets(tweets);

                if (tweets.size() > 0) {
                    tweetFragment.mLoadingTweetsProgress.setVisibility(View.GONE);
                } else {
                    tweetFragment.showLoadingOfTweetsFailed(tweetFragment.getString(R.string.error_no_tweets_to_show));
                }
            }
        }

        @Override
        public void failure(TwitterException e) {
            final TweetsFragment tweetFragment = mFragment.get();
            if (tweetFragment != null && tweetFragment.getActivity() != null) {
                if (e.getMessage().contains("401")) {
                    tweetFragment.loadTweetsWithGuestSession();
                } else {
                    tweetFragment.showLoadingOfTweetsFailed(tweetFragment.getString(R.string.error_loading_tweets));
                }
            }
        }
    }
}