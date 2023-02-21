package com.example.ch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.ch.Adapters.viewPagerAdapter;
import com.example.ch.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        // init();
        setupViewPager();
        
        // Get Token - DEBUG PURPOSES
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            Log.d("TOKEN", s);
        });
    }

    private void setupViewPager() {
        b.viewPager.setOffscreenPageLimit(2);
        b.viewPager.setAdapter(new viewPagerAdapter(getSupportFragmentManager(), new Lifecycle() {
            @Override
            public void addObserver(@NonNull LifecycleObserver observer) {

            }

            @Override
            public void removeObserver(@NonNull LifecycleObserver observer) {

            }

            @NonNull
            @Override
            public State getCurrentState() {
                return null;
            }
        }));
        new TabLayoutMediator(b.tabDots, b.viewPager, (tab, position) -> {

            if (position == 0) {
                tab.setText("Chat");
            }
            else {
                tab.setText("People");
            }
        }).attach();
    }
}