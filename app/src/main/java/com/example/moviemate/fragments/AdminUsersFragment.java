package com.example.moviemate.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.UserAdapter;
import com.example.moviemate.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {
    private List<User> users;
    private UserAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        users = new ArrayList<>();

        adapter = new UserAdapter(getContext(), users);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        RecyclerView recyclerView = view.findViewById(R.id.usersRecyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        Thread thread = new Thread(this::loadUsers);
        thread.start();

        return view;
    }

    private void loadUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                int activeUsers = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user == null)
                        continue;

                    users.add(user);

                    if (!user.isBanned)
                        activeUsers++;
                }

                View v = getView();
                if (v != null) {
                    TextView totalUsersTextView = v.findViewById(R.id.totalUserTextView);
                    totalUsersTextView.setText(String.valueOf(users.size()));
                    TextView activeUsersTextView = v.findViewById(R.id.totalActiveUserTextView);
                    activeUsersTextView.setText(String.valueOf(activeUsers));
                    TextView bannedUsersTextView = v.findViewById(R.id.totalBannedUserTextView);
                    bannedUsersTextView.setText(String.valueOf(users.size() - activeUsers));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminUsersFragment", "Failed to read value.", error.toException());
            }
        });
    }
}