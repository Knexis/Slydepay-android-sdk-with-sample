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
public class PayWithUiUtils {

    public   static final String TRANSACTION_PENDING    = "Sorry the transaction was not completed. It might be still pending";
    public   static final String TRANSACTION_UNVERIFIED = "Sorry the transaction couldn't be verified";
    public   static final String MESSAGE = "message";
    public   static final String ORDER_ID = "order_id";

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



    public static String getErrorMessage(String errorCode){
        if(errorCode.equalsIgnoreCase("Error: 1")){// these errors are not to be show to the user //they are for you// please check
            return "Please check your credentials that you are using your verified Merchant email and Key";
        }else{
            return TRANSACTION_PENDING;
        }
    }

}
