package com.dreamoval.android.sdk.slydepay.example;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * Created by Nana Kwame Nyantakyi on 1/7/2016 @12:37 PM.
 * Purpose:
 * Note:
 */
public class UiUtils {

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface getTypeface(String assetPath,Context context) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(context.getAssets(),
                            assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}
