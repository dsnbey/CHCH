package com.example.ch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.ch.Common.Common;
import com.example.ch.Models.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN_REQUEST_CODE = 6363;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener listener;

    FirebaseDatabase database;
    DatabaseReference userRef;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (mAuth != null && listener != null) {
            mAuth.removeAuthStateListener(listener);
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(Common.USER_REFERENCE);
        listener = myFirebaseAuth -> {
            Dexter.withContext(this).withPermissions(Arrays.asList(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )).withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted())
                    {
                        FirebaseUser user = myFirebaseAuth.getCurrentUser();
                        if (user != null){
                            checkUserFromFirebase();
                        }
                        else {
                            showLoginLayout();
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Please enable all permissions", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                }
            }).check();
        };
    }

    private void showLoginLayout() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers).build(), LOGIN_REQUEST_CODE);
    }

    private void checkUserFromFirebase() {
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            UserModel user = snapshot.getValue(UserModel.class);
                            user.setUid(snapshot.getKey());
                            goToHomeActivity(user);
                        }
                        else {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showRegisterLayout() {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        finish();
    }

    private void goToHomeActivity(UserModel user) {
        Common.currentUser = user;
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser usr = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText(this, "error: " + response.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}