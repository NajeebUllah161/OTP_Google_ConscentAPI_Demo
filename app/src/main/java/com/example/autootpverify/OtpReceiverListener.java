package com.example.autootpverify;

public interface OtpReceiverListener {

    void onOtpSuccess(String otp);

    void onOtpTimeout();
}
