package org.openthos.filemanager.component;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import org.openthos.filemanager.MainActivity;
import org.openthos.filemanager.R;
import org.openthos.filemanager.utils.OperateUtils;
import org.openthos.filemanager.utils.SambaUtils;

import java.util.ArrayList;
import java.util.List;

public class UserManagementDialog extends Dialog implements View.OnClickListener{

    private Context mContext;
    private TextView mTvUserList;
    private ListView mLvUserList;
    private TextView mTvAddUser;
    private List<String> mUserlist;
    private UserListAdapter mUserListAdapter;

    public UserManagementDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_user_management);
        init();
    }

    private void init() {
        mTvUserList = (TextView) findViewById(R.id.tv_user_list);
        mLvUserList = (ListView) findViewById(R.id.lv_user_list);
        mTvAddUser = (TextView) findViewById(R.id.tv_add_user);
        mTvAddUser.setOnClickListener(this);

        // get user list info
        mUserlist = SambaUtils.getAllUsers();
        mUserListAdapter = new UserListAdapter();
        mLvUserList.setAdapter(mUserListAdapter);
    }

    public void notifyUserInfoChanged() {
        mUserlist = SambaUtils.getAllUsers();
        mUserListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_delete_user:
                final String userRemoved = (String) v.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(String.format(mContext.getResources().getString(
                        R.string.dialog_confirm_delete_user), userRemoved));
                builder.setPositiveButton(
                        mContext.getResources().getString(R.string.dialog_delete_yes),
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SambaUtils.removeUser(userRemoved);
                                notifyUserInfoChanged();
                            }
                });
                builder.setNegativeButton(
                        mContext.getResources().getString(R.string.dialog_delete_no), null);
                builder.create().show();
                break;
            case R.id.iv_modify_passwd:
                String userModified = (String) v.getTag();
                ModifyPasswdDialog modifyPasswdDialog =
                        new ModifyPasswdDialog(mContext, userModified);
                modifyPasswdDialog.showDialog();
                break;
            case R.id.tv_add_user:
                AddUsersDialog addUsersDialog = new AddUsersDialog(mContext, this);
                addUsersDialog.showDialog();
                break;
        }
    }

    private class UserListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUserlist.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.dialog_user_list_item, null);
                UserListViewHolder viewHolder = new UserListViewHolder(convertView);
            }
            UserListViewHolder viewHolder = (UserListViewHolder) convertView.getTag();
            String userName = mUserlist.get(position);
            viewHolder.tvUserName.setText(userName);
            viewHolder.ivDeleteUser.setTag(userName);
            viewHolder.ivModifyPasswd.setTag(userName);
            return convertView;
        }
    }

    private class UserListViewHolder {
        TextView tvUserName;
        ImageView ivDeleteUser, ivModifyPasswd;

        public UserListViewHolder (View convertView) {
            tvUserName = (TextView) convertView.findViewById(R.id.tv_user_name);
            ivDeleteUser = (ImageView) convertView.findViewById(R.id.iv_delete_user);
            ivModifyPasswd = (ImageView) convertView.findViewById(R.id.iv_modify_passwd);
            ivDeleteUser.setOnClickListener(UserManagementDialog.this);
            ivModifyPasswd.setOnClickListener(UserManagementDialog.this);
            convertView.setTag(this);
        }
    }
}
