package com.openthos.filemanager;

import android.app.Activity;

/**
 * Created by root on 12/20/16.
 */

public class PickerActivity extends MainActivity {

    @Override
    public void onBackPressed() {
        if (mCurFragment == mSdStorageFragment) {
             setResult(Activity.RESULT_OK);
             finish();
        } else {
            super.onBackPressed();
        }
    }
}
