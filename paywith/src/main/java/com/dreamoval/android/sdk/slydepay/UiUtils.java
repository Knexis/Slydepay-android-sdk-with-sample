package com.dreamoval.android.sdk.slydepay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by Nana Kwame Nyantakyi on 1/7/2016 @5:09 PM.
 * Purpose:
 * Note:
 */
public class UiUtils {


//    public static boolean isSlydepayPresent(Context context) {
//        PackageManager pm = context.getPackageManager();
//        boolean app_installed;
//        try {
//            pm.getPackageInfo("com.dreamoval.slydepay.android.cruise", PackageManager.GET_ACTIVITIES);
//            app_installed = true;
//        }
//        catch (PackageManager.NameNotFoundException e) {
//            app_installed = false;
//        }
//        return app_installed;
//    }

    public static boolean isSlydepayPresent(Context context) {

        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.dreamoval.slydepay.android.cruise");
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static boolean isUpdatedVersion(Context context) {

        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("pay.with.slydepay");
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }



}
