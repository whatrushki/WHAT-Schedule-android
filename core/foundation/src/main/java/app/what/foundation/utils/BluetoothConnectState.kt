package app.what.foundation.utils

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class BluetoothStateReceiver(private val onBluetoothStateChanged: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        val isEnabled = when (state) {
            BluetoothAdapter.STATE_ON -> true
            BluetoothAdapter.STATE_OFF -> false
            else -> false // Ignore other states
        }
        onBluetoothStateChanged(isEnabled)
    }
}

@Composable
fun bluetoothConnectState(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isBluetoothEnabled by remember { mutableStateOf(false) }
    val bluetoothStateReceiver =
        remember { BluetoothStateReceiver { isEnabled -> isBluetoothEnabled = isEnabled } }

    LaunchedEffect(Unit) {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)

        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                context.unregisterReceiver(bluetoothStateReceiver)
            }
        })

        //Initial check
        isBluetoothEnabled = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
    }

    return isBluetoothEnabled
}