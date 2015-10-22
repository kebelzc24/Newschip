package com.newschip.fingerprint.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.trinea.android.common.util.ToastUtils;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.utils.PackageUtils;
import com.newschip.fingerprint.utils.PreferenceUtil;
import com.newschip.fingerprint.view.PasswordTextView;

public class PasswordActivity extends BaseActivity implements OnClickListener {
    protected Intent mIntent;
    // 密码框
    private PasswordTextView et_pwd1, et_pwd2, et_pwd3, et_pwd4;
    private int mType;
    public TextView tv_info;// 提示信息
//    protected TextView mResultText;
    protected TextView mForgetText;
    // 声明字符串保存每一次输入的密码
    private String input;
    private StringBuffer fBuffer = new StringBuffer();
    private String mPassword;
    protected PreferenceUtil mSPUtil;
    public final String KEY = "password";
    private int TYPE_SET_PASSWORD = 0;
    private int TYPE_CONFIRM_SET_PASSWORD = 1;
    private int TYPE_CHECK_PASSWORD = 2;

    private Button mBtn0;
    private Button mBtn1;
    private Button mBtn2;
    private Button mBtn3;
    private Button mBtn4;
    private Button mBtn5;
    private Button mBtn6;
    private Button mBtn7;
    private Button mBtn8;
    private Button mBtn9;

    @Override
    protected int getLayoutView() {
        return R.layout.activity_password;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSystemBarImmerse();
        mContext = this;
        mIntent = getIntent();
        mSPUtil = new PreferenceUtil(mContext);
        initWidget();// 初始化控件
        initListener();// 事件处理
        mPassword = mSPUtil.getString(KEY, "");

        if (mPassword == null || mPassword.equals("")) {
            tv_info.setText(getResources().getString(R.string.please_set_pwd));
            mType = TYPE_SET_PASSWORD;
            mForgetText.setVisibility(View.GONE);

        } else {
            mType = TYPE_CHECK_PASSWORD;
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        mBtn0 = (Button) findViewById(R.id.btn_0);
        mBtn1 = (Button) findViewById(R.id.btn_1);
        mBtn2 = (Button) findViewById(R.id.btn_2);
        mBtn3 = (Button) findViewById(R.id.btn_3);
        mBtn4 = (Button) findViewById(R.id.btn_4);
        mBtn5 = (Button) findViewById(R.id.btn_5);
        mBtn6 = (Button) findViewById(R.id.btn_6);
        mBtn7 = (Button) findViewById(R.id.btn_7);
        mBtn8 = (Button) findViewById(R.id.btn_8);
        mBtn9 = (Button) findViewById(R.id.btn_9);
        mBtn0.setOnClickListener(this);
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);
        mBtn4.setOnClickListener(this);
        mBtn5.setOnClickListener(this);
        mBtn6.setOnClickListener(this);
        mBtn7.setOnClickListener(this);
        mBtn8.setOnClickListener(this);
        mBtn9.setOnClickListener(this);
        mForgetText = (TextView) findViewById(R.id.tv_forget_mima);
        mForgetText.setOnClickListener(this);
        tv_info = (TextView) findViewById(R.id.tv_info);
        // 密码框
        et_pwd1 = (PasswordTextView) findViewById(R.id.et_pwd1);
        et_pwd2 = (PasswordTextView) findViewById(R.id.et_pwd2);
        et_pwd3 = (PasswordTextView) findViewById(R.id.et_pwd3);
        et_pwd4 = (PasswordTextView) findViewById(R.id.et_pwd4);
        tv_info = (TextView) findViewById(R.id.tv_info);// 提示信息
    }

