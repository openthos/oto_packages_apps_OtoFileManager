package org.openthos.filemanager;

import android.os.Environment;

import org.openthos.filemanager.bean.Mode;

/**
 * Created by root on 7/13/17.
 */

public class SetWallpaperActivity extends PickerActivity {
    @Override
    protected void setMode() {
        mMode = Mode.SETWALLPAPER;
    }

    @Override
    public void onBackPressed() {
        if (mCurFragment == mSdStorageFragment) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void initFirstPage() {
        String path = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        showSpaceFragment(path);
        if (isCollectedFolderPath(path)) {
            setSelectedBackground(getLeftTagAndViewIdMap().get(path));
        }
        setCurPath(path);
    }
}
