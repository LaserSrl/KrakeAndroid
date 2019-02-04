package com.krake.firebaseauth;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.krake.core.login.LoginViewManager;
import com.krake.core.network.RemoteRequest;


public class GoogleSignInButtonManager extends LoginViewManager implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int LOGIN_WITH_GOOGLE_REQUEST = 30763;
    private final SignInButton signInButton;
    private GoogleApiClient apiClient;
    private String token;

    public GoogleSignInButtonManager(@NonNull Activity activity, @NonNull LoginListener loginFragment) {
        super(activity, loginFragment);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getString(R.string.default_web_client_id), true)
                //.requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        apiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                .enableAutoManage((FragmentActivity) activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton = (SignInButton) activity.getLayoutInflater().inflate(R.layout.google_login_button, null);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);

    }

    @Override
    public View getLoginView() {
        return signInButton;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == LOGIN_WITH_GOOGLE_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                token = result.getSignInAccount().getServerAuthCode();
                getLoginFragment().onManagerTokenAvailable(this);
            } else {
                Status s = result.getStatus();
                s.getStatus();
            }
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
                    .setQuery(getString(R.string.orchard_login_provider_key), getString(R.string.orchard_login_provider_google))
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View view) {
        if (view == signInButton) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
            getActivity().startActivityForResult(signInIntent, LOGIN_WITH_GOOGLE_REQUEST);
        }
    }
}
