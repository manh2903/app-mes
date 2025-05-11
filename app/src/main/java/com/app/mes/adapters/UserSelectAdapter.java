package com.app.mes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mes.R;
import com.app.mes.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSelectAdapter extends RecyclerView.Adapter<UserSelectAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private Set<String> selectedUserIds;

    public UserSelectAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.selectedUserIds = new HashSet<>();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_select, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.txtName.setText(user.getName());
        holder.checkBox.setChecked(selectedUserIds.contains(user.getUid()));
        holder.itemView.setOnClickListener(v -> {
            boolean checked = !holder.checkBox.isChecked();
            holder.checkBox.setChecked(checked);
            if (checked) {
                selectedUserIds.add(user.getUid());
            } else {
                selectedUserIds.remove(user.getUid());
            }
        });
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUserIds.add(user.getUid());
            } else {
                selectedUserIds.remove(user.getUid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<String> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        CheckBox checkBox;
        UserViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
} 