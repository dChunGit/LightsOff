package simplelight.simplex.com.lightsoff

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import android.content.IntentFilter
import android.provider.Settings
import android.app.admin.DevicePolicyManager
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * Created by dwsch on 7/30/2017.
 */

class LightsService: Service() {
    private var postChannel = "post_channel"
    private var notificationId = 1
    private lateinit var mScreenStateReceiver: ScreenBroadcastReceiver
    private lateinit var scheduler: ScheduledExecutorService

    override fun onCreate() {
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show()
        //timeout = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 60000)
        //println(timeout)
        mScreenStateReceiver = ScreenBroadcastReceiver(this)

        var intent = Intent(this, MainActivity::class.java)
        var pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        var notification = NotificationCompat.Builder(this, postChannel)
                .setContentTitle("Test")
                .setContentText("Lights Off")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(notificationId, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        val screenStateFilter = IntentFilter()
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mScreenStateReceiver, screenStateFilter)

        var timeout = intent.getLongExtra("time", 0)

        scheduler = Executors.newScheduledThreadPool(1)
        println(timeout)
        scheduler.schedule(ScreenRunnable(this), timeout, TimeUnit.MINUTES)
        /*Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 10)
        println(Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 60000))*/

        if (intent.action == ServiceParams.STOP_SERVICE.toString()) {
            println("stopping")
            stopSelf()
            return START_STICKY
        }

        if (intent.action == ServiceParams.START_FOREGROUND.toString()) {

            // startForeground(...);

            return START_STICKY
        }

        if (intent.action ==ServiceParams.STOP_FOREGROUND.toString()) {
            stopForeground(true)
            return START_STICKY
        }

        // If we get killed, after returning from here, restart
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        unregisterReceiver(mScreenStateReceiver)
        //stopForeground(true)
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}
