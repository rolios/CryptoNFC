package com.roly.nfc.crypto.util;

import android.nfc.NdefRecord;
import android.os.Build;

public class NfcTagUtils {

    public static NdefRecord writeApplicationRecordIfPossible(){
        if(Build.VERSION_CODES.ICE_CREAM_SANDWICH <= Build.VERSION.SDK_INT)
            return NdefRecord.createApplicationRecord("com.roly.nfc.crypto");
        return null;
    }

}
