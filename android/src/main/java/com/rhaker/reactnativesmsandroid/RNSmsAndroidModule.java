package com.rhaker.reactnativesmsandroid;

import android.os.Build;
import android.telephony.SmsManager;
import java.lang.*;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class RNSmsAndroidModule extends ReactContextBaseJavaModule {

  private static final String TAG = RNSmsAndroidModule.class.getSimpleName();

  private ReactApplicationContext reactContext;

  // set the activity - pulled in from Main
  public RNSmsAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "SmsAndroid";
  }

  @ReactMethod
  public void sms(String phoneNumberString, String message, Callback callback) {
    try {
      SmsManager smsManager = SmsManager.getDefault();
      smsManager.sendTextMessage(phoneNumberString, null, message, null, null);
      callback.invoke(null, "success");
    }

    catch (Exception e) {
      callback.invoke(e.getMessage(), "error");
      e.printStackTrace();
    }
  }

  public static void validateTextMessage(String message){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
      throw new Exception("Android version must be atleast KitKat");
    }
    if (message == null || message.isEmpty()){
      throw new IllegalArgumentException("Message cannot be empty");
    }
  }
}
