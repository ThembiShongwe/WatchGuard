package com.example.watchguard;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FullScan extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvScanTitle;
    private RecyclerView recyclerViewResults;
    private Button btnFixIssues;
    private ScanResultsAdapter adapter;
    private List<String> vulnerabilities = new ArrayList<>();

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_scan);

        progressBar = findViewById(R.id.progressBar);
        tvScanTitle = findViewById(R.id.tvScanTitle);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        btnFixIssues = findViewById(R.id.btnFixIssues);

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScanResultsAdapter(vulnerabilities);
        recyclerViewResults.setAdapter(adapter);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        startFullScan();
    }

    private void startFullScan() {
        tvScanTitle.setText("Scanning device...");
        progressBar.setVisibility(ProgressBar.VISIBLE);
        recyclerViewResults.setVisibility(RecyclerView.GONE);
        btnFixIssues.setVisibility(Button.GONE);

        executorService.execute(() -> {
            scanInstalledApps();
            checkWifiSecurity();

            // Once scanning is complete, update UI on the main thread
            mainHandler.post(() -> {
                progressBar.setVisibility(ProgressBar.GONE);
                recyclerViewResults.setVisibility(RecyclerView.VISIBLE);
                btnFixIssues.setVisibility(Button.VISIBLE);

                tvScanTitle.setText("Scan Completed: " + vulnerabilities.size() + " issues found");
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void scanInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : installedApps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);

                    // Checking for dangerous permissions
                    if (packageInfo.requestedPermissions != null) {
                        for (String permission : packageInfo.requestedPermissions) {
                            if (permission.equals("android.permission.READ_SMS") ||
                                    permission.equals("android.permission.READ_CONTACTS") ||
                                    permission.equals("android.permission.RECORD_AUDIO") ||
                                    permission.equals("android.permission.CAMERA")) {
                                vulnerabilities.add("App " + app.loadLabel(pm) + " has risky permissions!");
                            }
                        }
                    }

                    // Checking for known malware signatures
                    if (isMaliciousApp(packageInfo)) {
                        vulnerabilities.add("Potential malware detected: " + app.loadLabel(pm));
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("ScanError", "Error scanning app: " + e.getMessage());
                }
            }
        }
    }

    private boolean isMaliciousApp(PackageInfo packageInfo) {
        // Known malware hashes (example list)
        String[] knownMalwareHashes = {
                "6dcd4ce23d88e2ee956f8dfc41b26d5b",
                "fd469dbac7d2b8f7ddcd6fe4a3a69d5a"
        };

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(packageInfo.packageName.getBytes());

            StringBuilder hashString = new StringBuilder();
            for (byte b : digest) {
                hashString.append(String.format("%02x", b));
            }

            for (String malwareHash : knownMalwareHashes) {
                if (hashString.toString().equals(malwareHash)) {
                    return true;
                }
            }

        } catch (NoSuchAlgorithmException e) {
            Log.e("MalwareScan", "Error generating hash: " + e.getMessage());
        }
        return false;
    }

    private void checkWifiSecurity() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String ssid = wifiInfo.getSSID();
                int securityType = wifiInfo.getNetworkId();

                if (!ssid.contains("WPA") && !ssid.contains("WPA2") && securityType != 0) {
                    vulnerabilities.add("WiFi is not secure. Use WPA2 encryption!");
                }
            }
        }
    }
}
