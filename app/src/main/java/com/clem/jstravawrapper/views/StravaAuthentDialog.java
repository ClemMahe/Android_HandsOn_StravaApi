package com.clem.jstravawrapper.views;

import android.app.Dialog;
import android.content.Context;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.clem.jstravawrapper.R;
import com.clem.jstravawrapper.utils.StravaPreferences;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by CMA10935 on 16/08/2016.
 */
public class StravaAuthentDialog extends Dialog {

    //These are constants used for build the urls
    private static final String AUTHORIZATION_URL = "https://www.strava.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://www.strava.com/oauth/token";
    private static final String SECRET_KEY_PARAM = "client_secret";
    private static final String RESPONSE_TYPE_PARAM = "response_type";
    private static final String RESPONSE_TYPE_VALUE = "code";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String STATE_PARAM = "state";
    private static final String SCOPE = "scope";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    /*---------------------------------------*/

    private static final String SCOPE_READ_WRITE = "view_private,write";

    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";


    @BindView(R.id.webview_dialog_strava)
    WebView mWebViewDialog;
    @BindView(R.id.progressbar_dialog_strava)
    ProgressBar progressbarDialogStrava;
    @BindView(R.id.linearlayout_dialog_progress)
    LinearLayout linearlayoutDialogProgress;


    //Context
    private Context mContext;

    //Listener
    private IStravaAuthentDialogListener mListener;

    //Variables
    private String mApiKey;
    private String mSecretKey;
    private String mUrlRedirect;
    private String mKeyCsrf;


    //Result
    private String mAccessToken;

    /**
     * Base constructor
     *
     * @param context Context
     */
    public StravaAuthentDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    /**
     * Base constructor
     *
     * @param context        Context
     * @param cancelable     Cancelable or not
     * @param cancelListener Cancel listener
     */
    public StravaAuthentDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    /**
     * Base constructor
     *
     * @param context    Context
     * @param themeResId Theme Res id
     */
    public StravaAuthentDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_auth_strava);
        ButterKnife.bind(this);

        //CreateView
        this.createWebView();
    }

    public void initWebView(IStravaAuthentDialogListener listener, String apiKey, String secretId, String urlRedirect, String generatedKeyCsrf) {
        //Values
        this.mListener = listener;
        this.mApiKey = apiKey;
        this.mSecretKey = secretId;
        this.mUrlRedirect = urlRedirect;
        this.mKeyCsrf = generatedKeyCsrf;
    }

    private void createWebView() {
        //this.mWebViewDialog = new WebView(mContext);
        this.mWebViewDialog.requestFocus(View.FOCUS_DOWN);

        this.linearlayoutDialogProgress.setVisibility(View.VISIBLE);

        //Set a custom web view client
        mWebViewDialog.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                linearlayoutDialogProgress.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
                if (authorizationUrl.startsWith(mUrlRedirect)) {
                    Log.i("Authorize", "");
                    Uri uri = Uri.parse(authorizationUrl);
                    //We take from the url the authorizationToken and the state token. We have to check that the state token returned by the Service is the same we sent.
                    //If not, that means the request may be a result of CSRF and must be rejected.
                    String stateToken = uri.getQueryParameter(STATE_PARAM);
                    if (stateToken == null || !stateToken.equals(mKeyCsrf)) {
                        Log.e("Authorize", "State token doesn't match");
                        return true;
                    }

                    //If the user doesn't allow authorization to our application, the authorizationToken Will be null.
                    String authorizationToken = uri.getQueryParameter(RESPONSE_TYPE_VALUE);
                    if (authorizationToken == null) {
                        Log.i("Authorize", "The user doesn't allow authorization.");
                        return true;
                    }
                    Log.i("Authorize", "Auth token received: " + authorizationToken);

                    //Generate URL for requesting Access Token
                    String accessTokenUrl = getAccessTokenUrl(authorizationToken);
                    //We make the request in a AsyncTask
                    new PostRequestAsyncTask().execute(accessTokenUrl);
                } else {
                    //Default behaviour
                    Log.i("Authorize", "Redirecting to: " + authorizationUrl);
                    mWebViewDialog.loadUrl(authorizationUrl);
                }
                return true;
            }
        });

        //Get the authorization Url
        String authUrl = getAuthorizationUrl();
        Log.i("Authorize", "Loading Auth Url: " + authUrl);
        //Load the authorization URL into the webView
        mWebViewDialog.loadUrl(authUrl);
    }


    /**
     * Method that generates the url for get the access token from the Service
     *
     * @return Url
     */
    private String getAccessTokenUrl(String authorizationToken) {
        return ACCESS_TOKEN_URL
                + QUESTION_MARK
                + CLIENT_ID_PARAM + EQUALS + mApiKey
                + AMPERSAND
                + SECRET_KEY_PARAM + EQUALS + mSecretKey
                + AMPERSAND
                + RESPONSE_TYPE_VALUE + EQUALS + authorizationToken;
    }

    /**
     * Method that generates the url for get the authorization token from the Service
     *
     * @return Url
     */
    private String getAuthorizationUrl() {
        return AUTHORIZATION_URL
                + QUESTION_MARK + RESPONSE_TYPE_PARAM + EQUALS + RESPONSE_TYPE_VALUE
                + AMPERSAND + CLIENT_ID_PARAM + EQUALS + mApiKey
                + AMPERSAND + STATE_PARAM + EQUALS + mKeyCsrf
                + AMPERSAND + REDIRECT_URI_PARAM + EQUALS + mUrlRedirect
                + AMPERSAND + SCOPE + EQUALS + SCOPE_READ_WRITE;
    }


    private class PostRequestAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            linearlayoutDialogProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            if (urls.length > 0) {
                String url = urls[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpost = new HttpPost(url);
                try {
                    HttpResponse response = httpClient.execute(httpost);
                    if (response != null) {
                        //If status is OK 200
                        if (response.getStatusLine().getStatusCode() == 200) {
                            String result = EntityUtils.toString(response.getEntity());
                            //Convert the string result to a JSON Object
                            JSONObject resultJson = new JSONObject(result);
                            //Extract data from JSON Response
                            //int expiresIn = resultJson.has("expires_in") ? resultJson.getInt("expires_in") : 0;
                            mAccessToken = resultJson.has("access_token") ? resultJson.getString("access_token") : null;
                            if (mAccessToken != null) {
                                ////Store both expires in and access token in shared preferences
                                StravaPreferences.setStravaAccessToken(mContext, mAccessToken);
                                return true;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e("Authorize", "Error Http response " + e.getLocalizedMessage());
                } catch (ParseException e) {
                    Log.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
                } catch (JSONException e) {
                    Log.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            linearlayoutDialogProgress.setVisibility(View.GONE);
            //Listener
            if (status) {
                mListener.authenticationSuccess();
            } else {
                mListener.authenticationFailed();

            }
            //close popup
            dismiss();
        }
    }

    ;


}

