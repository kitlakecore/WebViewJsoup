package com.kitlakecore.webviewjsoup;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebViewJsoup {

    public interface WebScrapingCallback {
        void onScrapingComplete(Document document);
        void onScrapingError(String errorMessage);
    }

    public static void runWebScraping(WebView webView, String url, final WebScrapingCallback callback) {
        // Check if we are already on the main UI thread.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // If we are on the main thread, proceed with WebView initialization directly.
            initializeWebView(webView, url, callback);
        } else {
            // If we are not on the main thread, use runOnUiThread to initialize the WebView.
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    initializeWebView(webView, url, callback);
                }
            });
        }
    }

    private static void initializeWebView(WebView webView, String url, final WebScrapingCallback callback) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true); // Enable DOM storage
        webView.getSettings().setLoadWithOverviewMode(true); // Load the WebView with overview mode
        webView.getSettings().setUseWideViewPort(true); // Use wide viewport
        webView.getSettings().setBlockNetworkImage(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Inject JavaScript to get the inner HTML of the job description element.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(
                            "(function() { return document.documentElement.outerHTML; })();", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String htmlContent) {
                                    // Unescape the HTML content to remove encoded characters.
                                    if (!TextUtils.isEmpty(htmlContent)){
                                        String unescapedHtmlContent = StringEscapeUtils.unescapeJava(htmlContent);
                                        // Use Jsoup to parse the HTML content and extract the desired data.
                                        Document document = Jsoup.parse(unescapedHtmlContent);
                                        callback.onScrapingComplete(document);
                                    } else {
                                        callback.onScrapingError("HTML content is empty.");
                                    }

                                    // Clean up WebView resources after scraping is done.
                                    webView.stopLoading();
                                    //webView.destroy();
                                }
                            }
                    );
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Handle the error here, display an error message, etc.
                callback.onScrapingError("WebView error: " + description);
                webView.stopLoading();
                //webView.destroy();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                // Handle HTTP errors here, display an error message, etc.
                callback.onScrapingError("HTTP error: " + errorResponse.getStatusCode());
                webView.stopLoading();
                //webView.destroy();
            }
        });

        webView.loadUrl(url);
    }

}
