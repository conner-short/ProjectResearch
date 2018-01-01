package com.example.conner.projectresearch;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView wv;

    private class JSObject {
        @JavascriptInterface
        public void MakeToast() {
            Context c = getApplicationContext();
            CharSequence text = "A toast to the Emperor!";

            Toast toast = Toast.makeText(c, text, Toast.LENGTH_SHORT);

            toast.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wv = new WebView(this);
        wv.setWebViewClient(new WebViewClient());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(new JSObject(), "InjectedObject");

        setContentView(wv);

        wv.loadData("<html><body></body></html>", "text/html", null);
        wv.loadUrl("javascript:InjectedObject.MakeToast()");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if(wv.canGoBack()) {
            wv.goBack();
        } else {
            finish();
        }
    }
}