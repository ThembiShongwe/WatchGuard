package com.example.watchguard;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FullScan extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvScanTitle;
    private RecyclerView recyclerViewResults;
    private Button btnFixIssues;
    private ScanResultsAdapter adapter;
    private List<String> vulnerabilities = new ArrayList<>();

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

        new FullDeviceScanTask().execute();
    }

    private class FullDeviceScanTask extends AsyncTask<Void, Integer, List<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvScanTitle.setText("Scanning device...");
            progressBar.setVisibility(ProgressBar.VISIBLE);
            recyclerViewResults.setVisibility(RecyclerView.GONE);
            btnFixIssues.setVisibility(Button.GONE);
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            scanInstalledApps();
            checkWifiSecurity();
            return vulnerabilities;
        }

        @Override
        protected void onPostExecute(List<String> results) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerViewResults.setVisibility(RecyclerView.VISIBLE);
            btnFixIssues.setVisibility(Button.VISIBLE);

            tvScanTitle.setText("Scan Completed: " + results.size() + " issues found");
            adapter.notifyDataSetChanged();
        }
    }

    private void scanInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : installedApps) {
            // Check for system apps and ignore them
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS);
                    if (packageInfo.requestedPermissions != null) {
                        for (String permission : packageInfo.requestedPermissions) {
                            if (permission.equals("android.permission.READ_SMS") ||
                                    permission.equals("android.permission.READ_CONTACTS")) {
                                vulnerabilities.add("App " + app.loadLabel(pm) + " has risky permissions!");
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("ScanError", "Error scanning app: " + e.getMessage());
                }
            }
        }
    }

    private void checkWifiSecurity() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null && wifiManager.getConnectionInfo().getSSID() != null) {
            if (!wifiManager.getConnectionInfo().getSSID().contains("WPA") &&
                    !wifiManager.getConnectionInfo().getSSID().contains("WPA2")) {
                vulnerabilities.add("WiFi is not secure. Use WPA2 encryption!");
            }
        }
    }
}
