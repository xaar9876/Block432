package com.norman.block432;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallScreeningService;

/** Rejects incoming calls from area code 432 unless the number is a saved contact. */
public class BlockerService extends CallScreeningService {

    static final String PREFS = "block432";
    static final String KEY_ENABLED = "enabled";
    static final String KEY_COUNT = "blocked_count";
    static final String KEY_LAST = "last_blocked";

    @Override
    public void onScreenCall(Call.Details details) {
        String number = details.getHandle() != null
                ? details.getHandle().getSchemeSpecificPart() : null;

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_ENABLED, true);

        boolean block = enabled
                && details.getCallDirection() == Call.Details.DIRECTION_INCOMING
                && is432(number)
                && !isSavedContact(number);

        CallResponse.Builder response = new CallResponse.Builder();
        if (block) {
            response.setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipNotification(true)
                    .setSkipCallLog(false); // still shows in call log so you can check later
            prefs.edit()
                    .putInt(KEY_COUNT, prefs.getInt(KEY_COUNT, 0) + 1)
                    .putString(KEY_LAST, number)
                    .apply();
        }
        respondToCall(details, response.build());
    }

    /** True for numbers like 432-555-1234, +1 432 555 1234, 14325551234. */
    static boolean is432(String number) {
        if (number == null) return false;
        String digits = number.replaceAll("[^0-9]", "");
        if (digits.length() == 11 && digits.startsWith("1")) digits = digits.substring(1);
        return digits.length() == 10 && digits.startsWith("432");
    }

    private boolean isSavedContact(String number) {
        // If we can't read contacts, fail safe and let the call through.
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        try (Cursor c = getContentResolver().query(
                uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null)) {
            return c != null && c.getCount() > 0;
        } catch (Exception e) {
            return true; // on any error, don't block
        }
    }
}
