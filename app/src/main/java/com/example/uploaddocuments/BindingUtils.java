package com.example.uploaddocuments;

import android.webkit.WebView;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class BindingUtils {

    @BindingAdapter(value = {"imageUrl", "callBack"}, requireAll = false)
    public static void setImage(ImageView imageView, String url, Callback callback) {
        if (callback == null) {
            Picasso.get().load(url).into(imageView);
        } else {
            Picasso.get().load(url).into(imageView, callback);
        }
    }

    @BindingAdapter("webViewUrl")
    public static void loadUrlToWebView(WebView webView, String url) {
        webView.loadUrl(url);
    }
}
