package com.roly.nfc.crypto.util;

import android.nfc.NdefRecord;
import android.os.Build;

public class NfcTagUtils {

    public static void writeApplicationRecordIfPossible(NdefRecord record){
        if(Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT)
            record = NdefRecord.createApplicationRecord("com.roly.nfc.crypto");
    }

}
