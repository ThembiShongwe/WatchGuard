package com.example.watchguard;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class WifiSecurity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION = 1001;
    private TextView tvWifiStatus, tvSecurityType, tvImprovementTips;
    private Button btnScanWifi;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_security);

        tvWifiStatus = findViewById(R.id.tvWifiStatus);
        tvSecurityType = findViewById(R.id.tvSecurityType);
        tvImprovementTips = findViewById(R.id.tvImprovementTips);
        btnScanWifi = findViewById(R.id.btnScanWifi);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        btnScanWifi.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                analyzeWifiSecurity();
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            return false;
        }
        return true;
    }

    private void analyzeWifiSecurity() {
        if (!isWifiConnected()) {
            tvWifiStatus.setText("Not connected to any WiFi network.");
            tvSecurityType.setText("N/A");
            tvImprovementTips.setText("Please connect to a WiFi network to check security.");
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "");
        int networkId = wifiInfo.getNetworkId();

        String securityType = getWifiSecurityType(networkId);
        String improvementTips = getSecurityImprovementTips(securityType);

        tvWifiStatus.setText("Connected to: " + ssid);
        tvSecurityType.setText("Security Type: " + securityType);
        tvImprovementTips.setText("Security Tips:\n" + improvementTips);
    }

    private boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }

    private String getWifiSecurityType(int networkId) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "Permission required";
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            if (scanResult.SSID.equals(wifiManager.getConnectionInfo().getSSID().replace("\"", ""))) {
                String capabilities = scanResult.capabilities;

                if (capabilities.contains("WPA3")) return "WPA3 (Very Secure)";
                if (capabilities.contains("WPA2")) return "WPA2 (Secure)";
                if (capabilities.contains("WPA")) return "WPA (Moderate Security)";
                if (capabilities.contains("WEP")) return "WEP (Weak Security)";
                if (capabilities.contains("PSK")) return "Pre-Shared Key (Common Security)";
                return "Open Network (Not Secure)";
            }
        }
        return "Unknown Security Type";
    }

    private String getSecurityImprovementTips(String securityType) {
        switch (securityType) {
            case "WPA3 (Very Secure)":
                return "Your WiFi is very secure. Keep your router firmware updated.";
            case "WPA2 (Secure)":
                return "Good security! If available, upgrade to WPA3 for better protection.";
            case "WPA (Moderate Security)":
                return "Consider upgrading to WPA2 or WPA3 for better encryption.";
            case "WEP (Weak Security)":
                return "WEP is outdated and highly vulnerable. Switch to WPA2 or WPA3 immediately.";
            case "Open Network (Not Secure)":
                return "Your network is open to anyone! Set a strong password and enable WPA2/WPA3.";
            default:
                return "Make sure your router settings are updated and use a strong password.";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                analyzeWifiSecurity();
            } else {
                tvWifiStatus.setText("Permission denied. Cannot analyze WiFi security.");
            }
        }
    }
}
