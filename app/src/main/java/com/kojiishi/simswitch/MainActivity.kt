package com.kojiishi.simswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.telephony.SubscriptionManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kojiishi.simswitch.ui.theme.SimSwitchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(
            refresh_receiver_,
            IntentFilter().apply {
                addAction("android.intent.action.ACTION_DEFAULT_VOICE_SUBSCRIPTION_CHANGED")
                addAction(SubscriptionManager.ACTION_DEFAULT_SMS_SUBSCRIPTION_CHANGED)
                addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")
            })
        refresh()
    }

    fun refresh() {
        val subscriptions = Subscription.getList(this
        ) { isGranted: Boolean ->
            if (isGranted) {
                this.refresh()
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }
        setContent {
            SimSwitchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { SubscriptionListView(subscriptions) }
            }
        }
    }

    inner class RefreshReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refresh()
        }
    }
    private val refresh_receiver_ = RefreshReceiver()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListView(subscriptions: List<Subscription>) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.app_name)) },
                actions = {
                    IconButton(onClick = {
                        if (context is MainActivity) {
                            context.refresh()
                        }
                    }) { Icon(Icons.Default.Refresh, context.getString(R.string.refresh)) }
                }
            )
        }
    ) {
        contentPadding -> LazyColumn(contentPadding = contentPadding) {
            items(subscriptions) { subscription -> SubscriptionView(subscription) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionView(subscription: Subscription) {
    val context = LocalContext.current
    ListItem(
        headlineText = { Text(subscription.displayName) },
        supportingText = { Text(subscription.stateText) },
        leadingContent = {
            Image(
                bitmap = subscription.icon,
                contentDescription = context.getString(R.string.sim_icon_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier
            .clickable { Subscription.showSettings(context) }
    )
}

@Preview(showBackground = true)
@Composable
fun SubscriptionListViewPreview() {
    val context = LocalContext.current
    val iconBitmap = Bitmap.createBitmap(10, 15, Bitmap.Config.ARGB_8888)
    iconBitmap.eraseColor(Color.GREEN)
    val icon = iconBitmap.asImageBitmap()
    val id = SubscriptionManager.getDefaultSubscriptionId()
    val subscriptions = listOf(
        Subscription(id, 1, icon, "SIM $id", context),
        Subscription(id + 1, 2, icon, "SIM 2", context),
        Subscription(id + 2, 2, icon, "Some long name", context),
        Subscription(id + 3, 2, icon, "Some long long long long long name", context),
        Subscription(id, 2, icon, "Some long long long long long name", context),
    )
    SimSwitchTheme {
        Surface {
            SubscriptionListView(subscriptions)
        }
    }
}
