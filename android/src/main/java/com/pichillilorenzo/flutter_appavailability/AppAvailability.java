package com.pichillilorenzo.flutter_appavailability;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.annotation.TargetApi;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

/** AppAvailability */
public class AppAvailability implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private @Nullable FlutterPluginBinding flutterPluginBinding;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding;

    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "com.pichillilorenzo/flutter_appavailability");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.flutterPluginBinding = null;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {}

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {}

  @Override
  public void onDetachedFromActivity() {}

  @Override
  public void onMethodCall(MethodCall call, @NonNull Result result) {
    String uriSchema;
    switch (call.method) {
      case "checkAvailability":
        uriSchema = Objects.requireNonNull(call.argument("uri")).toString();
        this.checkAvailability(uriSchema, result);
        break;

      case "getInstalledApps":
        result.success(getInstalledApps());
        break;

      case "isAppEnabled":
        uriSchema = Objects.requireNonNull(call.argument("uri")).toString();
        this.isAppEnabled(uriSchema, result);
        break;

      case "launchApp":
        uriSchema = Objects.requireNonNull(call.argument("uri")).toString();
        this.launchApp(uriSchema, result);
        break;

      default:
        result.notImplemented();
    }
  }

  private void checkAvailability(String uri, Result result) {
    PackageInfo info = getAppPackageInfo(uri);

    if(info != null) {
      result.success(this.convertPackageInfoToJson(info));
      return;
    }

    result.error("", "App not found " + uri, null);
  }

  @TargetApi(Build.VERSION_CODES.DONUT)
  private List<Map<String, Object>> getInstalledApps() {
    if (flutterPluginBinding == null) {
      return new ArrayList<>(0);
    }

    PackageManager packageManager = flutterPluginBinding.getApplicationContext().getPackageManager();
    List<PackageInfo> apps = packageManager.getInstalledPackages(0);
    List<Map<String, Object>> installedApps = new ArrayList<>(apps.size());
    int systemAppMask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;

    for (PackageInfo pInfo : apps) {
      if ((pInfo.applicationInfo.flags & systemAppMask) != 0) {
        continue;
      }

      Map<String, Object> map = this.convertPackageInfoToJson(pInfo);
      installedApps.add(map);
    }

    return installedApps;
  }

  private PackageInfo getAppPackageInfo(String uri) {
    Context ctx = activity.getApplicationContext();
    final PackageManager pm = ctx.getPackageManager();

    try {
      return pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
    } catch(PackageManager.NameNotFoundException ignored) {

    }

    return null;
  }

  private Map<String, Object> convertPackageInfoToJson(PackageInfo info) {
    Map<String, Object> map = new HashMap<>();

    if (flutterPluginBinding == null) {
      map.put("app_name", "");
    } else {
      map.put("app_name", info.applicationInfo.loadLabel(flutterPluginBinding.getApplicationContext().getPackageManager()).toString());
    }

    map.put("package_name", info.packageName);
    map.put("version_code", String.valueOf(info.versionCode));
    map.put("version_name", info.versionName);

    return map;
  }

  private void isAppEnabled(String packageName, Result result) {
    boolean appStatus = false;

    if (flutterPluginBinding != null) {
      try {
        ApplicationInfo ai = flutterPluginBinding.getApplicationContext().getPackageManager().getApplicationInfo(packageName, 0);
        if (ai != null) {
          appStatus = ai.enabled;
        }
      } catch (PackageManager.NameNotFoundException e) {
        result.error("", e.getMessage() + " " + packageName, e);
        return;
      }
    }

    result.success(appStatus);
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  private void launchApp(String packageName, Result result) {
    PackageInfo info = getAppPackageInfo(packageName);

    if (flutterPluginBinding != null && info != null) {
      Intent launchIntent = flutterPluginBinding.getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
      if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        flutterPluginBinding.getApplicationContext().startActivity(launchIntent);
        result.success(null);

        return;
      }
    }

    result.error("", "App not found " + packageName, null);
  }
}
