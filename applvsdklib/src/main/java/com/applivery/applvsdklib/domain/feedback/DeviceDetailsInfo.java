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

package com.applivery.applvsdklib.domain.feedback;

/**
 * Created by Sergio Martinez Rodriguez
 * Date 10/1/16.
 */
public interface DeviceDetailsInfo {

  String getOsName();

  String getVendor();

  String getModel();

  String getDeviceType();

  String getOsversion();

  String getDeviceId();

  int getBatteryPercentage();

  boolean isBatteryCharging();

  String getNetworkConnectivity();

  String getScreenResolution();

  String getUsedRam();

  String getTotalRam();

  String getFreeDiskPercentage();

  String getScreenOrientation();
}
