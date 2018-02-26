package com.example.conner.projectresearch;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends NativeNFCActivity {
    private WebView mWebView;
    private WebSettings mWebSettings;
    private WebViewClient mWebViewClient;

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    @Override
    void onCardData(byte[] bytes) {
        CharSequence text = "test: " + bytes.toString();
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialize the UI */
        setContentView(R.layout.activity_main);

        configureWebView(savedInstanceState);

        /* Install the Web app NFC interface */
        mWebView.addJavascriptInterface(this, "NativeNFC");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWebView.loadUrl("http://www.example.com/");
        mWebView.evaluateJavascript("NativeNFC.enable();", null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
    }

    /**
     * Configures the WebView with necessary options for running the Web app
     *
     * @param savedInstanceState State bundle to restore WebView state from
     */
    private void configureWebView(Bundle savedInstanceState) {
        mWebView = (WebView) findViewById(R.id.MainWebView);
        mWebSettings = mWebView.getSettings();

        mWebViewClient = new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        };

        mWebView.setWebViewClient(mWebViewClient);

        mWebSettings.setJavaScriptEnabled(true);

        /* Restore the state of the WebView if the Activity is being restarted */
        if(savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        }
    }
}