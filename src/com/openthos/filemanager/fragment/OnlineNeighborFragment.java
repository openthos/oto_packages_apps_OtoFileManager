package com.openthos.filemanager.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openthos.filemanager.R;

public class OnlineNeighborFragment extends Fragment{
    private TextView tv_internet;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personal_fragment_layout, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        tv_internet = (TextView) view.findViewById(R.id.tv_internet);
        tv_internet.setVisibility(View.VISIBLE);
    }
}
