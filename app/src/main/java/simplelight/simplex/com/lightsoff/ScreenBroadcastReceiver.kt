package simplelight.simplex.com.lightsoff

import android.widget.Toast
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.provider.Settings


class ScreenBroadcastReceiver constructor(service: LightsService): BroadcastReceiver() {
    private var lightService = service

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            println("Screen off")
            /*Settings.System.putInt(context.contentResolver,Settings.System.SCREEN_OFF_TIMEOUT, defaultTime)
            println(Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 60000))*/

            val lightIntent = Intent(context, LightsService::class.java)
            lightIntent.action = ServiceParams.STOP_SERVICE.toString()
            lightService.onStartCommand(lightIntent, 0, 0)

        }
    }
}