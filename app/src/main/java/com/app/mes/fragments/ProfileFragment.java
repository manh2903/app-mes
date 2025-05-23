package com.app.mes.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.mes.R;
import com.app.mes.databinding.FragmentProfileBinding;
import com.app.mes.helper.UploadHelper;
import com.app.mes.models.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ValueEventListener userListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        setupImagePicker();
        loadUserProfile();

        binding.btnUpdate.setOnClickListener(v -> updateProfile());
        binding.btnChangeImage.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadImage(imageUri);
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImage(Uri imageUri) {
        if (auth.getCurrentUser() != null) {
            UploadHelper.uploadImage(imageUri, "profile_images", auth.getCurrentUser().getUid(), new UploadHelper.UploadCallbackListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    databaseReference.child(auth.getCurrentUser().getUid())
                            .child("avatar")
                            .setValue(imageUrl)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUserProfile() {
        if (auth.getCurrentUser() != null) {
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding != null && isAdded()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            binding.name.setText(user.getName());
                            binding.status.setText(user.getStatus());
                            
                            // Load avatar using Glide
                            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                                Glide.with(requireContext())
                                        .load(user.getAvatar())
                                        .placeholder(R.drawable.default_avatar)
                                        .error(R.drawable.default_avatar)
                                        .circleCrop()
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                Log.e("Glide", "Load failed: " + e.getMessage());
                                                if (e != null) {
                                                    for (Throwable t : e.getRootCauses()) {
                                                        Log.e("Glide", "Caused by: " + t.getMessage());
                                                    }
                                                }
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                Log.d("Glide", "Load successful");
                                                return false;
                                            }
                                        })
                                        .into(binding.profileImage);
                            } else {
                                binding.profileImage.setImageResource(R.drawable.default_avatar);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            };
            databaseReference.child(auth.getCurrentUser().getUid())
                    .addValueEventListener(userListener);
        }
    }

    private void updateProfile() {
        if (auth.getCurrentUser() != null && binding != null && isAdded()) {
            String name = binding.name.getText().toString();
            String status = binding.status.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseReference.child(auth.getCurrentUser().getUid())
                    .child("name")
                    .setValue(name);
            databaseReference.child(auth.getCurrentUser().getUid())
                    .child("status")
                    .setValue(status)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (auth.getCurrentUser() != null && userListener != null) {
            databaseReference.child(auth.getCurrentUser().getUid())
                    .removeEventListener(userListener);
        }
        binding = null;
    }
} 