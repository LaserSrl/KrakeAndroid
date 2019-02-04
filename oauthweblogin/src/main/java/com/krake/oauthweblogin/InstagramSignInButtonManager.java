package com.krake.oauthweblogin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;

import com.krake.core.login.LoginViewManager;
import com.krake.core.network.RemoteRequest;


public class InstagramSignInButtonManager extends LoginViewManager implements View.OnClickListener {

    private static final int LOGIN_WITH_INSTAGRAM_REQUEST = 30766;
    private final View signInButton;
    private String token;


    public InstagramSignInButtonManager(@NonNull Activity activity, @NonNull LoginListener loginFragment) {
        super(activity, loginFragment);

        signInButton = activity.getLayoutInflater().inflate(R.layout.instagram_login_button, null);
        signInButton.setOnClickListener(this);

    }

    @Override
    public View getLoginView() {
        return signInButton;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LOGIN_WITH_INSTAGRAM_REQUEST && resultCode == Activity.RESULT_OK) {

            Uri uri = Uri.parse(data.getStringExtra(OAuthWebLoginActivity.EXTRA_RESULT_COMPLETE_CALL_URL));

            token = uri.getQueryParameter("code");

            if (token != null)
                getLoginFragment().onManagerTokenAvailable(this);
            return true;

        }
        return false;
    }

    @Override
    protected void setButtonEnabled(boolean enabled) {
        signInButton.setEnabled(enabled);
    }

    @Override
    public boolean sendTokenToOrchard() {

        if (token != null) {
            RemoteRequest request = new RemoteRequest(getActivity())
                    .setMethod(RemoteRequest.Method.GET)
                    .setQuery(getString(R.string.orchard_login_token_key), token)
                    .setQuery(getString(R.string.orchard_login_provider_key), getString(R.string.orchard_login_provider_instagram))
                    .setQuery("Culture", getString(R.string.orchard_language))
                    .setPath(getString(R.string.orchard_login_url_path));

            com.krake.core.login.LoginManager.shared.login(getActivity(), request, true);
            return true;
        }
        return false;

    }


    @Override
    public void logout() {
        token = null;
    }


    @Override
    public void onClick(View view) {
        if (view == signInButton) {
            Intent signInIntent = new Intent(getActivity(), OAuthWebLoginActivity.class);


            Uri.Builder builder = new Uri.Builder();

            builder.scheme("https")
                    .encodedAuthority("api.instagram.com/oauth/authorize")
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("client_id", getString(R.string.instagram_api_key))
                    .appendQueryParameter("redirect_uri", getString(R.string.orchard_base_service_url) + getString(R.string.orchard_login_redirect_path));
            signInIntent.putExtra(OAuthWebLoginActivity.EXTRA_COMPLETE_CALL_URL, builder.build().toString());
            getActivity().startActivityForResult(signInIntent, LOGIN_WITH_INSTAGRAM_REQUEST);
        }
    }
}
