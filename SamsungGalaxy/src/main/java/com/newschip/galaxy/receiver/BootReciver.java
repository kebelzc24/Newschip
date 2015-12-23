package com.newschip.galaxy.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.newschip.galaxy.provider.ProviderHelper;
import com.newschip.galaxy.service.WatchDogService;


public class BootReciver extends BroadcastReceiver{

    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        if(intent == null) return;
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            if(ProviderHelper.isEnableEasyHomeState(mContext)||ProviderHelper.isEnableSwitchState(mContext)||ProviderHelper.isEnableProtectState(mContext)){
                context.startService(new Intent(context, WatchDogService.class));
            }
        }
    } 

}
