package org.openthos.filemanager;

import android.app.Activity;

import org.openthos.filemanager.bean.Mode;

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

    @Override
    protected void setMode() {
        mMode = Mode.PICK;
    }
}
