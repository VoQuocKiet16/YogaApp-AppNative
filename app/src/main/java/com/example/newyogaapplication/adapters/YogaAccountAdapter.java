package com.example.newyogaapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.Role;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.database.YogaUserDB;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class YogaAccountAdapter extends RecyclerView.Adapter<YogaAccountAdapter.AccountViewHolder> {

    private List<YogaUser> userList;
    private Context context;
    private YogaUserDB userDbHelper;
    private DatabaseReference userRef;
    private String loggedInUserEmail;

    // Constructor cho adapter
    public YogaAccountAdapter(Context context, List<YogaUser> userList, YogaUserDB userDbHelper, DatabaseReference userRef, String loggedInUserEmail) {
        this.context = context;
        this.userList = userList;
        this.userDbHelper = userDbHelper;
        this.userRef = userRef;
        this.loggedInUserEmail = loggedInUserEmail;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_yoga_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        YogaUser user = userList.get(position);


        if (!user.getEmail().equals(loggedInUserEmail)) {
            holder.tvUserName.setText(user.getUsername());
            holder.tvUserEmail.setText(user.getEmail());
            holder.tvUserRole.setText(user.getRole().name());

            holder.imgEdit.setOnClickListener(v -> showEditDialog(user, position));
            holder.imgSetRole.setOnClickListener(v -> showSetRoleDialog(user, position));
        } else {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserRole;
        ImageView imgEdit, imgDelete, imgSetRole;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgSetRole = itemView.findViewById(R.id.imgSetRole);
        }
    }

    private void showEditDialog(YogaUser user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit User");

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_user, null);
        builder.setView(dialogView);

        EditText etUsername = dialogView.findViewById(R.id.etUsername);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Set existing data
        etUsername.setText(user.getUsername());
        etEmail.setText(user.getEmail());

        // Save button click listener
        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email already exists
            if (userDbHelper.isEmailExists(newEmail, user.getUserId())) {
                Toast.makeText(context, "Email already in use, please choose another one", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update user details
            user.setUsername(newUsername);
            user.setEmail(newEmail);

            // Update in SQLite
            int rowsAffected = userDbHelper.updateUser(user);
            if (rowsAffected > 0) {
                // Update in Firebase
                userRef.child(user.getFirebaseKey()).setValue(user).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.set(position, user);
                        notifyItemChanged(position);
                        Toast.makeText(context, "User updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to update user in Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Failed to update user in SQLite", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private void showSetRoleDialog(YogaUser user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set User Role");

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_set_role, null);
        builder.setView(dialogView);

        Spinner spinnerRoles = dialogView.findViewById(R.id.spinnerRoles);
        Button btnSetRole = dialogView.findViewById(R.id.btnSetRole);

        // Populate spinner with role options
        ArrayAdapter<Role> roleAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, Role.values());
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoles.setAdapter(roleAdapter);

        // Set the current role
        spinnerRoles.setSelection(user.getRole().ordinal());

        // Set Role button click listener
        btnSetRole.setOnClickListener(v -> {
            Role selectedRole = (Role) spinnerRoles.getSelectedItem();

            if (selectedRole != user.getRole()) {
                // Update the user's role
                user.setRole(selectedRole);

                // Update in SQLite
                int rowsAffected = userDbHelper.updateUser(user);
                if (rowsAffected > 0) {
                    // Update in Firebase
                    userRef.child(user.getFirebaseKey()).setValue(user).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userList.set(position, user);
                            notifyItemChanged(position);
                            Toast.makeText(context, "User role updated successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to update user role in Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Failed to update user role in SQLite", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No changes to the role", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
