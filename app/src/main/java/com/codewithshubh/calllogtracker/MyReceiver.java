package com.codewithshubh.calllogtracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class MyReceiver extends BroadcastReceiver {
    public static String SHOW_APP_CODE = "*1234#";
    @Override
    public void onReceive(Context context, Intent intent) {
        String dialedNumber=null;// = getResultData();
        if (dialedNumber == null) {
            // No reformatted number, use the original
            dialedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        }
        //Log.d("Dialed Number: ", dialedNumber);
        if (dialedNumber.equals(SHOW_APP_CODE)){
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, MainActivity.class);
            packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            //Intent to launch MainActivity
            Intent intent_to_mainActivity = new Intent(context, MainActivity.class);
            intent_to_mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent_to_mainActivity);

            // My app will bring up, so cancel the dialer broadcast
            setResultData(null);
        }
    }
}
