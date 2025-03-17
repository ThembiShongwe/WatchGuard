package com.example.watchguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashBoard extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_board);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        welcomeText = findViewById(R.id.welcomeText);

        loadUserName();

        setNavigation();
    }

    private void loadUserName() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    welcomeText.setText("Welcome, " + name + "!");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void setNavigation() {
        findViewById(R.id.cardFullScan).setOnClickListener(view -> openActivity(FullScan.class));
        findViewById(R.id.cardSmartScan).setOnClickListener(view -> openActivity(SmartScan.class));
        findViewById(R.id.cardWifiSecurity).setOnClickListener(view -> openActivity(WifiSecurity.class));
        findViewById(R.id.cardAppUpdate).setOnClickListener(view -> openActivity(AppUpdate.class));
        findViewById(R.id.cardLearningCentre).setOnClickListener(view -> openActivity(LearningCentre.class));
        findViewById(R.id.cardFireWall).setOnClickListener(view -> openActivity(FireWall.class));
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
}
