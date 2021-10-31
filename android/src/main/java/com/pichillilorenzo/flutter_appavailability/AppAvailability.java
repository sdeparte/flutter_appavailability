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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.annotation.TargetApi;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

/** AppAvailability */
public class AppAvailability implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "com.pichillilorenzo/flutter_appavailability");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {}

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

      case "getInstalledAppsByQuery":
        String query = call.argument("query").toString();
        result.success(getInstalledAppsByQuery(query));
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

    if (info != null) {
      result.success(this.convertPackageInfoToJson(info));
      return;
    }

    result.error("", "App not found " + uri, null);
  }

  @TargetApi(Build.VERSION_CODES.DONUT)
  private List<Map<String, Object>> getInstalledApps() {
    PackageManager packageManager = activity.getPackageManager();
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

  @TargetApi(Build.VERSION_CODES.DONUT)
  private List<Map<String, Object>> getInstalledAppsByQuery(String query) {
    PackageManager packageManager = activity.getPackageManager();
    List<PackageInfo> apps = packageManager.getInstalledPackages(0);
    List<Map<String, Object>> installedApps = new ArrayList<>(apps.size());
    int systemAppMask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;

    for (PackageInfo pInfo : apps) {
      if ((pInfo.applicationInfo.flags & systemAppMask) != 0) {
        continue;
      }

      if (!pInfo.packageName.toLowerCase().contains(query.toLowerCase()) &&
          !pInfo.applicationInfo.loadLabel(packageManager).toString().toLowerCase().contains(query.toLowerCase())
      ) {
        continue;
      }

      Map<String, Object> map = this.convertPackageInfoToJson(pInfo);
      installedApps.add(map);
    }

    return installedApps;
  }

  private PackageInfo getAppPackageInfo(String uri) {
    PackageManager packageManager = activity.getPackageManager();

    try {
      return packageManager.getPackageInfo(uri, 0);
    } catch(PackageManager.NameNotFoundException ignored) {}

    return null;
  }

  private Map<String, Object> convertPackageInfoToJson(PackageInfo info) {
    String encodedImage = "";

    Map<String, Object> map = new HashMap<>();

    PackageManager packageManager = activity.getPackageManager();

    try {
      Drawable icon = packageManager.getApplicationIcon(info.packageName);

      encodedImage = Base64Utils.encodeToBase64(
        DrawableUtils.getBitmapFromDrawable(icon),
        Bitmap.CompressFormat.PNG,
        100
      );
    } catch (PackageManager.NameNotFoundException ignored) {}

    map.put("app_name", info.applicationInfo.loadLabel(packageManager).toString());
    map.put("package_name", info.packageName);
    map.put("version_code", String.valueOf(info.versionCode));
    map.put("version_name", info.versionName);
    map.put("app_icon", encodedImage);

    return map;
  }

  private void isAppEnabled(String packageName, Result result) {
    boolean appStatus = false;

    try {
      ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(packageName, 0);
      if (ai != null) {
        appStatus = ai.enabled;
      }
    } catch (PackageManager.NameNotFoundException e) {
      result.error("", e.getMessage() + " " + packageName, e);
      return;
    }

    result.success(appStatus);
  }

  @TargetApi(Build.VERSION_CODES.CUPCAKE)
  private void launchApp(String packageName, Result result) {
    PackageInfo info = getAppPackageInfo(packageName);

    if (info != null) {
      Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
      if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.getApplicationContext().startActivity(launchIntent);
        result.success(null);

        return;
      }
    }

    result.error("", "App not found " + packageName, null);
  }
}
