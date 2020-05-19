/*
 * Copyright (c) 2019 Applivery
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
package com.applivery.applvsdklib.domain.model

data class Feedback(
  val deviceInfo: DeviceInfo,
  val message: String?,
  val packageInfo: PackageInfo,
  val screenshot: String?,
  val type: String
)

data class DeviceInfo(
  val device: Device,
  val os: Os
)

data class Device(
  val battery: Int,
  val batteryStatus: Boolean,
  val diskFree: String,
  val model: String,
  val network: String,
  val orientation: String,
  val ramTotal: String,
  val ramUsed: String,
  val resolution: String,
  val type: String,
  val vendor: String
)

data class Os(
  val name: String,
  val version: String
)

data class PackageInfo(
  val name: String,
  val version: Int,
  val versionName: String
)
