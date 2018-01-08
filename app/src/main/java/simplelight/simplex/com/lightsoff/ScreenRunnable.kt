package simplelight.simplex.com.lightsoff

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.stericson.RootShell.execution.Command
import com.stericson.RootTools.RootTools

class ScreenRunnable constructor(context_from: Context): Runnable {
    var context = context_from

    override fun run() {
        val sharedPref = context.getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        val adminStatus = sharedPref.getBoolean(ADMIN_STATUS, false)
        val rootStatus = sharedPref.getBoolean(ROOT, false)

        if(adminStatus) {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.lockNow()

        } else if(rootStatus) {

            val command = Command(0, "adb shell input keyevent 26")

            try {
                RootTools.getShell(true).add(command)
            } catch (e: Exception) {

                val editor = sharedPref.edit()
                editor.putBoolean(ROOT, false).apply()

                e.printStackTrace()

            }
        }
    }
}