    /**
     * 事件处理
     */
    private void initListener() {
        // 监听最后一个密码框的文本改变事件回调
        et_pwd4.setOnTextChangedListener(new PasswordTextView.OnTextChangedListener() {
            @Override
            public void textChanged(String content) {
                input = et_pwd1.getTextContent() + et_pwd2.getTextContent()
                        + et_pwd3.getTextContent() + et_pwd4.getTextContent();
                // 判断类型
                if (mType == TYPE_SET_PASSWORD) {// 设置密码
                    // 重新输入密码
                    tv_info.setText(getString(R.string.please_input_pwd_again));
                    mType = TYPE_CONFIRM_SET_PASSWORD;
                    fBuffer.append(input);// 保存第一次输入的密码
                    clearText();// 清除输入
                } else if (mType == TYPE_CHECK_PASSWORD) {// 登录
                    if (input.equals(mPassword)) {
                        String pkg = mIntent.getStringExtra("packageName");
                        if (TextUtils.isEmpty(pkg)) {
                            if(mSPUtil.getBoolean("has_register", false)){
                                Log.e("kebelzc","MainActivity...");
                                startActivity(new Intent(PasswordActivity.this,
                                        MainActivity.class));
                            } else {
                                startActivity(new Intent(PasswordActivity.this,
                                        MainActivity.class));
                            }
                        } else {
                            PackageUtils.runApp(mContext, pkg);
                        }
                        finish();
                    } else {
                        ToastUtils.show(mContext,
                                getString(R.string.password_not_equals));
                        clearText();// 清除输入
                    }

                } else if (mType == TYPE_CONFIRM_SET_PASSWORD) {// 确认密码
                    // 判断两次输入的密码是否一致
                    if (input.equals(fBuffer.toString())) {// 一致
                        // 保存密码到文件中
                        ToastUtils.show(mContext, "密码设置成功");

                        mSPUtil.saveString(KEY, input);
                        if(mSPUtil.getBoolean("has_register", false)){
                            startActivity(new Intent(PasswordActivity.this,
                                    MainActivity.class));
                        } else {
                            startActivity(new Intent(PasswordActivity.this,
                                    MainActivity.class));
                        }
                        

                        finish();
                    } else {// 不一致
                        ToastUtils.show(mContext,
                                getString(R.string.not_equals));
                        // tv_info.setText(getResources().getString(R.string.please_set_pwd));
                        clearText();// 清除输入
                    }
                }
            }
        });
    }

    /**
     * 设置显示的密码
     * 
     * @param text
     */
    private void setText(String text) {
        // 从左往右依次显示
        if (TextUtils.isEmpty(et_pwd1.getTextContent())) {
            et_pwd1.setTextContent(text);
        } else if (TextUtils.isEmpty(et_pwd2.getTextContent())) {
            et_pwd2.setTextContent(text);
        } else if (TextUtils.isEmpty(et_pwd3.getTextContent())) {
            et_pwd3.setTextContent(text);
        } else if (TextUtils.isEmpty(et_pwd4.getTextContent())) {
            et_pwd4.setTextContent(text);
        }
    }

    /**
     * 清除输入的内容--重输
     */
    private void clearText() {
        et_pwd1.setTextContent("");
        et_pwd2.setTextContent("");
        et_pwd3.setTextContent("");
        et_pwd4.setTextContent("");
    }

    /**
     * 删除刚刚输入的内容
     */
    private void deleteText() {
        // 从右往左依次删除
        if (!TextUtils.isEmpty(et_pwd4.getTextContent())) {
            et_pwd4.setTextContent("");
        } else if (!TextUtils.isEmpty(et_pwd3.getTextContent())) {
            et_pwd3.setTextContent("");
        } else if (!TextUtils.isEmpty(et_pwd2.getTextContent())) {
            et_pwd2.setTextContent("");
        } else if (!TextUtils.isEmpty(et_pwd1.getTextContent())) {
            et_pwd1.setTextContent("");
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        int number = 10;
        switch (id) {
        case R.id.btn_0:
            number = 0;
            break;
        case R.id.btn_1:
            number = 1;
            break;
        case R.id.btn_2:
            number = 2;
            break;
        case R.id.btn_3:
            number = 3;
            break;
        case R.id.btn_4:
            number = 4;
            break;
        case R.id.btn_5:
            number = 5;
            break;
        case R.id.btn_6:
            number = 6;
            break;
        case R.id.btn_7:
            number = 7;
            break;
        case R.id.btn_8:
            number = 8;
            break;
        case R.id.btn_9:
            number = 9;
            break;

        }
        setText(number + "");
        if (view == mForgetText) {
            ToastUtils.show(mContext, "请到设置清除应用数据");

        }
    }

}
