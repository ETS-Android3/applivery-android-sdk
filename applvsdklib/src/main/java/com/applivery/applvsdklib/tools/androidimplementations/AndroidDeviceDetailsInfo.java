/*
 * Copyright (c) 2016 Applivery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.applivery.applvsdklib.tools.androidimplementations;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;
import com.applivery.applvsdklib.AppliverySdk;
import com.applivery.applvsdklib.domain.feedback.DeviceDetailsInfo;
import java.util.Locale;

/**
 * Created by Sergio Martinez Rodriguez
 * Date 10/4/16.
 */
public class AndroidDeviceDetailsInfo implements DeviceDetailsInfo {

  private static final String ANDROID = "Android";
  private static final String TABLET = "tablet";
  private static final String MOBILE = "mobile";
  private static final String CONNECTIVITY_UNKNOWN = "unknown";
  private static final String CONNECTIVITY_WIFI = "wifi";
  private static final String CONNECTIVITY_2G = "gprs";
  private static final String CONNECTIVITY_3G = "3g";
  private static final String CONNECTIVITY_4G = "4g";

  @Override public String getOsName() {
    return ANDROID;
  }

  @Override public String getVendor() {
    return Build.MANUFACTURER;
  }

  @Override public String getModel() {
    String model = Build.MODEL;
    if (model.contains(Build.MANUFACTURER+" ")){
      return model.replace(Build.MANUFACTURER+" ", "");
    } else {
      return model;
    }
  }

  @Override public String getDeviceType() {
    Context context = AppliverySdk.getApplicationContext();

    boolean isTablet = false;

    isTablet = (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;

    if (isTablet){
      return TABLET;
    } else{
      return MOBILE;
    }
  }

  @Override public String getOsversion() {
    return Build.VERSION.RELEASE;
  }

  @Override public String getDeviceId() {
    // TODO
    return null;
  }

  @Override public String getBatteryPercentage() {
    Context context = AppliverySdk.getApplicationContext();
    Intent batteryStatus = getBatteryStatus(context);
    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

    String batteryPercentage = String.valueOf((level / (float) scale) * 100);

    return batteryPercentage;
  }

  @Override public String isBatteryCharging() {
    Context context = AppliverySdk.getApplicationContext();
    boolean isBatteryCharging = false;
    Intent batteryStatus = getBatteryStatus(context);
    int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    isBatteryCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC
        || plugged == BatteryManager.BATTERY_PLUGGED_USB;

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
      isBatteryCharging = isBatteryCharging || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    return String.valueOf(isBatteryCharging);
  }

  @Override public String getNetworkConnectivity() {
    Context context = AppliverySdk.getApplicationContext();
    String connectivity = CONNECTIVITY_UNKNOWN;

    if (isConnectedWifi(context)) {
      connectivity = CONNECTIVITY_WIFI;
    } else if (isConnectedMobile(context)) {
      connectivity = getNetworkType(context);
    }

    return connectivity;
  }

  @Override public String getScreenResolution() {
    Context context = AppliverySdk.getApplicationContext();
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();

    int width;
    int height;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
      Point size = new Point();
      display.getSize(size);
      width = size.x;
      height = size.y;
    } else {
      width = display.getWidth();
      height = display.getHeight();
    }

    String screenResolution = String.format(Locale.getDefault(), "%dx%d", width, height);

    return screenResolution;
  }

  @Override public String getUsedRam() {
    return null;
  }

  @Override public String getTotalRam() {
    return null;
  }

  @Override public String getFreeDiskPercentage() {
    return null;
  }

  @Override public String getScreenOrientation() {
    return null;
  }

  private Intent getBatteryStatus(Context context) {
    return context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
  }

  private boolean isConnectedWifi(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
  }

  private boolean isConnectedMobile(Context context) {
    NetworkInfo info = getNetworkInfo(context);
    return (info != null
        && info.isConnected()
        && info.getType() == ConnectivityManager.TYPE_MOBILE);
  }

  private String getNetworkType(Context context) {
    TelephonyManager mTelephonyManager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    int networkType = mTelephonyManager.getNetworkType();
    switch (networkType) {
      case TelephonyManager.NETWORK_TYPE_GPRS:
      case TelephonyManager.NETWORK_TYPE_EDGE:
      case TelephonyManager.NETWORK_TYPE_CDMA:
      case TelephonyManager.NETWORK_TYPE_1xRTT:
      case TelephonyManager.NETWORK_TYPE_IDEN:
        return CONNECTIVITY_2G;
      case TelephonyManager.NETWORK_TYPE_UMTS:
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
      case TelephonyManager.NETWORK_TYPE_HSDPA:
      case TelephonyManager.NETWORK_TYPE_HSUPA:
      case TelephonyManager.NETWORK_TYPE_HSPA:
      case TelephonyManager.NETWORK_TYPE_EVDO_B:
      case TelephonyManager.NETWORK_TYPE_EHRPD:
      case TelephonyManager.NETWORK_TYPE_HSPAP:
        return CONNECTIVITY_3G;
      case TelephonyManager.NETWORK_TYPE_LTE:
        return CONNECTIVITY_4G;
      default:
        return CONNECTIVITY_UNKNOWN;
    }
  }

  private NetworkInfo getNetworkInfo(Context context) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo();
  }
}
