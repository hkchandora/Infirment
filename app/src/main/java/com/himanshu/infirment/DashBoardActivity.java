package com.himanshu.infirment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.himanshu.infirment.fragment.HomeFragment;
import com.himanshu.infirment.fragment.ImageFragment;
import com.himanshu.infirment.fragment.VideoFragment;

public class DashBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                                    .commit();
                            break;
                        case R.id.nav_image:
                            selectedFragment = new ImageFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                                    .commit();
                            break;
                        case R.id.nav_video:
                            selectedFragment = new VideoFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                                    .commit();
                            break;
                        case R.id.nav_logOut:
                            logOut();
                            break;
                    }
                    return true;
                }
            };

    private void logOut() {

        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        GoogleSignIn.getClient(
                                getApplicationContext(),
                                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        ).signOut();
                        Intent i4 = new Intent(getApplicationContext(), SignUpActivity.class);
                        i4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i4);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}