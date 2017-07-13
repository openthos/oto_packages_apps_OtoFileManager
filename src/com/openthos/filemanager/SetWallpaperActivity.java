package com.openthos.filemanager;

import android.os.Environment;

import com.openthos.filemanager.bean.Mode;
import com.openthos.filemanager.system.Constants;

import java.io.File;

/**
 * Created by root on 7/13/17.
 */

public class SetWallpaperActivity extends PickerActivity {
    @Override
    protected void setMode() {
        mMode = Mode.SETWALLPAPER;
    }

    @Override
    protected void initFirstPage() {
        String path = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        showSpaceFragment(path);
        setSelectedBackground(R.id.tv_picture);
        setCurPath(path);
    }
}
