package com.app.mes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.app.mes.databinding.ActivityMainBinding;
import com.app.mes.fragments.ChatsFragment;
import com.app.mes.fragments.ProfileFragment;
import com.app.mes.fragments.UsersFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("MES");

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ChatsFragment())
                .commit();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_chats) {
                selectedFragment = new ChatsFragment();
            } else if (itemId == R.id.nav_users) {
                selectedFragment = new UsersFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        if (auth.getCurrentUser() != null) {
            // Cập nhật trạng thái offline trước
            databaseReference.child(auth.getCurrentUser().getUid())
                    .child("isOnline")
                    .setValue(false)
                    .addOnCompleteListener(task -> {
                        // Đăng xuất sau khi đã cập nhật trạng thái
                        auth.signOut();
                        // Chuyển về màn hình đăng nhập
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Lỗi khi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Nếu không có user đang đăng nhập, chuyển thẳng về màn hình đăng nhập
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void updateUserStatus(boolean online) {
        if (auth.getCurrentUser() != null) {
            databaseReference.child(auth.getCurrentUser().getUid())
                    .child("isOnline")
                    .setValue(online);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus(false);
    }
}