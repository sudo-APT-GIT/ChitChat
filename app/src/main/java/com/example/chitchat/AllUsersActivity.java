package com.example.chitchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    private RecyclerView mRecylcerView;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mUserRef;

    private FirebaseRecyclerAdapter mAdapter;

    private ShimmerFrameLayout mShimmerViewContainer;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container_users);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mToolBar = (Toolbar) findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mRecylcerView = (RecyclerView) findViewById(R.id.alluserslist);
        mRecylcerView.setHasFixedSize(true);
        mRecylcerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        mRecylcerView.addItemDecoration(decoration);

        FirebaseRecyclerOptions<AllUsersModel> options = new FirebaseRecyclerOptions.Builder<AllUsersModel>()
                .setQuery(mUsersDatabase, AllUsersModel.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<AllUsersModel, AllUsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AllUsersViewHolder holder, int position, @NonNull AllUsersModel model) {
                holder.nameTV.setText(model.getName());
                holder.statusTV.setText(model.getStatus());

                final String user_id = getRef(position).getKey();
                Picasso.get().load(model.getImage()).placeholder(R.drawable.default_gravatar).into(holder.imageView);

                holder.cLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("user_id", user_id);
                            startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public AllUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mContext = parent.getContext();
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_layout, parent, false);

                return new AllUsersViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
            }
        };

        mRecylcerView.setAdapter(mAdapter);
    }

    class AllUsersViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, statusTV;
        CircleImageView imageView;
        ConstraintLayout cLayout;

        public AllUsersViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = (TextView) itemView.findViewById(R.id.allusername);
            statusTV = (TextView) itemView.findViewById(R.id.alluserstatus);
            imageView = (CircleImageView) itemView.findViewById(R.id.alluserimage);
            cLayout = (ConstraintLayout) itemView.findViewById(R.id.alluserlayout);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserRef.child("online").setValue("true");
        mAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }



    @Override
    protected void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);
        super.onPause();
    }

}
