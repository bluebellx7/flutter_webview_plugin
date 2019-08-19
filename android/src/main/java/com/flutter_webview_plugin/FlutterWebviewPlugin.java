package com.flutter_webview_plugin;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.widget.FrameLayout;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.sdk.TbsVideo;

import android.os.Build;

import android.os.Bundle;
import java.util.HashMap;
import android.util.Log;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FlutterWebviewPlugin
 */
public class FlutterWebviewPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {
    private Activity activity;
    private WebviewManager webViewManager;
    private Context context;
    static MethodChannel channel;
    private static final String CHANNEL_NAME = "flutter_webview_plugin";

    public static void registerWith(PluginRegistry.Registrar registrar) {
        channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
        final FlutterWebviewPlugin instance = new FlutterWebviewPlugin(registrar.activity(),registrar.activeContext());
        registrar.addActivityResultListener(instance);
        channel.setMethodCallHandler(instance);
    }

    private FlutterWebviewPlugin(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }



    
    public void initX5(Context context) {
        // 在调用TBS初始化、创建WebView之前进行如下配置，以开启优化方案
        HashMap <String,Object> map = new HashMap <String,Object> ();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setNeedInitX5FirstTime(true);
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.setTbsListener(new TbsListener() {
            @Override public void onDownloadFinish(int i) {
                Log.d("FileReader", "下载完成");
            }
    
            @Override public void onInstallFinish(int i) {
                Log.d("FileReader", "安装完成");
                onX5LoadComplete();
            }
    
            @Override public void onDownloadProgress(int i) {
                Log.d("FileReader", "下载进度:" + i);
            }
        });
    
        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override public void onCoreInitFinished() {
                Log.d("FileReader", "内核初始化完成");
            }
    
            @Override public void onViewInitFinished(boolean b) {
                Log.d("FileReader", "view初始化完成状态:" + b);
                onX5LoadComplete();
            }
        });
    }


    private void onX5LoadComplete() {
        if (channel != null) {
        channel.invokeMethod("onLoad", isLoadX5(context));
        }

        }
        boolean isLoadX5(Context context) {
        return QbSdk.canLoadX5(context);
    }


    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "initX5":
                initX5(context);
            case "canUseTbsPlayer":
                result.success(TbsVideo.canUseTbsPlayer(context));
            case "openVideo":
                String url = call.argument("url");
                String screenMode = call.argument("fullScreen");//?103:104;
                Bundle bundle = new Bundle();
                if(screenMode==null){
                    screenMode=103;
                }
                bundle.putInt("screenMode", Integer.parseInt(screenMode));
                TbsVideo.openVideo(context, url, bundle);
                result.success(null);
            case "launch":
                openUrl(call, result);
                break;
            case "close":
                close(call, result);
                break;
            case "eval":
                eval(call, result);
                break;
            case "resize":
                resize(call, result);
                break;
            case "reload":
                reload(call, result);
                break;
            case "back":
                back(call, result);
                break;
            case "forward":
                forward(call, result);
                break;
            case "hide":
                hide(call, result);
                break;
            case "show":
                show(call, result);
                break;
            case "reloadUrl":
                reloadUrl(call, result);
                break;
            case "stopLoading":
                stopLoading(call, result);
                break;
            case "cleanCookies":
                cleanCookies(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void openUrl(MethodCall call, MethodChannel.Result result) {
        boolean hidden = call.argument("hidden");
        String url = call.argument("url");
        String userAgent = call.argument("userAgent");
        boolean withJavascript = call.argument("withJavascript");
        boolean clearCache = call.argument("clearCache");
        boolean clearCookies = call.argument("clearCookies");
        boolean withZoom = call.argument("withZoom");
        boolean withLocalStorage = call.argument("withLocalStorage");
        boolean supportMultipleWindows = call.argument("supportMultipleWindows");
        boolean appCacheEnabled = call.argument("appCacheEnabled");
        Map<String, String> headers = call.argument("headers");
        boolean scrollBar = call.argument("scrollBar");
        boolean allowFileURLs = call.argument("allowFileURLs");
        boolean useWideViewPort = call.argument("useWideViewPort");
        String invalidUrlRegex = call.argument("invalidUrlRegex");
        boolean geolocationEnabled = call.argument("geolocationEnabled");
        boolean debuggingEnabled = call.argument("debuggingEnabled");

        if (webViewManager == null || webViewManager.closed == true) {
            webViewManager = new WebviewManager(activity, context);
        }

        FrameLayout.LayoutParams params = buildLayoutParams(call);

        activity.addContentView(webViewManager.webView, params);

        webViewManager.openUrl(withJavascript,
                clearCache,
                hidden,
                clearCookies,
                userAgent,
                url,
                headers,
                withZoom,
                withLocalStorage,
                scrollBar,
                supportMultipleWindows,
                appCacheEnabled,
                allowFileURLs,
                useWideViewPort,
                invalidUrlRegex,
                geolocationEnabled,
                debuggingEnabled
        );
        result.success(null);
    }

    private FrameLayout.LayoutParams buildLayoutParams(MethodCall call) {
        Map<String, Number> rc = call.argument("rect");
        FrameLayout.LayoutParams params;
        if (rc != null) {
            params = new FrameLayout.LayoutParams(
                    dp2px(activity, rc.get("width").intValue()), dp2px(activity, rc.get("height").intValue()));
            params.setMargins(dp2px(activity, rc.get("left").intValue()), dp2px(activity, rc.get("top").intValue()),
                    0, 0);
        } else {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            params = new FrameLayout.LayoutParams(width, height);
        }

        return params;
    }

    private void stopLoading(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.stopLoading(call, result);
        }
        result.success(null);
    }

    private void close(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.close(call, result);
            webViewManager = null;
        }
    }

    /**
     * Navigates back on the Webview.
     */
    private void back(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.back(call, result);
        }
        result.success(null);
    }

    /**
     * Navigates forward on the Webview.
     */
    private void forward(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.forward(call, result);
        }
        result.success(null);
    }

    /**
     * Reloads the Webview.
     */
    private void reload(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.reload(call, result);
        }
        result.success(null);
    }

    private void reloadUrl(MethodCall call, MethodChannel.Result result) {
        if (webViewManager != null) {
            String url = call.argument("url");
            webViewManager.reloadUrl(url);
        }
        result.success(null);
    }

    private void eval(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.eval(call, result);
        }
    }

    private void resize(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            FrameLayout.LayoutParams params = buildLayoutParams(call);
            webViewManager.resize(params);
        }
        result.success(null);
    }

    private void hide(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.hide(call, result);
        }
        result.success(null);
    }

    private void show(MethodCall call, final MethodChannel.Result result) {
        if (webViewManager != null) {
            webViewManager.show(call, result);
        }
        result.success(null);
    }

    private void cleanCookies(MethodCall call, final MethodChannel.Result result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean aBoolean) {

                }
            });
        } else {
            CookieManager.getInstance().removeAllCookie();
        }
        result.success(null);
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public boolean onActivityResult(int i, int i1, Intent intent) {
        if (webViewManager != null && webViewManager.resultHandler != null) {
            return webViewManager.resultHandler.handleResult(i, i1, intent);
        }
        return false;
    }
}
