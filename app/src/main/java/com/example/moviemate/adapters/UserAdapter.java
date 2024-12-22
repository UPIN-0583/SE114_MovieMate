package com.example.moviemate.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.models.User;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);

        return new UserViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.name.setText(user.name);
        if (user.role.equals("admin")) {
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.yellow));
        }

        holder.email.setText(user.email);

        if (user.phone == null || user.phone.isEmpty()) {
            holder.phone.setText("Not set yet");
        } else {
            holder.phone.setText(user.phone);
        }

        if (user.avatarUrl != null)
            Picasso.get().load(user.avatarUrl).into(holder.avatar);

        if (user.role.equals("user")) {
            holder.adminIndicator.setVisibility(View.GONE);
        }
        else {
            holder.adminIndicator.setVisibility(View.VISIBLE);
        }

        if (user.isBanned) {
            holder.bannedIndicator.setVisibility(View.VISIBLE);
            holder.activeIndicator.setVisibility(View.GONE);
            holder.banUnbanButton.setImageResource(R.drawable.ic_unban);
        } else {
            holder.bannedIndicator.setVisibility(View.GONE);
            holder.activeIndicator.setVisibility(View.VISIBLE);
            holder.banUnbanButton.setImageResource(R.drawable.ic_ban);
        }

        holder.banUnbanButton.setOnClickListener(v -> {
            // Đảo ngược trạng thái banned của user
            FirebaseDatabase.getInstance().getReference("Users").child(user.id).child("isBanned").setValue(!user.isBanned);
        });
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageView adminIndicator;
        private final CircleImageView avatar;
        private final TextView name;
        private final TextView email;
        private final TextView phone;
        private TextView activeIndicator;
        private TextView bannedIndicator;
        private ImageButton banUnbanButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            adminIndicator = itemView.findViewById(R.id.adminIndicator);
            avatar = itemView.findViewById(R.id.userAvatar);
            name = itemView.findViewById(R.id.nameTextView);
            email = itemView.findViewById(R.id.emailTextView);
            phone = itemView.findViewById(R.id.phoneTextView);
            activeIndicator = itemView.findViewById(R.id.activeStateTextView);
            bannedIndicator = itemView.findViewById(R.id.bannedStateTextView);
            banUnbanButton = itemView.findViewById(R.id.banUnbanImgBtn);
        }
    }
}
