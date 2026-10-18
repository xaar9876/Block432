package com.norman.block432;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int REQ_ROLE = 1;
    private static final int REQ_CONTACTS = 2;

    private TextView status;
    private Button setupButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(BlockerService.PREFS, MODE_PRIVATE);

        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad * 2, pad, pad);

        TextView title = new TextView(this);
        title.setText("Block 432");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Rejects calls from the 432 area code unless the number is saved in your contacts.");
        desc.setTextSize(16);
        desc.setPadding(0, pad / 2, 0, pad);
        root.addView(desc);

        Switch toggle = new Switch(this);
        toggle.setText("Blocking on");
        toggle.setTextSize(18);
        toggle.setChecked(prefs.getBoolean(BlockerService.KEY_ENABLED, true));
        toggle.setOnCheckedChangeListener((b, on) ->
                prefs.edit().putBoolean(BlockerService.KEY_ENABLED, on).apply());
        root.addView(toggle);

        setupButton = new Button(this);
        setupButton.setText("Finish setup");
        setupButton.setOnClickListener(v -> requestSetup());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = pad;
        root.addView(setupButton, lp);

        status = new TextView(this);
        status.setTextSize(15);
        status.setPadding(0, pad, 0, 0);
        status.setGravity(Gravity.START);
        root.addView(status);

        setContentView(root);
        requestSetup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private boolean hasRole() {
        RoleManager rm = getSystemService(RoleManager.class);
        return rm != null && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
    }

    private boolean hasContacts() {
        return checkSelfPermission(Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSetup() {
        if (!hasContacts()) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQ_CONTACTS);
        } else if (!hasRole()) {
            RoleManager rm = getSystemService(RoleManager.class);
            Intent i = rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
            startActivityForResult(i, REQ_ROLE);
        }
        refresh();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        requestSetup();
    }

    @Override
    protected void onActivityResult(int code, int result, Intent data) {
        super.onActivityResult(code, result, data);
        refresh();
    }

    private void refresh() {
        boolean ready = hasRole() && hasContacts();
        setupButton.setVisibility(ready ? Button.GONE : Button.VISIBLE);

        StringBuilder sb = new StringBuilder();
        sb.append(ready ? "✅ Active — screening calls\n\n"
                : "⚠️ Setup needed:\n"
                + (hasContacts() ? "" : " • Allow Contacts access\n")
                + (hasRole() ? "" : " • Set Block 432 as your call screening app\n")
                + "\n");
        sb.append("Calls blocked: ").append(prefs.getInt(BlockerService.KEY_COUNT, 0));
        String last = prefs.getString(BlockerService.KEY_LAST, null);
        if (last != null) sb.append("\nLast blocked: ").append(last);
        status.setText(sb.toString());
    }
}
