package nl.xservices.plugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothHeadset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HeadsetDetection extends CordovaPlugin {

  private static final String LOG_TAG = "HeadsetDetection";

  private static final String ACTION_DETECT = "detect";
  private static final String ACTION_EVENT = "registerRemoteEvents";
  protected static CordovaWebView mCachedWebView = null;

  BroadcastReceiver receiver;

  public HeadsetDetection() {
      this.receiver = null;
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
      super.initialize(cordova, webView);
      mCachedWebView = webView;
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
      intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
      intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

      this.receiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
              if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                  int state = intent.getIntExtra("state", -1);
                  switch (state) {
                  case 0:
                      Log.d(LOG_TAG, "Headset is unplugged");
                      mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteHeadsetRemoved();");
                      break;
                  case 1:
                      Log.d(LOG_TAG, "Headset is plugged");
                      mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteHeadsetAdded();");
                      break;
                  default:
                      Log.d(LOG_TAG, "I have no idea what the headset state is");
                  }
              }
              if (intent.getAction().equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                // mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteHeadsetRemoved();");
                Log.d(LOG_TAG, "Bluetooth headset state changed intent: " + intent.toString());
                Bundle extras = intent.getExtras();
                Log.d(LOG_TAG, "Bluetooth headset state changed extra: " + extras.toString());
                int state = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1);
                switch (state) {
                case 0:
                    Log.d(LOG_TAG, "Headset is unplugged");
                    mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteBluetoothHeadsetRemoved();");
                    break;
                case 3:
                    Log.d(LOG_TAG, "Headset is unplugged");
                    mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteBluetoothHeadsetRemoved();");
                    break;
                default:
                    mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteBluetoothHeadsetAdded();");
                    Log.d(LOG_TAG, "I have no idea what the headset state is");
                }
              }
              if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.d(LOG_TAG, "BluetoothAdapter-headset state changed intent: " + intent.toString());
                Bundle extras = intent.getExtras();
                Log.d(LOG_TAG, "BluetoothAdapter-headset state changed extra: " + extras.toString());
                int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                switch (state) {
                case 13:
                    Log.d(LOG_TAG, "BluetoothAdapter turning off -> HeadsetRemoved");
                    mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteBluetoothHeadsetRemoved();");
                    break;
                case 12:
                    Log.d(LOG_TAG, "BluetoothAdapter turning on -> check Headset in 3 sec");
                    mCachedWebView.sendJavascript("cordova.require('cordova-plugin-headsetdetection.HeadsetDetection').remoteBluetoothAdapterStarted();");
                    break;
                default:
                    Log.d(LOG_TAG, "I have no idea what the headset state is");
                }

              }
          }
      };
      mCachedWebView.getContext().registerReceiver(this.receiver, intentFilter);
  }

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_DETECT.equals(action) || ACTION_EVENT.equals(action)) {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, isHeadsetEnabled()));
        return true;
      } else {
        callbackContext.error("headsetdetection." + action + " is not a supported function. Did you mean '" + ACTION_DETECT + "'?");
        return false;
      }
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
      return false;
    }
  }

  private boolean isHeadsetEnabled() {
    final AudioManager audioManager = (AudioManager) cordova.getActivity().getSystemService(Context.AUDIO_SERVICE);
    return audioManager.isWiredHeadsetOn() ||
        audioManager.isBluetoothA2dpOn() ||
        audioManager.isBluetoothScoOn();
  }

  public void onDestroy() {
      removeHeadsetListener();
  }

  public void onReset() {
      removeHeadsetListener();
  }

  private void removeHeadsetListener() {
      if (this.receiver != null) {
          try {
              mCachedWebView.getContext().unregisterReceiver(this.receiver);
              this.receiver = null;
          } catch (Exception e) {
              Log.e(LOG_TAG, "Error unregistering battery receiver: " + e.getMessage(), e);
          }
      }
  }
}
