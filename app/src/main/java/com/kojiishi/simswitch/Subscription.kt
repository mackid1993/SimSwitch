package com.kojiishi.simswitch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.ListFormatter
import android.provider.Settings
import android.service.quicksettings.TileService
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import java.lang.Exception
import java.util.Locale

data class Subscription(
    var id: Int,
    var slotIndex: Int,
    var icon: ImageBitmap,
    var displayName: String,
    var context: Context) {
    constructor(subscription: SubscriptionInfo, context: Context) : this(
        id = subscription.subscriptionId,
        slotIndex = subscription.simSlotIndex,
        icon = subscription.createIconBitmap(context).asImageBitmap(),
        displayName = subscription.displayName.toString(),
        context = context)

    fun setDefaultData() {
        val intent = Intent("android.intent.action.DATA_DEFAULT_SIM")
        intent.putExtra("simid", id)
        context.sendBroadcast(intent)
    }

    val isDefaultData: Boolean
        get() = id == SubscriptionManager.getDefaultDataSubscriptionId()
    val isDefaultSms: Boolean
        get() = id == SubscriptionManager.getDefaultSmsSubscriptionId()
    val isDefaultVoice: Boolean
        get() {
            try {
                return id == SubscriptionManager.getDefaultVoiceSubscriptionId()
            } catch (e: Exception) {  // This fails in Android Studio's preview.
                Log.e("isDefaultVoice", e.toString())
                return id == SubscriptionManager.INVALID_SUBSCRIPTION_ID
            }
        }
    val stateText: String
        get() {
            val rids: MutableList<Int> = mutableListOf()
            if (isDefaultData) rids.add(R.string.default_state_data)
            if (isDefaultVoice) rids.add(R.string.default_state_voice)
            if (isDefaultSms) rids.add(R.string.default_state_sms)
            if (rids.isEmpty())
                return ""
            val strings = rids.map { context.getString(it) }
            val locale = Locale.getDefault(Locale.Category.FORMAT)
            val formatter = ListFormatter.getInstance(locale)
            val default_text = formatter.format(strings)
            return context.getString(R.string.default_state, default_text)
        }

    companion object {
        fun getList(context: Context,
                    callback: ActivityResultCallback<Boolean>? = null) : List<Subscription> {
            if (!checkPermission(context, callback)) {
                return listOf()
            }
            val mgr = context.getSystemService(SubscriptionManager::class.java)
            val subscriptionInfos = mgr.activeSubscriptionInfoList
            val subscriptions = subscriptionInfos.map { Subscription(it, context) }
            return subscriptions
        }

        fun checkPermission(context: Context,
                            callback: ActivityResultCallback<Boolean>? = null) : Boolean {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED) {
                return true
            }
            if (context is ComponentActivity && callback != null) {
                val requestPermissionLauncher =
                    context.registerForActivityResult(
                        ActivityResultContracts.RequestPermission(), callback
                    )
                requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }
            return false
        }

        fun showSettings(context: Context) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS)
            context.startActivity(intent)
        }

        fun showSettingsAndCollapse(context: TileService) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivityAndCollapse(intent)
        }
    }
}
