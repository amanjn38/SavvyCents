package com.finance.savvycents.utilities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object SmsUtils {

    const val REQUEST_CODE_SMS_PERMISSION = 123
    fun isDefaultSmsApp(context: Context): Boolean {
        val defaultSmsPackageName = context.packageName
        return defaultSmsPackageName == Telephony.Sms.getDefaultSmsPackage(context)
    }

    fun openDefaultSmsSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }

    fun requestSmsPermission(context: Context, listener: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val sendSmsPermissionState = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            val receiveSmsPermissionState = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
            if (sendSmsPermissionState == PackageManager.PERMISSION_DENIED || receiveSmsPermissionState == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS), REQUEST_CODE_SMS_PERMISSION)
            } else {
                listener(true)
            }
        } else {
            listener(true)
        }
    }
}