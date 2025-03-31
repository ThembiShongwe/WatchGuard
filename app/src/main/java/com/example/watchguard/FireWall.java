package com.example.watchguard;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FireWall extends AppCompatActivity {

    private ListView appListView;
    private TextView tvWarning;
    private Button btnStartVpn;
    private AppListAdapter adapter;
    private List<AppInfo> installedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_wall);

        appListView = findViewById(R.id.appListView);
        tvWarning = findViewById(R.id.tvWarning);
        btnStartVpn = findViewById(R.id.btnStartVpn);

        loadInstalledApps();
        checkSuspiciousApps();

        btnStartVpn.setOnClickListener(v -> startVpnService());
    }

    private void loadInstalledApps() {
        installedApps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                installedApps.add(new AppInfo(app.loadLabel(pm).toString(), app.packageName, app.loadIcon(pm)));
            }
        }

        adapter = new AppListAdapter(this, installedApps);
        appListView.setAdapter(adapter);
    }

    private void checkSuspiciousApps() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        StringBuilder warnings = new StringBuilder();

        for (PackageInfo pkg : packages) {
            if (pkg.requestedPermissions != null) {
                for (String permission : pkg.requestedPermissions) {
                    if (permission.equals("android.permission.SYSTEM_ALERT_WINDOW") ||
                            permission.equals("android.permission.READ_SMS") ||
                            permission.equals("android.permission.BIND_DEVICE_ADMIN")) {
                        warnings.append(pkg.packageName).append(" has high-risk permissions!\n");
                        break;
                    }
                }
            }
        }

        if (warnings.length() > 0) {
            tvWarning.setText("⚠️ Suspicious Apps Found:\n" + warnings.toString());
        } else {
            tvWarning.setText("✅ No suspicious apps detected.");
        }
    }

    private void startVpnService() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // ✅ Call super method

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "VPN Service Started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "VPN Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
