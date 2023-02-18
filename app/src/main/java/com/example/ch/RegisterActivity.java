package com.example.ch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ch.Common.Common;
import com.example.ch.Models.UserModel;
import com.example.ch.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding b;
    FirebaseDatabase database;
    DatabaseReference userRef;

    MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.
            datePicker().build();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Calendar calendar = Calendar.getInstance();
    boolean isSelectedBirthDate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);
        init();
        setDefaultData();
    }

    private void setDefaultData() {
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        b.inputPhone.setText(mUser.getPhoneNumber());
        b.inputPhone.setEnabled(false);

        b.inputDateOfBirth.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                materialDatePicker.show(getSupportFragmentManager() ,materialDatePicker.toString());

            }
        });

        b.buttonSignUp.setOnClickListener(view -> {
            if (!isSelectedBirthDate) {
                Toast.makeText(RegisterActivity.this, "Please enter birth date", Toast.LENGTH_SHORT).show();
                return;
            }
            UserModel user = new UserModel();

            // personal
            user.setFirstName(b.inputFirstName.getText().toString());
            user.setLastname(b.inputLastName.getText().toString());
            user.setBio(b.inputBio.getText().toString());
            user.setPhone(b.inputPhone.getText().toString());
            user.getBirthDate(calendar.getTimeInMillis());
            user.setUid(mUser.getUid());

            userRef.child(user.getUid())
                    .setValue(user)
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                        Common.currentUser = user;
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    });


        });


    }

    private void init() {
        ButterKnife.bind(this);
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(Common.USER_REFERENCE);
        materialDatePicker.addOnPositiveButtonClickListener(selection ->{
            calendar.setTimeInMillis(selection);
            b.inputDateOfBirth.setText(simpleDateFormat.format(selection));
            isSelectedBirthDate = true;
        });
    }
}