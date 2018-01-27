package simplelight.simplex.com.lightsoff

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import android.content.IntentFilter
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LightsService: Service() {
    private var postChannel = "post_channel"
    private var notificationId = 1
    private var timer = 0L
    private var serviceRunning = false
    private lateinit var runnable: Runnable
    private lateinit var mScreenStateReceiver: ScreenBroadcastReceiver
    private lateinit var scheduler: ScheduledExecutorService
    private lateinit var handler: Handler
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var scheduledfuture: ScheduledFuture<*>

    override fun onCreate() {
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show()
        serviceRunning = true
        mScreenStateReceiver = ScreenBroadcastReceiver(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val intentEnd = Intent(this, LightsService::class.java)
        if(serviceRunning) {
            intentEnd.action = ServiceParams.STOP_SERVICE.toString()
        }
        val pendingIntentEnd = PendingIntent.getService(this, 10, intentEnd, 0)

        notificationBuilder = NotificationCompat.Builder(this, postChannel)
                .setContentTitle("Lights Off")
                .setContentText("Time Remaining:")
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.drawable.ic_tile, "CANCEL", pendingIntentEnd)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)

        startForeground(notificationId, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        println("Service Starting " + intent.action)
        val screenStateFilter = IntentFilter()
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mScreenStateReceiver, screenStateFilter)

        val timeout = intent.getLongExtra("time", 0)

        if(timeout != 0L) {
            scheduler = Executors.newScheduledThreadPool(1)
            println(timeout)
            timer = timeout
            scheduledfuture = scheduler.schedule(ScreenRunnable(this), timeout, TimeUnit.SECONDS)

            handler = Handler()
            runnable = Runnable {
                val timeString = when (timer) {
                    in 0..60 -> "Time Remaining: " + timer + " seconds"
                    else -> "Time Remaining: " + (timer/60) + " minutes"
                }

                Log.d("Handler", "Time Left = " + timer)

                val intentEnd = Intent(this, LightsService::class.java)
                if(serviceRunning) {
                    intentEnd.action = ServiceParams.STOP_SERVICE.toString()
                }
                val pendingIntentEnd = PendingIntent.getService(this, 10, intentEnd, 0)

                notificationBuilder = NotificationCompat.Builder(this, postChannel)
                        .setContentTitle("Lights Off")
                        .setContentText(timeString)
                        .addAction(R.drawable.ic_tile, "CANCEL", pendingIntentEnd)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                notificationManager.notify(notificationId, notificationBuilder.build())

                timer -= 10
                if(timer > 0) {
                    handler.postDelayed(runnable, 10000)
                }
            }
            handler.post(runnable)
        }

        if (intent.action == ServiceParams.STOP_SERVICE.toString()) {
            println("stopping")
            handler.removeCallbacks(runnable)
            scheduledfuture.cancel(true)
            scheduler.shutdownNow()
            stopSelf()
            return START_STICKY
        }

        if (intent.action == ServiceParams.START_FOREGROUND.toString()) {
            println("starting")
            return START_STICKY
        }

        if (intent.action == ServiceParams.STOP_FOREGROUND.toString()) {
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
        serviceRunning = false
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}
