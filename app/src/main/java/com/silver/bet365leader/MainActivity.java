
package com.silver.bet365leader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {

    private String mApiEndpoint;
    private String mUserName;
    private static final String BET365_URL = "https://www.bet365.com";
    private static final int REQUEST_CODE = 1000;
    private final String TAG = getClass().getSimpleName();


    PayloadRecorder mRecorder;
    String mOverridejsContent;
    RequestQueue mRequestQueue;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);*/


        // Load Endppint and Username from SharedPreferences

        AppSharedInfo appSharedInfo = new AppSharedInfo(this);
        mApiEndpoint = appSharedInfo.getEndpoint();
        mUserName = appSharedInfo.getUsername();

        Log.d(TAG, "API_ENDPOINT : " + mApiEndpoint);
        Log.d(TAG, "API_ENDPOINT : " + mUserName);


        AssetManager am = this.getAssets();
        try {
            InputStream inputStream = am.open("override.js");
            int fileSize = inputStream.available();
            byte[] data = new byte[fileSize];
            int nResult = inputStream.read(data);
            if (nResult == 0) {
                Log.e("assets", "read 0 byte from override.js");
            }
            this.mOverridejsContent = new String(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                view.evaluateJavascript(MainActivity.this.mOverridejsContent, null);
                super.onPageStarted(view, url, favicon);
            }
        });

        mRecorder = new PayloadRecorder();
        webView.addJavascriptInterface(mRecorder, "recorder");
        this.mRequestQueue = Volley.newRequestQueue(this);

        webView.loadUrl(BET365_URL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btnSetting) {
            startActivityForResult(new Intent(this, SettingActivity.class), REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mApiEndpoint = data.getStringExtra("Endpoint");
            mUserName = data.getStringExtra("Username");

            Log.d(TAG, "API_ENDPOINT : " + mApiEndpoint);
        }
    }

    private void sendRequestData(String username, String url, String data, Boolean isRequest) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", username);
            jsonBody.put("url", url);
            jsonBody.put("payload", data);
            jsonBody.put("isRequest", isRequest);

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.GET, mApiEndpoint, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            mRequestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PayloadRecorder {
        @JavascriptInterface
        public void recordPayload(String method, String url, String payload, boolean isRequest) {
            Boolean filtered = false;
            if ((url.indexOf("/betswebapi/addbet") > 0 && isRequest == false) ||
                    url.indexOf("betswebapi/placebet?betguid") > 0 ||
                    (url.indexOf("betslip/mybets/closebet.ashx?") > 0 && isRequest == true)) {
                filtered = true;
            }
            if (filtered) {
                Log.e("seasea", String.format("%s:: %s ::: %s ::: %s ::: (%b)", mUserName, method, url, payload, isRequest));
                MainActivity.this.sendRequestData(mUserName, url, payload, isRequest);
            }
        }

        @JavascriptInterface
        public void recordResult(String method, String url, String payload) {
            Log.e("bruce", String.format("%s-%s: %s", method, url, "request finished"));
        }
    }
}
