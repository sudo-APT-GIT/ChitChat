package com.example.chitchat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayName, mStatus, mFriendsCount;
    private ImageView mImageView;
    private Button mSendRequestButton;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDisplayName = (TextView) findViewById(R.id.profiledisplayname);
        mStatus = (TextView) findViewById(R.id.profilestatus);
        mFriendsCount = (TextView) findViewById(R.id.profilefriendscount);
        mImageView = (ImageView) findViewById(R.id.profileimageview);
        mSendRequestButton = (Button) findViewById(R.id.profilesendrequestbtn);

        mCurrent_state = "not_friends";

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String displayname = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("image").getValue().toString();

                mDisplayName.setText(displayname);
                mStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_gravatar).into(mImageView);

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.hasChild(user_id)) {
                            String req_type = snapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("recvd")) {

                                mCurrent_state = "req_recvd";
                                mSendRequestButton.setText("Accept Friend Request");

                            } else if (req_type.equals("sent")) {

                                mCurrent_state = "req_sent";
                                mSendRequestButton.setText("Cancel Friend Request");

                            } else {

                                mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild(user_id)){

                                            mCurrent_state = "friends";
                                            mSendRequestButton.setText("Unfriend");

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSendRequestButton.setEnabled(false);
                //-------------- Not Friends --------------------------//
                if (mCurrent_state.equals("not_friends")) {

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id)
                            .child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                                .setValue("recvd").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mSendRequestButton.setEnabled(true);
                                                mCurrent_state = "req_sent";
                                                mSendRequestButton.setText("Cancel Friend Request");
                                                Toast.makeText(ProfileActivity.this, "Friend Request sent!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Aww snap! Something Went Wrong :(", Toast.LENGTH_SHORT).show();
                                        mSendRequestButton.setEnabled(true);
                                    }
                                }

                            });
                }


                //----------------- Cancel Request --------------//

                if (mCurrent_state.equals("req_sent")) {

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id)
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mSendRequestButton.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mSendRequestButton.setText("Send Friend Request");

                                }
                            });

                        }
                    });
                }


                // ------------------- Req Recvd state ----------------------//

                if (mCurrent_state.equals("req_recvd")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    mFriendsDatabase.child(mCurrentUser.getUid()).child(user_id)
                            .setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id)
                                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid())
                                                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mSendRequestButton.setEnabled(true);
                                                            mCurrent_state = "friends";
                                                            mSendRequestButton.setText("Unfriend");

                                                        }
                                                    });

                                                }
                                            });


                                        }
                                    });
                        }
                    });
                }
            }
        });
    }
}
