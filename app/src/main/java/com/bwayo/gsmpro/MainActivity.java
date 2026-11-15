package com.bwayo.gsmpro;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.HashMap;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView usbStatus, fileStatus, logView, chipsetStatus;
    private String selectedChipset = "MediaTek (MTK)";
    private String selectedFileName = "No firmware selected";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        refreshUsb();
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(dp(4), dp(5), dp(4), dp(5));
        return t;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(24));
        root.setBackgroundColor(0xFF07111F);
        scroll.addView(root);
        setContentView(scroll);

        TextView title = text("BWAYO GSM PRO", 26, 0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, 1);
        root.addView(title);
        TextView subtitle = text("ANDROID PHONE SERVICE TOOLKIT", 12, 0xFF8FB7E8);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);
        addGap();

        addSection("USB OTG CONNECTION");
        usbStatus = text("Checking USB devices...", 14, 0xFFFFFFFF);
        addCard(usbStatus);
        addButton("REFRESH USB CONNECTION", v -> refreshUsb());

        addSection("CHIPSET FAMILY");
        Spinner chips = new Spinner(this);
        String[] options = {"MediaTek (MTK)", "Qualcomm", "Unisoc (SPD)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chips.setAdapter(adapter);
        chips.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                selectedChipset = options[pos];
                chipsetStatus.setText("Selected: " + selectedChipset);
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        addCard(chips);
        chipsetStatus = text("Selected: " + selectedChipset, 13, 0xFFB8CBE0);
        root.addView(chipsetStatus);

        addSection("FIRMWARE FROM USB");
        fileStatus = text(selectedFileName, 14, 0xFFFFFFFF);
        addCard(fileStatus);
        addButton("SELECT FIRMWARE FILE", v -> chooseFirmware());
        addButton("CHECK FILE DETAILS", v -> {
            if ("No firmware selected".equals(selectedFileName)) {
                log("Select a firmware file first.");
            } else {
                log("Selected file: " + selectedFileName + "\nChipset chosen: " + selectedChipset +
                    "\nBasic file selection completed. Signature and device compatibility are not verified.");
            }
        });

        addSection("SERVICE TOOLS");
        addButton("DEVICE INFORMATION", v -> showDeviceInfo());
        addButton("ADB / FASTBOOT INFORMATION", v ->
            log("ADB and Fastboot require an authorized host connection and supported device mode. Command execution is not included in this starter."));
        addButton("AUTHORIZED FRP RECOVERY", v ->
            log("Use the manufacturer's official account-recovery process or an authorized service center. This app does not bypass Factory Reset Protection."));
        addButton("RECOVERY / REBOOT GUIDE", v ->
            log("Recovery and reboot key combinations vary by model. Follow the manufacturer's instructions for your exact device."));

        addSection("FLASHING STATUS");
        addCard(text("Flashing engine: NOT INSTALLED\nNo erase/write operation is implemented. A validated chipset-specific engine and model-specific testing are required.", 13, 0xFFFFD38A));
        addSection("ACTIVITY LOG");
        logView = text("BWAYO GSM PRO ready.", 13, 0xFFB8CBE0);
        addCard(logView);
        TextView footer = text("BWAYO GSM PRO • Version 1.0 starter", 11, 0xFF7188A5);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
    }

    private void addGap() {
        View gap = new View(this);
        root.addView(gap, new LinearLayout.LayoutParams(1, dp(12)));
    }
    private void addSection(String name) {
        TextView t = text(name, 14, 0xFF5DA5FF);
        t.setTypeface(null, 1);
        t.setPadding(0, dp(16), 0, dp(7));
        root.addView(t);
    }
    private void addCard(View child) {
        LinearLayout card = new LinearLayout(this);
        card.setPadding(dp(12), dp(9), dp(12), dp(9));
        card.setBackgroundColor(0xFF14243A);
        card.addView(child);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(8);
        root.addView(card, lp);
    }
    private void addButton(String label, View.OnClickListener action) {
        Button b = new Button(this);
        b.setText(label); b.setTextColor(0xFFFFFFFF); b.setAllCaps(false);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1769C2));
        b.setOnClickListener(action);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(7);
        root.addView(b, lp);
    }
    private void refreshUsb() {
        UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        if (devices == null || devices.isEmpty()) {
            usbStatus.setText("No USB device detected. Connect a USB OTG adapter and device, then refresh.");
        } else {
            StringBuilder s = new StringBuilder("Detected USB device(s):\n");
            for (UsbDevice d : devices.values()) {
                s.append("• ").append(d.getDeviceName()).append("\n  Vendor ID: ")
                 .append(d.getVendorId()).append("  Product ID: ").append(d.getProductId()).append("\n");
            }
            usbStatus.setText(s.toString());
        }
        log("USB list refreshed.");
    }
    private void chooseFirmware() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 101);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {}
                selectedFileName = getDisplayName(uri);
                fileStatus.setText("Selected: " + selectedFileName);
                log("Firmware file selected. Confirm it is from your USB drive and matches the exact phone model.");
            }
        }
    }
    private String getDisplayName(Uri uri) {
        String name = uri.getLastPathSegment();
        Cursor c = getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (i >= 0) name = c.getString(i);
                }
            } finally { c.close(); }
        }
        return name == null ? "Selected firmware file" : name;
    }
    private void showDeviceInfo() {
        log("Manufacturer: " + android.os.Build.MANUFACTURER +
            "\nModel: " + android.os.Build.MODEL +
            "\nAndroid: " + android.os.Build.VERSION.RELEASE +
            "\nSDK: " + android.os.Build.VERSION.SDK_INT +
            "\nDevice: " + android.os.Build.DEVICE);
    }
    private void log(String message) {
        if (logView != null) logView.setText(message);
    }
}
