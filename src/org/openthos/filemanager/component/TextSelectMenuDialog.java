package org.openthos.filemanager.component;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.FileProvider;

import org.openthos.filemanager.BaseMenuDialog;
import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.adapter.BaseDialogAdapter;
import org.openthos.filemanager.utils.Constants;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class TextSelectMenuDialog extends BaseMenuDialog implements AdapterView.OnItemClickListener {
    private String filePath;

    public TextSelectMenuDialog(Context context, String filePath) {
        super(context);
        mActivity = (MainActivity) context;
        this.filePath = filePath;
    }

    @Override
    protected void initData() {
        mDatas = new ArrayList();
        String[] sArr = mActivity.getResources().getStringArray(R.array.text_select_menu);
        for (int i = 0; i < sArr.length; i++) {
            mDatas.add(sArr[i]);
        }
        mListView.setAdapter(new BaseDialogAdapter(getContext(),
                mDatas, mFileViewInteractionHub));
    }

    @Override
    protected void initListener() {
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selectType = "";
        String content = (String) view.getTag();
        if (mActivity.getString(R.string.dialog_type_text).equals(content)) {
            selectType = "text/plain";
        } else if (mActivity.getString(R.string.dialog_type_audio).equals(content)) {
            selectType = "audio/*";
        } else if (mActivity.getString(R.string.dialog_type_image).equals(content)) {
            selectType = "image/*";
        } else if (mActivity.getString(R.string.dialog_type_video).equals(content)) {
            selectType = "video/*";
        }
        List<ResolveInfo> resolveInfoList = new ArrayList<>();
        PackageManager manager = mActivity.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(mActivity,
                    "org.openthos.support.filemanager.fileprovider", new File(filePath));
            intent.setDataAndType(uri, selectType);
        } else {
            uri = Uri.fromFile(new File(filePath));
            intent.setDataAndType(uri, selectType);
        }
        resolveInfoList = manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        dismiss();
        if (resolveInfoList.size() > 0) {
            if (android.os.Build.VERSION.SDK_INT >= 24) {
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //for (ResolveInfo resolveInfo : resolveInfoList) {
                //    String packageName = resolveInfo.activityInfo.packageName;
                //    mActivity.grantUriPermission(packageName, uri,
                //            Intent.FLAG_GRANT_READ_URI_PERMISSION
                //            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //}
            }
            intent.putExtra(Constants.PACKAGENAME_TAG, Constants.APPNAME_OTO_LAUNCHER);
            mActivity.startActivity(intent);
        } else {
            OpenWithDialog openWithDialog = new OpenWithDialog(mActivity, filePath, selectType);
            openWithDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            openWithDialog.showDialog();
        }
    }
}
