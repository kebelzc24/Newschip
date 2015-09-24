package com.newschip.fingerprint.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.newschip.fingerprint.provider.ProviderHelper;
import com.newschip.fingerprint.service.WatchDogService;
import com.newschip.fingerprint.utils.ConstantUtils;

public class BootReciver extends BroadcastReceiver{

    private final String TAG = ConstantUtils.TAG+"BroadcastReciver";
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        if(intent == null) return;
        String action = intent.getAction();
        Log.d(TAG, "action = "+action);
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            if(getSwitchState()||getProtectState()){
                context.startService(new Intent(context, WatchDogService.class));
            }
        }
    } 
    private boolean getSwitchState() {
        ProviderHelper helper = new ProviderHelper(mContext, "switch_state");
        return helper.getSwitchState();
    }
    private boolean getProtectState() {
        ProviderHelper helper = new ProviderHelper(mContext, "protect_state");
        return helper.getProtectState();
    }

}
