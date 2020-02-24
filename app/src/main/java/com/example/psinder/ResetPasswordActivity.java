package com.example.psinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPasswordActivity extends AppCompatActivity
{
    private Button ResetPasswordSendEmailButton;
    private EditText ResetEmailInput;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.forget_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Resetowanie hasla");
        



        ResetPasswordSendEmailButton = (Button) findViewById(R.id.reset_password_email_button);
        ResetEmailInput = (EditText) findViewById(R.id.reset_password_EMAIL);

        // cos ponizej jest moze byc zdupcone ale nie powinno ale moze byc bo raz kompa wydupcylo

        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String userEmail = ResetEmailInput.getText().toString();

                if(TextUtils.isEmpty(userEmail))
                {
                    Toast.makeText(ResetPasswordActivity.this, "Wpisz poprawny Email", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(ResetPasswordActivity.this, "Sprawdz swoja skrzynke mailowa", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        });

        //wrazie co wywalic to wyzej i dziala
    }
}
