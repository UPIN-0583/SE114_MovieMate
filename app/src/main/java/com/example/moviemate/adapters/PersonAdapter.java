package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.models.Person;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.PersonViewHolder> {

    private Context context;
    private List<Person> personList;
    private boolean showRemoveButton;

    public PersonAdapter(Context context, List<Person> personList, boolean showRemoveButton) {
        this.context = context;
        this.personList = personList;
        this.showRemoveButton = showRemoveButton;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.person_item, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        Person person = personList.get(position);
        holder.personName.setText(person.getName());
        Picasso.get().load(person.getPicUrl()).into(holder.personImage);

        if (showRemoveButton) {
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(v -> {
                personList.remove(position);
                notifyDataSetChanged();
            });
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        TextView personName;
        CircleImageView personImage;
        ImageButton removeButton;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.person_name);
            personImage = itemView.findViewById(R.id.person_image);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
