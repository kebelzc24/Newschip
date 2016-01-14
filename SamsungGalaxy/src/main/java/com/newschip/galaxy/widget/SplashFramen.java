package com.newschip.galaxy.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.newschip.galaxy.R;
import com.newschip.galaxy.activity.MainActivity;
import com.newschip.galaxy.activity.PasswordActivity;
import com.newschip.galaxy.utils.PreferenceUtil;


public class SplashFramen extends Fragment {
    private Drawable mDrawable;
    boolean mButtonVisible;
    private Context mContext;

    public SplashFramen(){
        
    }
    @SuppressLint({"NewApi", "ValidFragment"})
    public SplashFramen(Context context, Drawable drawable, boolean visible) {
        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.mDrawable = drawable;
        this.mButtonVisible = visible;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.splash_fragment, container,
                false);
        ((ImageView) (view.findViewById(R.id.iv_splash)))
                .setImageDrawable(mDrawable);
        if(mButtonVisible){
            view.findViewById(R.id.iv_experience).setVisibility(View.VISIBLE);
        }
        view.findViewById(R.id.iv_experience).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        // TODO Auto-generated method stub
                        // 通过preference文件保存结果，只显示一次引导页
                        PreferenceUtil util = new PreferenceUtil(mContext);
                        util.saveBoolean(
                                PasswordActivity.KEY_FIRST_START, false);
                        startActivity(new Intent(getActivity(),
                                MainActivity.class));
                       getActivity().finish();
                    }
                });
        return view;

    }
}
