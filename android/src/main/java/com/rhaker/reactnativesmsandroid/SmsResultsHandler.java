package com.rhaker.reactnativesmsandroid;

import java.util.ArrayList;

public class SmsResultsHandler {
  private ArrayList<String> errorMessages;
  private int pendingMessages;

  public SmsResultsHandler(int pendingMessages) {
    this.errorMessages = new ArrayList<String>();
    this.pendingMessages = pendingMessages;
  }

  public void addErrorMessage(String errorMessage){
    this.errorMessages.add(errorMessage);
  }

  public String getErrorMessagesAsString(){
    if (errorMessages.size() == 0){
      return null;
    }
    return errorMessages.toString();
  }

  public void messagePartProcessed(){
    pendingMessages--;
  }

  public boolean isAllMessagesSent(){
    return this.pendingMessages == 0;
  }
}