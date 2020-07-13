package com.example.chitchat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayName, mStatus, mFriendsCount;
    private ImageView mImageView;
    private Button mSendRequestButton;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mrootRef;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state;
    String newNID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id;

        String data = getIntent().getStringExtra("user_id");

        if (data == null) {

            user_id = getIntent().getStringExtra("from_user_id");

        } else {

            user_id = getIntent().getStringExtra("user_id");
        }

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mrootRef = FirebaseDatabase.getInstance().getReference();

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
                mSendRequestButton.setEnabled(false);
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

                            }
                        } else {

                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(user_id)) {

                                        mCurrent_state = "friends";
                                        mSendRequestButton.setText("Unfriend");

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                        mSendRequestButton.setEnabled(true);

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
                    /*
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

                                                HashMap<String, String> notifications = new HashMap<>();
                                                notifications.put("from", mCurrentUser.getUid());
                                                notifications.put("type", "request");

                                                mNotificationDatabase.child(user_id).push().setValue(notifications).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mSendRequestButton.setEnabled(true);
                                                        mCurrent_state = "req_sent";
                                                        mSendRequestButton.setText("Cancel Friend Request");
                                                        Toast.makeText(ProfileActivity.this, "Friend Request sent!", Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                            }
                                        });
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Aww snap! Something Went Wrong :(", Toast.LENGTH_SHORT).show();
                                        mSendRequestButton.setEnabled(true);
                                    }
                                }

                            });*/

                    DatabaseReference newNotification = mrootRef.child("Notifications").child(user_id).push();
                    newNID = newNotification.getKey();

                    HashMap<String, String> notificationsData = new HashMap<>();
                    notificationsData.put("from", mCurrentUser.getUid());
                    notificationsData.put("type", "request");


                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "recvd");
                    requestMap.put("Notifications/" + user_id + "/" + newNID, notificationsData);

                    mrootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ProfileActivity.this, "Aww snap! Something Went Wrong :(", Toast.LENGTH_SHORT).show();
                            }
                            mSendRequestButton.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mSendRequestButton.setText("Cancel Friend Request");
                        }
                    });
                }


                //----------------- Cancel Request --------------//

                if (mCurrent_state.equals("req_sent")) {

                    /*
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
                    });*/

                    Map requestSentMap = new HashMap();
                    requestSentMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    requestSentMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);
                    requestSentMap.put("Notifications/" + user_id + "/" + newNID, null);

                    mrootRef.updateChildren(requestSentMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ProfileActivity.this, "Aww snap! Something Went Wrong :(", Toast.LENGTH_SHORT).show();
                            }

                            mSendRequestButton.setEnabled(true);
                            mCurrent_state = "not_friends";
                            mSendRequestButton.setText("Send Friend Request");
                        }
                    });

                }


                // ------------------- Req Recvd state ----------------------//

                if (mCurrent_state.equals("req_recvd")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map reqRecvdState = new HashMap();
                    reqRecvdState.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, currentDate);
                    reqRecvdState.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), currentDate);

                    reqRecvdState.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    reqRecvdState.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mrootRef.updateChildren(reqRecvdState, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ProfileActivity.this, "Aww snap! Something Went Wrong :(", Toast.LENGTH_SHORT).show();
                            }
                            mSendRequestButton.setEnabled(true);
                            mCurrent_state = "friends";
                            mSendRequestButton.setText("Unfriend");
                        }
                    });

        /*
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
         */
                }
                /*------------ Unfriend -----------------------------*/
                if (mCurrent_state.equals("friends")) {
                    Map unfriendState = new HashMap();
                    unfriendState.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendState.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mrootRef.updateChildren(unfriendState, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(ProfileActivity.this, "Aww snap! Something Went Wrong :(", Toast.LENGTH_SHORT).show();
                            }

                            mSendRequestButton.setEnabled(true);
                            mCurrent_state = "not_friends";
                            mSendRequestButton.setText("Send Friend Request");
                        }
                    });

                    /*
                    mSendRequestButton.setEnabled(false);
                    mFriendsDatabase.child(mCurrentUser.getUid()).child(user_id)
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendsDatabase.child(user_id).child(mCurrentUser.getUid())
                                    .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendRequestButton.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mSendRequestButton.setText("Send Friend Request");
                                }
                            });
                        }
                    });*/
                }
            }
        });
    }
}
