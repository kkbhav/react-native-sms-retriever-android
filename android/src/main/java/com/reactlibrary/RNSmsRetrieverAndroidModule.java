
package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class RNSmsRetrieverAndroidModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;
  private Handler handler;
  private String SMS_RETRIEVE_EVENT_NAME = "RNSmsRetrieverAndroid_SMS_RETRIEVE_EVENT";
  private IntentFilter intentFilter = new IntentFilter("com.google.android.gms.auth.api.phone.SMS_RETRIEVED");

  public RNSmsRetrieverAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.handler = new Handler(reactContext.getMainLooper());
  }

  @Override
  public String getName() {
    return "RNSmsRetrieverAndroid";
  }

  @ReactMethod
  public void getAppSignature(final Promise promise) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          AppSignatureHelper helper = new AppSignatureHelper(reactContext);
          ArrayList<String> signatures = helper.getAppSignatures();
          WritableNativeArray array = new WritableNativeArray();
          for (String signature: signatures) {
            array.pushString(signature);
          }
          promise.resolve(array);
        } catch (Exception e) {
          promise.reject(e);
        }
      }
    });
    thread.start();
  }

  @ReactMethod
  public void retrieveSMS(final Promise promise) {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {

          // Get an instance of SmsRetrieverClient, used to start listening for a matching
          // SMS message.
          SmsRetrieverClient client = SmsRetriever.getClient(reactContext /* context */);

          // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
          // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
          // action SmsRetriever#SMS_RETRIEVED_ACTION.
          Task<Void> task = client.startSmsRetriever();

          // Listen for success/failure of the start Task. If in a background thread, this
          // can be made blocking using Tasks.await(task, [timeout]);
          task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              Log.d("RNSmsRetrieverAndroid", "successfully subscribed");
              getReactApplicationContext().registerReceiver(receiver, intentFilter);
              handler.postDelayed(runnable, 1000 * 60 * 6);
              promise.resolve(true);
            }
          });

          task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              Log.d("RNSmsRetrieverAndroid", "failed to add listener");
              Log.d("RNSmsRetrieverAndroid", e.getMessage());
              getReactApplicationContext().unregisterReceiver(receiver);
              promise.reject(e);
            }
          });
        } catch (Exception e) {
          promise.reject(e);
        }
      }
    });
    thread.start();
  }

  final Runnable runnable = new Runnable() {
    @Override
    public void run() {
      WritableNativeMap map = new WritableNativeMap();
      map.putString("message", null);
      map.putString("error", "ETIMEOUT");
      map.putInt("code", 408);
      sendEvent(reactContext, SMS_RETRIEVE_EVENT_NAME, map);
      getReactApplicationContext().unregisterReceiver(receiver);
    }
  };

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context contextOther, Intent intent) {
      if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
        Bundle extras = intent.getExtras();
        Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
        WritableNativeMap map = new WritableNativeMap();
        switch(status.getStatusCode()) {
          case CommonStatusCodes.SUCCESS:
            handler.removeCallbacks(runnable);
            // Get SMS message contents
            String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
            Log.d("RNSmsRetrieverAndroid", "message received" + message);

            map.putString("message", message);
            map.putString("error", null);
            map.putInt("code", 200);
            sendEvent(reactContext, SMS_RETRIEVE_EVENT_NAME, map);
            break;
          case CommonStatusCodes.ERROR:
          case CommonStatusCodes.DEVELOPER_ERROR:
            handler.removeCallbacks(runnable);
            map.putString("message", null);
            map.putString("error", "Something went wrong");
            map.putInt("code", 404);
            sendEvent(reactContext, SMS_RETRIEVE_EVENT_NAME, map);
            break;
          case CommonStatusCodes.TIMEOUT:
            handler.removeCallbacks(runnable);
            Log.d("RNSmsRetrieverAndroid", "ETIMEOUT");
            map.putString("message", null);
            map.putString("error", "ETIMEOUT");
            map.putInt("code", 408);
            sendEvent(reactContext, SMS_RETRIEVE_EVENT_NAME, map);
            break;
        }
      }
    }
  };

  private void sendEvent(ReactContext reactContext, String eventName, WritableNativeArray arr) {
    reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, arr);
  }

  private void sendEvent(ReactContext reactContext, String eventName, WritableNativeMap map) {
    reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, map);
  }

  private void sendEvent(ReactContext reactContext, String eventName) {
    reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, null);
  }
}