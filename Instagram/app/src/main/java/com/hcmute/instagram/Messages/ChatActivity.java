package com.hcmute.instagram.Messages;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import com.hcmute.instagram.Home;
import com.hcmute.instagram.Messages.Adapter.PageAdapter;
import com.hcmute.instagram.R;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.ChatActivity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sociala");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.ChatActivity_mainTabPager);
        pageAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);

        tabLayout = findViewById(R.id.ChatActivity_maintabs);
        tabLayout.setupWithViewPager(viewPager);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean returnSuperKeyDown = true;

        if(keyCode == KeyEvent.KEYCODE_BACK){
            startActivity(new Intent(ChatActivity.this, Home.class));
        }
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Handle the click on the Up button (the back button in the toolbar)
            // Here you can perform any action you want, such as navigating back or executing a custom action.
            // For example, you can start the Home activity:
            startActivity(new Intent(ChatActivity.this, Home.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}