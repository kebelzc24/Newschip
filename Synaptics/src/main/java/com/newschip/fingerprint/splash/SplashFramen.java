package com.newschip.fingerprint.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.activity.MainActivity;

/***
 * author:liuzhicang data:2015-06-09 description:SplashFramentOne内容引导页1
 * */
public class SplashFramen extends Fragment {
    private int mLayoutId;
    public SplashFramen(int res) {
        // TODO Auto-generated constructor stub
        this.mLayoutId = res;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(mLayoutId, container, false);
        return view;
        
    }
}
