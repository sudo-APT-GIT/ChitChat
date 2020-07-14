package com.example.chitchat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private String mChatUserName;

    private androidx.appcompat.widget.Toolbar mChatToolBar;
    private DatabaseReference mRootRef;

    private TextView mTitle;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("user_name");
        mChatToolBar = (Toolbar) findViewById(R.id.chat_app_bar_layout);
        setSupportActionBar(mChatToolBar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        mRootRef = FirebaseDatabase.getInstance().getReference();

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mTitle = (TextView) findViewById(R.id.chatusername);
        mLastSeen = (TextView) findViewById(R.id.chatlastseen);
        mProfileImage = (CircleImageView) findViewById(R.id.chatprofileimage);

        mTitle.setText(mChatUserName);
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild("online")) {
                    String online = snapshot.child("online").getValue().toString();
                    if (online.equals("true")) {
                        mLastSeen.setText("Online");
                    } else {
                        mLastSeen.setText(online);
                    }
                }
                String image = snapshot.child("image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.default_gravatar).into(mProfileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth.getCurrentUser() != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
