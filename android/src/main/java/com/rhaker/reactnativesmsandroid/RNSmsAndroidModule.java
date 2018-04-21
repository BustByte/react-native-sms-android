package com.rhaker.reactnativesmsandroid;

import android.os.Build;
import android.telephony.SmsManager;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.content.Context;
import android.app.Activity;
import android.content.BroadcastReceiver;

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNSmsAndroidModule extends ReactContextBaseJavaModule {

  private static final String TAG = RNSmsAndroidModule.class.getSimpleName();
  private static final String SMS_SENT = "SMS_SENT";
  private ReactApplicationContext reactContext;

  public RNSmsAndroidModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "SmsAndroid";
  }

  @ReactMethod
  public void sms(String phoneNumber, String message, final Callback callback) {
    try {
      RNSmsAndroidModule.validateTextMessage(message);
      SmsManager smsManager = SmsManager.getDefault();
      PendingIntent sentPendingIntent = PendingIntent.getBroadcast(getReactApplicationContext(), 0, new Intent(SMS_SENT), 0);
      ArrayList<String> smsParts = smsManager.divideMessage(message);
      ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
      int numberOfMessages = smsParts.size();
      SmsResultsHandler smsResultsHandler = new SmsResultsHandler(numberOfMessages);
      
      // Create one intent per message part, so that we can handle the result of each sent message separately
      for (int i = 0; i < numberOfMessages; i++) {
        sentPendingIntents.add(sentPendingIntent);
      }
      
      // Register a handler for each of the sent messages
      getReactApplicationContext().registerReceiver(
        getSmsBroadCastReceiver(smsResultsHandler, callback),
        new IntentFilter(SMS_SENT)
      );
      
      // Send all the messages
      smsManager.sendMultipartTextMessage(phoneNumber, null, smsParts, sentPendingIntents, null);
    }

    catch (Exception e) {
      callback.invoke(e.getMessage(), "error");
    }
  }

  public BroadcastReceiver getSmsBroadCastReceiver(final SmsResultsHandler smsResultsHandler, final Callback callback){
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
              break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
              smsResultsHandler.addErrorMessage("Service is currently unavailable");
              break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
              smsResultsHandler.addErrorMessage("Service is currently unavailable");
              break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
              smsResultsHandler.addErrorMessage("No pdu provided");
              break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
              smsResultsHandler.addErrorMessage("Radio was explicitly turned off");
              break;
            default:
              smsResultsHandler.addErrorMessage("Something went wrong.");
        }
        // Mark *this* message as processed
        smsResultsHandler.messagePartProcessed();
        if (smsResultsHandler.isAllMessagesSent()){
          final String errorMessages = smsResultsHandler.getErrorMessagesAsString();
          if (errorMessages == null){
            callback.invoke("SMS sent successfully", "success");
          } else {
            callback.invoke(errorMessages, "error");
          }
        }
      }
    };
  }

  public static void validateTextMessage(String message) throws Exception {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
      throw new Exception("Android version must be atleast KitKat");
    }
    if (message == null || message.isEmpty()){
      throw new IllegalArgumentException("Message cannot be empty");
    }
  }
}
