package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.models.User;
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

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.name.setText(user.name);
        if (user.role.equals("admin")) {
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.yellow));
        }

        holder.email.setText(user.email);
        holder.phone.setText(user.phone);

        if (user.avatarUrl != null)
            Picasso.get().load(user.avatarUrl).into(holder.avatar);

        if (user.role.equals("user")) {
            holder.adminIndicator.setVisibility(View.GONE);
        }
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


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            adminIndicator = itemView.findViewById(R.id.adminIndicator);
            avatar = itemView.findViewById(R.id.userAvatar);
            name = itemView.findViewById(R.id.nameTextView);
            email = itemView.findViewById(R.id.emailTextView);
            phone = itemView.findViewById(R.id.phoneTextView);
        }
    }
}
