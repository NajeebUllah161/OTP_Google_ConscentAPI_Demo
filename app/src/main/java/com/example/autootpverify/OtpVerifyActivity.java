package com.example.autootpverify;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.autootpverify.databinding.ActivityOtpVerifyBinding;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerifyActivity extends AppCompatActivity {

    private ActivityOtpVerifyBinding binding;
    private String verificationId;
    // Auto OTP read..
    private OTP_Broadcast_Receiver otp_broadcast_receiver;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        editTextInput();

        binding.tvMobile.setText(String.format(
                "+91-%s", getIntent().getStringExtra("phone")
        ));

        verificationId = getIntent().getStringExtra("verificationId");

        binding.tvResendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(OtpVerifyActivity.this, "OTP Send Successfully.", Toast.LENGTH_SHORT).show();
                againOtpSend();

            }
        });

        binding.btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBarVerify.setVisibility(View.VISIBLE);
                binding.btnVerify.setVisibility(View.INVISIBLE);
                if (binding.etC1.getText().toString().trim().isEmpty() ||
                        binding.etC2.getText().toString().trim().isEmpty() ||
                        binding.etC3.getText().toString().trim().isEmpty() ||
                        binding.etC4.getText().toString().trim().isEmpty() ||
                        binding.etC5.getText().toString().trim().isEmpty() ||
                        binding.etC6.getText().toString().trim().isEmpty()) {
                    Toast.makeText(OtpVerifyActivity.this, "OTP is not Valid!", Toast.LENGTH_SHORT).show();
                } else {
                    if (verificationId != null) {
                        String code = binding.etC1.getText().toString().trim() +
                                binding.etC2.getText().toString().trim() +
                                binding.etC3.getText().toString().trim() +
                                binding.etC4.getText().toString().trim() +
                                binding.etC5.getText().toString().trim() +
                                binding.etC6.getText().toString().trim();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                        FirebaseAuth
                                .getInstance()
                                .signInWithCredential(credential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            binding.progressBarVerify.setVisibility(View.VISIBLE);
                                            binding.btnVerify.setVisibility(View.INVISIBLE);
                                            Toast.makeText(OtpVerifyActivity.this, "Welcome...", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(OtpVerifyActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        } else {
                                            binding.progressBarVerify.setVisibility(View.GONE);
                                            binding.btnVerify.setVisibility(View.VISIBLE);
                                            Toast.makeText(OtpVerifyActivity.this, "OTP is not Valid!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

        // OTP auto retrieval method
        autoOtpReceiver();

    }

    private void autoOtpReceiver() {
        otp_broadcast_receiver = new OTP_Broadcast_Receiver();
        this.registerReceiver(otp_broadcast_receiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));
        otp_broadcast_receiver.initListener(new OtpReceiverListener() {
            @Override
            public void onOtpSuccess(String otp) {
                int optChar1 = Character.getNumericValue(otp.charAt(0));
                int optChar2 = Character.getNumericValue(otp.charAt(1));
                int optChar3 = Character.getNumericValue(otp.charAt(2));
                int optChar4 = Character.getNumericValue(otp.charAt(3));
                int optChar5 = Character.getNumericValue(otp.charAt(4));
                int optChar6 = Character.getNumericValue(otp.charAt(5));

                binding.etC1.setText(String.valueOf(optChar1));
                binding.etC2.setText(String.valueOf(optChar2));
                binding.etC3.setText(String.valueOf(optChar3));
                binding.etC4.setText(String.valueOf(optChar4));
                binding.etC5.setText(String.valueOf(optChar5));
                binding.etC6.setText(String.valueOf(optChar6));

            }

            @Override
            public void onOtpTimeout() {
                Toast.makeText(OtpVerifyActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otp_broadcast_receiver != null) {
            unregisterReceiver(otp_broadcast_receiver);
        }
    }



    private void againOtpSend() {

        binding.progressBarVerify.setVisibility(View.INVISIBLE);
        binding.btnVerify.setVisibility(View.INVISIBLE);

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                binding.progressBarVerify.setVisibility(View.GONE);
                binding.btnVerify.setVisibility(View.VISIBLE);
                Toast.makeText(OtpVerifyActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                super.onCodeSent(verificationId, token);
                binding.progressBarVerify.setVisibility(View.GONE);
                binding.btnVerify.setVisibility(View.VISIBLE);
                Toast.makeText(OtpVerifyActivity.this, "OTP is successfully send.", Toast.LENGTH_SHORT).show();

            }

        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+92" + getIntent().getStringExtra("phone").trim())
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallback)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void editTextInput() {
        binding.etC1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etC2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etC2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etC3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etC3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etC4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etC4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etC5.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.etC5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.etC6.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}