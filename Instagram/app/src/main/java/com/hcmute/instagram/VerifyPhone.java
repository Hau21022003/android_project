package com.hcmute.instagram;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hcmute.instagram.R;
import java.util.concurrent.TimeUnit;

import com.hcmute.instagram.Login;
import com.hcmute.instagram.ReusableCode.ReusableCodeForAll;

public class VerifyPhone extends AppCompatActivity {

    private FirebaseAuth FAuth;
    private String verificationId;
    private EditText enterCode;
    private Button verifyButton;
    private Button resendButton;
    private TextView countdownText;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        FAuth = FirebaseAuth.getInstance();

        phoneNumber = getIntent().getStringExtra("phonenumber").trim();
        sendVerificationCode(phoneNumber);

        enterCode = findViewById(R.id.OTP);
        countdownText = findViewById(R.id.text);
        resendButton = findViewById(R.id.Resendotp);
        verifyButton = findViewById(R.id.Verify);

        resendButton.setVisibility(View.INVISIBLE);
        countdownText.setVisibility(View.INVISIBLE);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = enterCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    enterCode.setError("Enter valid code");
                    enterCode.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });

        startCountdown();
        setupResendButton();
    }

    private void sendVerificationCode(String number) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60L,
                TimeUnit.SECONDS,
                (Activity) TaskExecutors.MAIN_THREAD,
                mCallbacks
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    verificationId = s;
                }

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        enterCode.setText(code);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(VerifyPhone.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        linkCredential(credential);
    }

    private void linkCredential(PhoneAuthCredential credential) {
        FAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(VerifyPhone.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(VerifyPhone.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            ReusableCodeForAll.ShowAlert(VerifyPhone.this, "Error", task.getException().getMessage());
                        }
                    }
                });
    }

    private void startCountdown() {
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownText.setVisibility(View.VISIBLE);
                countdownText.setText("Resend Code within " + millisUntilFinished / 1000 + " Seconds");
            }

            @Override
            public void onFinish() {
                resendButton.setVisibility(View.VISIBLE);
                countdownText.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private void setupResendButton() {
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendButton.setVisibility(View.INVISIBLE);
                sendVerificationCode(phoneNumber);
                startCountdown();
            }
        });
    }
}
