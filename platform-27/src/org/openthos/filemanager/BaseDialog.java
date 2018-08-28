package org.openthos.filemanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.openthos.filemanager.system.FileViewInteractionHub;

import java.util.ArrayList;

/**
 * Created by wang on 17-3-9.
 */

public abstract class BaseDialog extends Dialog {

    public BaseDialog(@NonNull Context context) {
        super(context);
    }

    public BaseDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
    }

    public BaseDialog(@NonNull Context context, boolean cancelable,
                          @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void showDialog(int x, int y) {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        show();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        lp.x = x;
        lp.y = y;
        dialogWindow.setAttributes(lp);
    }

    public void showDialog() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.dimAmount = 0.0f;
        show();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
    }
}
