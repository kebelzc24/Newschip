package com.newschip.galaxy.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.newschip.galaxy.R;
import com.newschip.galaxy.dialog.DialogHelper;
import com.newschip.galaxy.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginRegisterActivity extends BaseActivity implements OnClickListener {

    private final String TAG = "RegisterActivity";
    private EditText mPhoneNumEt;
    private EditText mPasswordEt;
    private Button mLoginBtn;
    private Button mRegisterBtn;
    private final static int PORT = 5099;
    private final String HOST_NAME = "newschip.xicp.net";
    private String mType;

    public final String KEY = "has_register";

    @Override
    public int getLayoutView() {
        return R.layout.activity_login_register;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        //setCommonTitleBarTitle(R.string.register);
        mPhoneNumEt = (EditText) findViewById(R.id.et_user_name);
        mPasswordEt = (EditText) findViewById(R.id.et_input_password);
        mLoginBtn = (Button) findViewById(R.id.btn_login);
        mRegisterBtn = (Button) findViewById(R.id.btn_register);
        mLoginBtn.setOnClickListener(this);
        mRegisterBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        switch (id) {
        case R.id.btn_login:
            if (isReady()) {
                mType = "login";
                connectToServer(mType);
            }
            break;
        case R.id.btn_register:
            if (isReady()) {
                mType = "register";
                connectToServer(mType);
            }
            break;

        default:
            break;
        }
    }

    private void connectToServer(String type) {
        JSONObject json = getJSONObject(type);
        if (json != null) {
            new SocketTask().execute(json);
        }
    }

    private boolean isReady() {
        if (TextUtils.isEmpty(mPhoneNumEt.getText())) {
            ToastUtils.show(mContext, "请输入手机号");
            return false;
        }
        if (TextUtils.isEmpty(mPasswordEt.getText())) {
            ToastUtils.show(mContext, "请输入密码");
            return false;
        }
        if (!isPhoneNum(mPhoneNumEt.getText().toString())) {
            ToastUtils.show(mContext, "手机号不合法，请重新输入");
            return false;
        }
        if (!isNetworkAvailable(mContext)) {
            ToastUtils.show(mContext, "网络不可用");
            return false;
        }
        return true;

    }

    private JSONObject getJSONObject(String type) {
        JSONObject mObject = new JSONObject();
        try {
            mObject.put("TYPE", type);
            mObject.put("USER_NAME", mPhoneNumEt.getText());
            mObject.put("PASSWORD", mPasswordEt.getText());
            mObject.put("MODEL", Build.MODEL);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return mObject;
    }

    private boolean isPhoneNum(String num) {
        Pattern p = Pattern.compile("^(1[3,4,5,7,8][0-9])\\d{8}$");
        Matcher m = p.matcher(num);
        return m.matches();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
    }

    private class SocketTask extends AsyncTask<JSONObject, Integer, String> {

        private Dialog mDialog;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mDialog = DialogHelper.createLoadingDialog(mContext, "请稍后...");
            mDialog.show();
        }

        @Override
        protected String doInBackground(JSONObject... json) {
            // TODO Auto-generated method stub
            return doSocket(json[0]);
        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            mDialog.dismiss();
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mDialog.dismiss();
            praseResult(result);
        }
    }

    private void praseResult(String result) {
        if (result.toLowerCase(Locale.CHINESE).contains("error")) {
            if (mType.contains("register")) {
                ToastUtils.show(mContext, "用户名已存在，请重试");
            } else if (mType.contains("login")) {
                ToastUtils.show(mContext, "用户名或者密码不正确");
            } else {
                ToastUtils.show(mContext, "失败了，请重试");
            }

        } else if (result.toLowerCase(Locale.CHINESE).contains("default_fail")) {
            ToastUtils.show(mContext, "服务器忙，请稍后重试");
        } else {
            mSPUtil.saveBoolean(KEY, true);
            ToastUtils.show(mContext, "成功");
            if(mSPUtil.getBoolean(PasswordActivity.KEY_FIRST_START, true)){
                startActivity(new Intent(mContext, MainActivity.class));
            } else {
                startActivity(new Intent(mContext, MainActivity.class));
            }

            finish();
        }
    }

    private String doSocket(JSONObject json) {

        String result = "default_fail";
        OutputStream out = null;
        try {
            InetAddress address = InetAddress.getByName(HOST_NAME);
            String ipAddress = address.getHostAddress();
            byte[] bypes = json.toString().getBytes();
            ByteBuffer buf = ByteBuffer.allocate(bypes.length+10);
            buf.put(bypes);
            Socket socket = new Socket(ipAddress, PORT);
            if (socket.isConnected()) {
                out = socket.getOutputStream();
                out.write(buf.array());
                out.flush();
                BufferedReader bff = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                String line = null;
                String buffer = "";
                while ((line = bff.readLine()) != null) {
                    buffer = line + buffer;
                    break;
                }
                bff.close();
                out.close();
                socket.close();
                result = buffer;
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            // TODO: handle exception
        } finally {
        }
        return result;
    }

}
