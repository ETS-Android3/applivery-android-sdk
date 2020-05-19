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
package com.applivery.updates

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.applivery.base.AppliveryDataManager
import com.applivery.base.data.LIMIT_EXCEEDED_ERROR
import com.applivery.base.data.ServerResponse
import com.applivery.base.util.AppliveryLog
import com.applivery.updates.data.ApiServiceProvider
import com.applivery.updates.data.DownloadApiService
import com.applivery.updates.data.UpdatesApiService
import com.applivery.updates.data.response.ApiBuildToken
import com.applivery.updates.domain.DownloadInfo
import com.applivery.updates.util.ApkInstaller
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.IOException


private const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_87234"
private const val NOTIFICATION_ID = 0x21
private const val MAX_PROGRESS = 100

class DownloadService : IntentService("Download apk service") {

    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
    private lateinit var fileManager: FileManager
    private lateinit var downloadApiService: DownloadApiService
    private lateinit var updatesApiService: UpdatesApiService

    override fun onHandleIntent(intent: Intent?) {
        AppliveryDataManager.appData?.run {

            isDownloadStarted = true
            fileManager = FileManager(applicationContext)
            updatesApiService = ApiServiceProvider.getApiService()
            downloadApiService = ApiServiceProvider.getDownloadApiService()

            val apkFileName = name.replace(" ", "-") +
                    "-" + appConfig.lastBuildId + ".apk"

            // TODO check if file exits

            val buildToken = getBuildToken(appConfig.lastBuildId)
            if (buildToken.isNotEmpty()) {
                initDownload(name, apkFileName, buildToken)
            }
        } ?: also {
            isDownloadStarted = false
            AppliveryLog.error("The download cannot be started with null app data")
        }
    }

    private fun getBuildToken(buildId: String): String {
        return try {
            val tokenResponse: Response<ServerResponse<ApiBuildToken>> =
                updatesApiService.obtainBuildToken(buildId).execute()

            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.data?.token as String
            } else {
                handleError(tokenResponse)
                ""
            }
        } catch (e: IOException) {
            AppliveryLog.error("Cannot get the build token")
            ""
        }
    }

    private fun handleError(response: Response<ServerResponse<ApiBuildToken>>) {
        try {
            val error = Gson().fromJson<ServerResponse<ApiBuildToken>>(
                response.errorBody()?.string(),
                ServerResponse::class.java
            ).error

            if (error.code == LIMIT_EXCEEDED_ERROR) {
                val limit = error.data?.get("limit") ?: "-1"
                AppliveryLog.error("Installations limit exceeded. Limit: $limit/month")

            } else {
                AppliveryLog.error("Cannot get the build token. ${error.message}")
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
            AppliveryLog.error("Cannot get the build token. Invalid config")
        }
    }

    private fun initDownload(appName: String, apkFileName: String, buildToken: String) {
        showNotification(appName)
        try {
            val request = downloadApiService.downloadBuild(buildToken)
            request.execute().body()?.run { downloadFile(apkFileName, this) }
        } catch (e: IOException) {
            notificationManager?.cancel(0)
            AppliveryLog.error("Error downloading apk")
        }
    }

    @Throws(IOException::class)
    private fun downloadFile(apkFileName: String, body: ResponseBody) {

        val outputFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File(cacheDir, apkFileName)
        } else {
            File(filesDir, apkFileName)
        }

        fileManager.writeResponseBodyToDisk(body = body,
            file = outputFile,
            onUpdate = { downloadInfo ->
                updateProgress(downloadInfo)
                ProgressListener.downloadInfo = downloadInfo
            },
            onFinish = {
                ProgressListener.onFinish?.invoke()
                isDownloadStarted = false
                onDownloadComplete(outputFile.path)
            },
            onError = {
                isDownloadStarted = false
                AppliveryLog.error("Error saving apk")
            })
    }

    private fun showNotification(appName: String) {
        createNotificationChannel()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_applivery_blue)
            .setContentTitle(getString(R.string.applivery_updates_app_name))
            .setContentText(getString(R.string.applivery_updates_notification_text, appName))
            .setAutoCancel(false)
            .setOngoing(true)
            .setSound(null)
            .setVibrate(null)
            .setProgress(MAX_PROGRESS, 0, true)
        notificationManager?.notify(NOTIFICATION_ID, notificationBuilder?.build())
    }

    private fun updateProgress(downloadInfo: DownloadInfo) {
        notificationBuilder?.setProgress(MAX_PROGRESS, downloadInfo.progress, false)
        notificationBuilder?.setContentText(
            getString(
                R.string.applivery_updates_notification_progress,
                downloadInfo.currentFileSize,
                downloadInfo.totalFileSize
            )
        )
        notificationManager?.notify(NOTIFICATION_ID, notificationBuilder!!.build())


    }

    private fun onDownloadComplete(filePath: String) {
        clearNotification()
        ApkInstaller.installApplication(this, filePath)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        clearNotification()
    }

    private fun clearNotification() {
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.applivery_updates_channel_name)
            val description = getString(R.string.applivery_updates_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            channel.setSound(null, null)
            channel.enableVibration(false)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    companion object {
        private var isDownloadStarted = false

        fun startDownloadService(context: Context) {
            if (!isDownloadStarted) {
                val intent = Intent(context, DownloadService::class.java)
                context.startService(intent)
            }
        }
    }
}
