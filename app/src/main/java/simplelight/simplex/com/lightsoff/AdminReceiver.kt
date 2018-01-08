package simplelight.simplex.com.lightsoff

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class AdminReceiver: DeviceAdminReceiver() {
    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onEnabled(context: Context, intent: Intent) {
        println("ENABLED")
        val sharedPref = context.getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(ADMIN_STATUS, true).apply()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "DISABLE WARNING"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        showToast(context, "DISABLED")
        val sharedPref = context.getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(ADMIN_STATUS, false).apply()
    }
}