import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

/// Main class of the plugin.
class AppAvailability {
  static const MethodChannel _channel =
      const MethodChannel('com.pichillilorenzo/flutter_appavailability');

  /// Check if an app is available with the given [uri] scheme.
  ///
  /// Returns a [Map<String, String>] containing info about the App or throws a [PlatformException]
  /// if the app isn't found.
  ///
  /// The returned [Map] has a form like this:
  /// ```dart
  /// {
  ///   "app_name": "",
  ///   "package_name": "",
  ///   "versionCode": "",
  ///   "version_name": "",
  ///   "app_icon": ""
  /// }
  static Future<Map<String, String?>?> checkAvailability(String uri) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent('uri', () => uri);

    if (Platform.isAndroid) {
      Map<dynamic, dynamic> app = await _channel.invokeMethod("checkAvailability", args);
      return {
        "app_name": app["app_name"],
        "package_name": app["package_name"],
        "versionCode": app["versionCode"],
        "version_name": app["version_name"],
        "app_icon": app["app_icon"]
      };
    }
    else if (Platform.isIOS) {
      bool appAvailable = await (_channel.invokeMethod("checkAvailability", args) as bool);

      if (!appAvailable) {
        throw PlatformException(code: "", message: "App not found $uri");
      }

      return {
        "app_name": "",
        "package_name": uri,
        "versionCode": "",
        "version_name": "",
        "app_icon": ""
      };
    }

    return null;
  }

  /// Only for **Android**.
  ///
  /// Get the list of all installed apps, where
  /// each app has a form like [checkAvailability()].
  static Future<List<Map<String, String?>>> getInstalledApps(int? limit) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent('limit', () => limit);
    List<dynamic>? apps = await _channel.invokeMethod("getInstalledApps", args);
    if (apps != null && apps is List) {
      List<Map<String, String?>> list = [];
      for (var app in apps) {
        if (app is Map) {
          list.add({
            "app_name": app["app_name"],
            "package_name": app["package_name"],
            "versionCode": app["versionCode"],
            "version_name": app["version_name"],
            "app_icon": app["app_icon"]
          });
        }
      }

      return list;
    }

    return [];
  }

  /// Only for **Android**.
  ///
  /// Get the list of all installed apps, where
  /// each app has a form like [checkAvailability()].
  static Future<List<Map<String, String?>>> getInstalledAppsByQuery(String query, int? limit) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent('query', () => query);
    args.putIfAbsent('limit', () => limit);
    List<dynamic>? apps = await _channel.invokeMethod("getInstalledAppsByQuery", args);
    if (apps != null && apps is List) {
      List<Map<String, String?>> list = [];
      for (var app in apps) {
        if (app is Map) {
          list.add({
            "app_name": app["app_name"],
            "package_name": app["package_name"],
            "versionCode": app["versionCode"],
            "version_name": app["version_name"],
            "app_icon": app["app_icon"]
          });
        }
      }

      return list;
    }

    return [];
  }

  /// Only for **Android**.
  ///
  /// Check if the app is enabled or not with the given [uri] scheme.
  ///
  /// If the app isn't found, then a [PlatformException] is thrown.
  static Future<bool?> isAppEnabled(String uri) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent('uri', () => uri);
    return await _channel.invokeMethod("isAppEnabled", args);
  }

  /// Launch an app with the given [uri] scheme if it exists.
  ///
  /// If the app app isn't found, then a [PlatformException] is thrown.
  static Future<void> launchApp(String uri) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent('uri', () => uri);
    if (Platform.isAndroid) {
      await _channel.invokeMethod("launchApp", args);
    }
    else if (Platform.isIOS) {
      bool appAvailable = await (_channel.invokeMethod("launchApp", args) as FutureOr<bool>);
      if (!appAvailable) {
        throw PlatformException(code: "", message: "App not found $uri");
      }
    }

  }

}