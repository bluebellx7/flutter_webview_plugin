package com.flutter_webview_plugin;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.sdk.WebSettings;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.view.WindowManager;

public class  X5WebViewActivity extends Activity {
    private WebView webView;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);


        // String appBarColor = getIntent().getStringExtra("appBarColor");
        // int color = Color.parseColor(appBarColor);
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  
        //     getWindow().setStatusBarColor(color);
        // }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
        //     //设置状态栏透明
        //     getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // }
        // if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
        //     //设置状态栏透明
        //     getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // }


        webView =new WebView(this);
        setContentView(webView);

        initView();
    }


    private void initView() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String appBarColor = intent.getStringExtra("appBarColor");

        ActionBar actionBar = getActionBar();
		if(actionBar != null){
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);


            //一、将十六进制颜色值转为int类型color
            int color = Color.parseColor(appBarColor);
            //二、将int类型color值转为Drawable类型
            ColorDrawable drawable = new ColorDrawable(color);
            //三、将drawable设置给actionbar
            actionBar.setBackgroundDrawable(drawable);
		}
	
        webView.loadUrl(intent.getStringExtra("url"));

        WebSettings webSetting= webView.getSettings();
        webSetting.setAllowFileAccess(true);
		webSetting.setLayoutAlgorithm(com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		webSetting.setSupportZoom(true);
		webSetting.setBuiltInZoomControls(true);
		webSetting.setUseWideViewPort(true);
		webSetting.setSupportMultipleWindows(false);
		webSetting.setLoadWithOverviewMode(true);
		webSetting.setAppCacheEnabled(true);
		webSetting.setDatabaseEnabled(true);
		webSetting.setDomStorageEnabled(true);
		webSetting.setJavaScriptEnabled(true);
		webSetting.setGeolocationEnabled(true);
		webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
		webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
		webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
		webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0).getPath());
		webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
   
        // 新增内容
        this.getWindow().setFormat(android.graphics.PixelFormat.TRANSLUCENT);

        //适配5.0不允许http和https混合使用情况
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(   MenuItem item) {
   
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }
  

	@Override
	protected void onDestroy() {
		if (webView != null)
        webView.destroy();
		super.onDestroy();
    }

}