package com.pichillilorenzo.flutter_appavailability;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.annotation.TargetApi;

import org.jetbrains.annotations.Nullable;

/** AppAvailability */
public class AppAvailability implements FlutterPlugin {
  private @Nullable FlutterPluginBinding flutterPluginBinding;
  private final Activity activity;

  public AppAvailability(Registrar registrar, Activity activity) {
    this.activity = activity;
  }

  @SuppressWarnings("deprecation")
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "com.pichillilorenzo/flutter_appavailability");
    channel.setMethodCallHandler(new MethodCallHandlerImpl(registrar.context(), registrar.activity()));
  }

  @Override
  public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding;
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding flutterPluginBinding) {
    this.flutterPluginBinding = null;
  }
}
