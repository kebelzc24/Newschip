package com.newschip.fingerprint.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newschip.fingerprint.R;

public class CommonTitleLayout extends RelativeLayout {

    private LayoutInflater mLayoutInflater;
    private View mView;

    private TextView mTitleText;
    private LinearLayout mLinearLayout;
    private Context mContext;

    public CommonTitleLayout(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public CommonTitleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public CommonTitleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mView = mLayoutInflater.inflate(R.layout.common_title_bar, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(mView, lp);

        mTitleText = (TextView) mView.findViewById(R.id.txt_title);
        mLinearLayout = (LinearLayout) mView.findViewById(R.id.layout_new);
    }

    public void setTitle(int id) {
        if (mTitleText != null) {
            mTitleText.setText(mContext.getResources().getString(id));
        }
    }

    public void startNewAnimation() {
        if (mLinearLayout != null) {
            mLinearLayout.setVisibility(View.VISIBLE);
            mLinearLayout.startAnimation(AnimationUtils.loadAnimation(
                    mContext, R.anim.shake));
        }

    }

}
