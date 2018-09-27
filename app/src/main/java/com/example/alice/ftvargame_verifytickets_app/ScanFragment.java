package com.example.alice.ftvargame_verifytickets_app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by alice on 2018/9/21.
 */

public class ScanFragment extends Fragment {
    public static ScanFragment newInstance(){
        ScanFragment frag = new ScanFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString(ARG_PARAM, str);
//        ScanFragment.setArguments(bundle);   //设置参数
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
