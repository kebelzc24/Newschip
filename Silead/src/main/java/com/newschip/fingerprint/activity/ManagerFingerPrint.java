package com.newschip.fingerprint.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.adapter.FingerAdapter;
import com.newschip.fingerprint.dialog.SweetAlertDialog;
import com.newschip.fingerprint.utils.ToastUtils;
import com.silead.fp.utils.FpControllerNative;
import com.silead.fp.utils.FpControllerNative.SLFpsvcFPInfo;
import com.silead.fp.utils.FpControllerNative.SLFpsvcIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ManagerFingerPrint extends BaseActivity implements android.view.View.OnClickListener {

    // private static final int
//	private static final int CREATE_NEW_FINGER_NAME = 0;
//	private static final int RENAME_FINGER = 1;

    private final static String TAG = "Settings";
    private LinearLayout mEnrollmentLayout;
    private TextView mFingerPrompt;
    private SimpleAdapter mAdapter;
    private HashMap<Integer, Boolean> check;

    private ArrayList<SLFpsvcFPInfo> mFingerList;
    private FpControllerNative mControllerNative;
    private SLFpsvcIndex fpsvcIndex;
    public SLFpsvcFPInfo fPInfos;

    List<Map<String, Object>> mFingerData = new ArrayList<Map<String, Object>>();
    private ListView mSwipeListView;
    public static int deviceWidth;
    private Toolbar mToolbar;

    @Override
    protected int getLayoutView() {
        return R.layout.acivity_manager_fingerprint;
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        super.initToolbar(mToolbar);
        mToolbar.setTitle(R.string.manager_finger_print);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initToolbar();
        deviceWidth = getDeviceWidth();
//		identify = false;
        mControllerNative = FpControllerNative.getInstance();
        mControllerNative.initFPSystem();
        initView();


        Log.d(TAG, "enrollCredentialRSP $$$$$$$ GetFpInfo begin");



    }

    private void getFingerList(){
        mFingerList = new ArrayList<SLFpsvcFPInfo>();
        mFingerList.clear();
        fpsvcIndex = mControllerNative.GetFpInfo();


        if (fpsvcIndex.max > 5) {
            fpsvcIndex.max = 5;
        }
        for (int i = 0; i < fpsvcIndex.max; i++) {
            if (fpsvcIndex.FPInfo[i].slot == 1) {
                Log.d(TAG, " $$$ fpsvcIndex.fingerName 222 = " + fpsvcIndex.FPInfo[i].fingerName + ":" + fpsvcIndex.FPInfo[i].enrollIndex);
                mFingerList.add(fpsvcIndex.FPInfo[i]);
            }
        }

        if (mFingerList.size() > 0) {
            mSwipeListView.setVisibility(View.VISIBLE);
            mFingerPrompt.setVisibility(View.GONE);
            mFingerPrompt.setText("");
            for (int i = 0; i < mFingerList.size(); i++) {
                if (mFingerList.get(i).getEnable() == 0) {
                    mFingerList.get(i).setEnable(1);
                    mControllerNative.EnalbeCredential(mFingerList.get(i).enrollIndex, 1);
                    check.put(mFingerList.get(i).enrollIndex, true);
                    mAdapter.notifyDataSetChanged();
                }
            }
        } else {
            mSwipeListView.setVisibility(View.GONE);
            mFingerPrompt.setVisibility(View.VISIBLE);
            mFingerPrompt.setText(getString(R.string.no_finger_print));
        }
        // sharedPrefrences = getSharedPreferences(tAG1, MODE_PRIVATE);
        // edit = sharedPrefrences.edit();

        // for (int i = 0; i < mFingerList.size(); i++) {
        // //
        // edit.putInt("fingerSlot+i", mFingerList.get(i).slot);
        // edit.putInt("fingerEnable+i", mFingerList.get(i).enable);
        // edit.commit();
        // edit.clear();
        // }

//		mDateManager = new DataUtil(this);

//		boolean checked = mDateManager.getFPSwitch();
        check = FingerAdapter.getCheck();
        for (int j = 0; j < fpsvcIndex.max; j++) {
            if (fpsvcIndex.FPInfo[j].enable == 0) {
                check.put(j, false);
            } else if (fpsvcIndex.FPInfo[j].enable == 1) {
                check.put(j, true);
            }
        }
        mFingerData.clear();
        mFingerData = getData();
    }


    private void initFpAdapter() {
         mAdapter = new SimpleAdapter(this, mFingerData, R.layout.finger_list_item_layout, new String[]{"finger"}, new int[]{R.id.textView});
        mSwipeListView.setAdapter(mAdapter);
        mSwipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                showAlertDialog(mContext,SweetAlertDialog.NORMAL_TYPE,position);
            }
        });
    }
    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for (int i = 0; i < mFingerList.size(); i++) {
            map = new HashMap<String, Object>();
            map.put("finger", mFingerList.get(i).getFingerName());
            list.add(map);
        }
        return list;
    }

    private int getDeviceWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    class FingerListItemOnItemLongClick implements OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int pos1, long pos2) {
            SLFpsvcFPInfoonLongClick(pos1);
            return true;
        }
    }

    public static void DisableFp() {
        Log.d(TAG, "DisableFp $$$$$$$ DisableFp 2222 ");
        FpControllerNative controllerNative = FpControllerNative.getInstance();
        controllerNative.initFPSystem();
        SLFpsvcIndex fpIndex = controllerNative.GetFpInfo();
        for (int i = 0; i < fpIndex.max; i++) {
            if (fpIndex.FPInfo[i].enable == 1) {
                Log.d(TAG, "DisableFp $$$$$$$ DisableFp enable[" + i + "] = " + fpIndex.FPInfo[i].enable);
                fpIndex.FPInfo[i].setEnable(0);
            }
        }
        controllerNative.SetFpInfo(fpIndex);

    }
    public void showAlertDialog(Context context, int type,final int position) {
        // TODO Auto-generated method stub
        final SweetAlertDialog alert = new SweetAlertDialog(context, type);
        alert.setCancelable(true);
        alert.setTitleText(getResources()
                .getString(R.string.delete_finger_print));
        alert.setConfirmText(getResources().getString(R.string.ok));
        alert.setCancelText(getResources().getString(R.string.cancel));
        alert.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                alert.dismiss();
                deleteFingerPrint(position);
            }
        });
        alert.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                alert.dismiss();
            }
        });
        alert.show();
    }
    @SuppressLint("NewApi")
    private void SLFpsvcFPInfoonLongClick(final int pos) {

        final String[] mItems = {"重命名", "删除"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ManagerFingerPrint.this);
        builder.setItems(mItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 点击后弹出窗口选择了第几项
                switch (which) {
                    case 0:
                        renameSLFpsvcFPInfo(pos);
                        break;
                    case 1:
                        // 顶层删除finger
                        Log.d(TAG, "SLFpsvcFPInfoonLongClick 1  pos = " + pos + ":" + mFingerList.size());
                        fPInfos = (SLFpsvcFPInfo) mFingerList.get(pos);
                        fpsvcIndex = mControllerNative.GetFpInfo();
                        int deleteIndex = fPInfos.enrollIndex;
                        mControllerNative.removeCredential(deleteIndex);
                        mFingerList.remove(pos);
                        check.put(deleteIndex, false);
                        for (int i = 0; i < 5; i++) {
                            System.out.println("removeCredential fpsvcIndex.FPInfo[i].enrollIndex"
                                    + fpsvcIndex.FPInfo[i].enrollIndex + "fpsvcIndex.FPInfo[i].slot"
                                    + fpsvcIndex.FPInfo[i].slot + "fpsvcIndex.FPInfo[i].enable"
                                    + fpsvcIndex.FPInfo[i].enable + "fpsvcIndex.FPInfo[i].fingerName"
                                    + fpsvcIndex.FPInfo[i].fingerName);
                        }
                        mAdapter.notifyDataSetChanged();
                        if (mFingerList.size() == 0) {
                            mFingerPrompt.setVisibility(View.VISIBLE);
                            mFingerPrompt.setText(getString(R.string.no_finger_print));
                        }

                        Log.d(TAG, "SLFpsvcFPInfoonLongClick 1  deleteIndex = " + deleteIndex + ":" + mFingerList.size());
                        if (mFingerList.size() > pos) {
                            Log.d(TAG,
                                    "mFingerList.get(pos).slot = "
                                            + mFingerList.get(pos).slot);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        builder.create().show();
    }

    class EntryOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View arg0) {
            System.out.println("EntryOnClickListener onClick");
            Intent intent = new Intent(ManagerFingerPrint.this, EnrollActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == 0 && data != null) {
            Bundle extras = data.getExtras();
            int enrollIndex = extras.getInt("enrollIndex");
            fpsvcIndex = mControllerNative.GetFpInfo();
            mFingerList.add(fpsvcIndex.FPInfo[enrollIndex]);
            check.put(enrollIndex, true);
        }

        if (mFingerList.size() > 0) {
            mFingerPrompt.setText("");
        }
    }

    private void initView() {
        mSwipeListView = (ListView) findViewById(R.id.fp_finger_list);
        mEnrollmentLayout = (LinearLayout) findViewById(R.id.ll_enrollment);
        mEnrollmentLayout.setOnClickListener(this);
        mFingerPrompt = (TextView) findViewById(R.id.fp_prompt);
    }

    public void myLog(String msg) {
        Log.d(TAG, msg);
    }

    public void onResume() {
        super.onResume();
        setFingerListView();
    }

    private void setFingerListView(){
        getFingerList();
        initFpAdapter();
        mAdapter.notifyDataSetChanged();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        System.out.println("Settings onstart 启动了");
    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "run onstop(), cancel enrolling");
//		mControllerNative.destroyFPSystem();
        System.out.println("Settings onStop 启动了");
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        System.out.println("Settings onRestart 启动了");
    }

    public void onDestroy() {
        super.onDestroy();
//		identify = false;
        //mControllerNative.FpCancelOperation();
        mControllerNative.destroyFPSystem();
        System.out.println("Settings onDestroy 启动了");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("backkey onKeyDown" + keyCode + ":"
                + KeyEvent.KEYCODE_BACK);
        super.onKeyDown(keyCode, event);

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:// 按返回键退出录入
                //mControllerNative.FpCancelOperation();

                return true;
            case KeyEvent.KEYCODE_HOME:
                //mControllerNative.FpCancelOperation();
                return true;
            case KeyEvent.KEYCODE_MENU:
                //mControllerNative.FpCancelOperation();
                return true;
            case KeyEvent.KEYCODE_POWER:
            default:
                System.out.println("backkey onKeyDown default");
                break;
        }
        return true;
    }

    public void Mytoast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void renameSLFpsvcFPInfo(final int index) {

        final SLFpsvcFPInfo mfinger = mFingerList.get(index);
        Builder mDialog = new Builder(ManagerFingerPrint.this);

        String title = getString(R.string.entry_finger_name);
        String ok = getString(R.string.ok);
        String cancel = getString(R.string.cancel);
        mDialog.setTitle(title);
        mDialog.setIcon(android.R.drawable.ic_dialog_info);

        final EditText et = new EditText(ManagerFingerPrint.this);
        // et.setHint(mfinger.getFingerName());
        mDialog.setView(et);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        et.setMaxLines(1);
        mDialog.setPositiveButton(ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                String fingerName = et.getText().toString().trim();
                mfinger.setFingerName(fingerName);
                int enrollIndex2 = mfinger.enrollIndex;
                fpsvcIndex = mControllerNative.GetFpInfo();
                System.out.println("fpsvcIndex.FPInfo[enrollIndex2].enrollIndex"
                        + fpsvcIndex.FPInfo[enrollIndex2].enrollIndex + "fpsvcIndex.FPInfo[index].slot"
                        + fpsvcIndex.FPInfo[enrollIndex2].slot + "fpsvcIndex.FPInfo[index].enable"
                        + fpsvcIndex.FPInfo[enrollIndex2].enable + "fpsvcIndex.FPInfo[index].fingerName"
                        + fpsvcIndex.FPInfo[enrollIndex2].fingerName);
                fpsvcIndex.FPInfo[enrollIndex2].setFingerName(fingerName);
                fpsvcIndex.setFPInfo(fpsvcIndex.FPInfo);
                mControllerNative.SetFpInfo(fpsvcIndex);
                mAdapter.notifyDataSetChanged();
            }
        });
        mDialog.setNeutralButton(cancel, null);
        mDialog.show();

    }


    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view == mEnrollmentLayout) {
            if (mFingerList.size() >= 5) {
                ToastUtils.show(mContext, getResources().getString(R.string.enroll_more_than_five));
            } else {
                Intent intent = new Intent(ManagerFingerPrint.this, EnrollActivity.class);
                startActivityForResult(intent, 0);
            }

        }
    }


    public void onRenameClick(int pos) {
        // TODO Auto-generated method stub
        renameSLFpsvcFPInfo(pos);
    }

    public void deleteFingerPrint(int pos) {
        // TODO Auto-generated method stub
        fPInfos = (SLFpsvcFPInfo) mFingerList.get(pos);
        fpsvcIndex = mControllerNative.GetFpInfo();
        int deleteIndex = fPInfos.enrollIndex;
        mControllerNative.removeCredential(deleteIndex);
        mFingerList.remove(pos);
        check.put(deleteIndex, false);
        setFingerListView();
        if (mFingerList.size() == 0) {
            mSwipeListView.setVisibility(View.GONE);
            mFingerPrompt.setVisibility(View.VISIBLE);
            mFingerPrompt.setText(getString(R.string.no_finger_print));
        }

    }


}
