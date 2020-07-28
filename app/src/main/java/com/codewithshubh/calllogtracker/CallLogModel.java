package com.codewithshubh.calllogtracker;

public class CallLogModel {
    String phNumber, contactName, callType, callDate, callTime, callDuration;

    public CallLogModel(String phNumber, String contactName, String callType, String callDate, String callTime,
                        String callDuration) {
        this.phNumber = phNumber;
        this.contactName = contactName;
        this.callType = callType;
        this.callDate = callDate;
        this.callTime = callTime;
        this.callDuration = callDuration;

    }

    public String getPhNumber() {
        return phNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public String getCallType() {
        return callType;
    }

    public String getCallDate() {
        return callDate;
    }

    public String getCallTime() {
        return callTime;
    }

    public String getCallDuration() {
        return callDuration;
    }
}
