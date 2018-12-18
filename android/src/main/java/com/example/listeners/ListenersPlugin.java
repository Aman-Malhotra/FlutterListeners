package com.example.listeners;

// import io.flutter.plugin.common.MethodCall;
// import io.flutter.plugin.common.MethodChannel;
// import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
// import io.flutter.plugin.common.MethodChannel.Result;
// import io.flutter.plugin.common.PluginRegistry.Registrar;

// /** ListenersPlugin */
// public class ListenersPlugin implements MethodCallHandler {
//   /** Plugin registration. */
//   public static void registerWith(Registrar registrar) {
//     final MethodChannel channel = new MethodChannel(registrar.messenger(), "listeners");
//     channel.setMethodCallHandler(new ListenersPlugin());
//   }

//   @Override
//   public void onMethodCall(MethodCall call, Result result) {
//     if (call.method.equals("getPlatformVersion")) {
//       result.success("Android " + android.os.Build.VERSION.RELEASE);
//     } else {
//       result.notImplemented();
//     }
//   }
// }


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * MusicFinderPlugin
 */
public class MusicFinderPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
  private final MethodChannel channel;

  private static final int REQUEST_CODE_STORAGE_PERMISSION = 3777;

  private Activity activity;
  private Map<String, Object> arguments;
  private boolean executeAfterPermissionGranted;
  private static MusicFinderPlugin instance;
  private Result pendingResult;

  //MusicPlayer
  private static AudioManager am;

  final Handler handler = new Handler();

  MediaPlayer mediaPlayer;

  /**
   * Plugin registration.
   */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "music_finder");
    instance = new MusicFinderPlugin(registrar.activity(), channel);
    registrar.addRequestPermissionsResultListener(instance);
    channel.setMethodCallHandler(instance);

  }

  private MusicFinderPlugin(Activity activity, MethodChannel channel) {
    this.activity = activity;
    this.channel = channel;
    this.channel.setMethodCallHandler(this);
    if (MusicFinderPlugin.am == null) {
      MusicFinderPlugin.am = (AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }
  }
  private int checkSelfPermission(Context context, String permission) {
    if (permission == null) {
      throw new IllegalArgumentException("permission is null");
    }
    return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
  }

  private void setNoPermissionsError() {
    pendingResult.error("permission", "you don't have the user permission to access the camera", null);
    pendingResult = null;
    arguments = null;
  }

  private final Runnable sendData = new Runnable() {
    public void run() {
      try {
        if (!mediaPlayer.isPlaying()) {
          handler.removeCallbacks(sendData);
        }
        int time = mediaPlayer.getCurrentPosition();
        channel.invokeMethod("audio.onCurrentPosition", time);

        handler.postDelayed(this, 200);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

}