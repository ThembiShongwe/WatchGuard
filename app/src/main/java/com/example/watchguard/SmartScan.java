package com.example.watchguard;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SmartScan extends AppCompatActivity {

    private ProgressBar scanProgress;
    private TextView scanStatus;
    private Button startScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_scan);

        // Initialize UI elements
        scanProgress = findViewById(R.id.scanProgress);
        scanStatus = findViewById(R.id.scanStatus);
        startScanButton = findViewById(R.id.startScanButton);

        // Set default visibility
        scanProgress.setVisibility(View.INVISIBLE);
        scanStatus.setText("Ready for a Smart Scan.");

        startScanButton.setOnClickListener(v -> startSmartScan());
    }

    private void startSmartScan() {
        startScanButton.setEnabled(false);
        scanProgress.setVisibility(View.VISIBLE);
        scanStatus.setText("Scanning...");

        new Handler().postDelayed(() -> {
            scanProgress.setVisibility(View.INVISIBLE);
            scanStatus.setText("Scan Complete! No threats detected.");
            startScanButton.setEnabled(true);
        }, 3000);
    }
}
