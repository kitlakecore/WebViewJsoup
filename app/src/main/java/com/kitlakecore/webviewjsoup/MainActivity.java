package com.kitlakecore.webviewjsoup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import org.jsoup.nodes.Document;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    String url = "https://www.google.com";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        startScraping();


    }

    private void startScraping() {
        WebViewJsoup webViewJsoup;
        webViewJsoup = new WebViewJsoup();

        webViewJsoup.runWebScraping(webView, url, new WebViewJsoup.WebScrapingCallback() {
            @Override
            public void onScrapingComplete(Document document) {
                String scrapedDoc = document.toString();
                Log.d("scrapedDoc", scrapedDoc.toString());
            }

            @Override
            public void onScrapingError(String errorMessage) {

            }
        });
    }
}