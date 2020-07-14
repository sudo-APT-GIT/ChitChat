package com.example.chitchat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentViewHolder;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter mAdapter;

    private String mCurrent_user_id;
    private View mMainView;
    private Context mContext;
    private ShimmerFrameLayout mShimmerViewContainer;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mShimmerViewContainer = mMainView.findViewById(R.id.shimmer_view_container_users_friends);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friendsfragRV);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendsDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mFriendsList.addItemDecoration(decoration);

        FirebaseRecyclerOptions<FriendsModel> options = new FirebaseRecyclerOptions.Builder<FriendsModel>()
                .setQuery(mFriendsDatabase, FriendsModel.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull FriendsModel model) {
                    holder.statusTv.setText(model.getDate());
                    String list_user_id = getRef(position).getKey();

                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String username = snapshot.child("name").getValue().toString();
                            String userImage = snapshot.child("image").getValue().toString();
                            holder.setName(username);
                            holder.setImage(userImage);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            }


            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mContext = parent.getContext();
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_layout, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
            }
        };
        mFriendsList.setAdapter(mAdapter);
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    public void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);
        super.onPause();
    }

    private class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView statusTv;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            statusTv =(TextView) itemView.findViewById(R.id.alluserstatus);
        }

        public void setName(String name){
            TextView userNameTv = (TextView) mView.findViewById(R.id.allusername);
            userNameTv.setText(name);
        }

        public void setImage(String userImage) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.alluserimage);
            Picasso.get().load(userImage).placeholder(R.drawable.default_gravatar).into(userImageView);
        }
    }
}
