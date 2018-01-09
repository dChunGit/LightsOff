package simplelight.simplex.com.lightsoff

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.widget.Toast
import com.stericson.RootShell.execution.Command
import com.stericson.RootTools.RootTools

class ScreenRunnable constructor(context_from: Context): Runnable {
    var context = context_from

    override fun run() {
        println("Runnable started")

        val sharedPref = context.getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        val adminStatus = sharedPref.getBoolean(ADMIN_STATUS, false)
        val rootStatus = sharedPref.getBoolean(ROOT, false)
        println("" + adminStatus + " " + rootStatus)

        if(adminStatus) {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            devicePolicyManager.lockNow()

        } else if(rootStatus && RootTools.isAccessGiven()) {
            println("Root Method")

            val command = Command(0, " input keyevent 26")

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