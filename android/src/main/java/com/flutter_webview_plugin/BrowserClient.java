package com.flutter_webview_plugin;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import java.net.URISyntaxException;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.List;
import android.util.Log;
import android.content.res.Resources;

/**
 * Created by lejard_h on 20/12/2017.
 */

public class BrowserClient extends WebViewClient {
    private Pattern invalidUrlPattern = null;
    Context context;
    Activity activity;

    public BrowserClient() {
        this(null);
    }

    public BrowserClient(String invalidUrlRegex) {
        super();
        if (invalidUrlRegex != null) {
            invalidUrlPattern = Pattern.compile(invalidUrlRegex);
        }
    }

    public BrowserClient(final Activity activity, final Context context) {
        this(null);
        this.activity = activity;
        this.context = context;
    }

    public void updateInvalidUrlRegex(String invalidUrlRegex) {
        if (invalidUrlRegex != null) {
            invalidUrlPattern = Pattern.compile(invalidUrlRegex);
        } else {
            invalidUrlPattern = null;
        }
    }

    
   @Override
   public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//        Log.i("--------->","地址："+url);
       url = url.toLowerCase();

       Resources res = context.getResources();
       String[] filterUrls = res.getStringArray(R.array.adUrls);
       for (String adUrl : filterUrls ) {
           if (url.contains(adUrl)) {
            // Log.i("--------->","广告："+url);
            return new WebResourceResponse(null,null,null);
           }
       }
       return super.shouldInterceptRequest(view, url);
   }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("type", "startLoad");
        FlutterWebviewPlugin.channel.invokeMethod("onState", data);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);

        FlutterWebviewPlugin.channel.invokeMethod("onUrlChanged", data);

        data.put("type", "finishLoad");
        FlutterWebviewPlugin.channel.invokeMethod("onState", data);

    }

    // 注释代码
    // @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    // @Override
    // public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest
    // request) {
    // // returning true causes the current WebView to abort loading the URL,
    // // while returning false causes the WebView to continue loading the URL as
    // usual.
    // String url = request.getUrl().toString();
    // boolean isInvalid = checkInvalidUrl(url);
    // Map<String, Object> data = new HashMap<>();
    // data.put("url", url);
    // data.put("type", isInvalid ? "abortLoad" : "shouldStart");

    // FlutterWebviewPlugin.channel.invokeMethod("onState", data);
    // return isInvalid;
    // }

    Intent intent;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // returning true causes the current WebView to abort loading the URL,
        // while returning false causes the WebView to continue loading the URL as
        // usual.

        if (url.startsWith("http") || url.startsWith("https") || url.startsWith("ftp")) {
            // 返回true会终止url请求，返回false继续加载
            boolean isInvalid = checkInvalidUrl(url);
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("type", isInvalid ? "abortLoad" : "shouldStart");

            FlutterWebviewPlugin.channel.invokeMethod("onState", data);

            return isInvalid;
        }
        try {
            // URL Scheme 比如 taobao://... 自动跳转到淘宝app
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Log.d("-------------------------url", url);
            if (isAvailable(intent)) {
                String _scheme = url.substring(0, url.indexOf("://"));
                Log.d("-------------------------getScheme", _scheme);
                // 如果允许自动跳转第三方app
                if (FlutterWebviewPlugin.allowSchemes != null && FlutterWebviewPlugin.allowSchemes.size() > 0
                        && FlutterWebviewPlugin.allowSchemes.contains(_scheme)) {
                    activity.startActivity(intent);
                } else {
                    AlertDialog.Builder ad1 = new AlertDialog.Builder(activity);
                    ad1.setTitle("提示");
                    ad1.setMessage("确定跳转至第三方APP(" + _scheme + ")吗?");
                    DialogInterface.OnClickListener listener1 = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.startActivity(intent);
                        }
                    };
                    ad1.setPositiveButton(" 确 定 ", listener1);
                    DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface diyalog, int which) {
                            Toast t = Toast.makeText(context, "取消跳转！", Toast.LENGTH_LONG);
                            t.show();
                        }
                    };
                    ad1.setNegativeButton(" 取 消 ", listener2);
                    ad1.show();
                }
            } else {
                Toast t = Toast.makeText(context, "没有安装相应的第三方App！", Toast.LENGTH_LONG);
                t.show();
            }
        } catch (Exception e) {
            Log.d("-------------------------Exception", e.getMessage());
        }
        return true;
        // // 注释代码
        // boolean isInvalid = checkInvalidUrl(url);
        // Map<String, Object> data = new HashMap<>();
        // data.put("url", url);
        // data.put("type", isInvalid ? "abortLoad" : "shouldStart");

        // FlutterWebviewPlugin.channel.invokeMethod("onState", data);
        // return isInvalid;
    }

    // 新增代码
    public boolean isAvailable(Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    // 注释代码
    // @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    // @Override
    // public void onReceivedHttpError(WebView view, WebResourceRequest request,
    // WebResourceResponse errorResponse) {
    // super.onReceivedHttpError(view, request, errorResponse);
    // Map<String, Object> data = new HashMap<>();
    // data.put("url", request.getUrl().toString());
    // data.put("code", Integer.toString(errorResponse.getStatusCode()));
    // FlutterWebviewPlugin.channel.invokeMethod("onHttpError", data);
    // }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Map<String, Object> data = new HashMap<>();
        data.put("url", failingUrl);
        data.put("code", errorCode);
        FlutterWebviewPlugin.channel.invokeMethod("onHttpError", data);
    }

    private boolean checkInvalidUrl(String url) {
        if (invalidUrlPattern == null) {
            return false;
        } else {
            Matcher matcher = invalidUrlPattern.matcher(url);
            return matcher.lookingAt();
        }
    }
}