package simplelight.simplex.com.lightsoff

import android.annotation.TargetApi
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.fxn769.Numpad
import com.shawnlin.numberpicker.NumberPicker
import com.stericson.RootTools.RootTools
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var np: Numpad
    private lateinit var htext: TextView
    private lateinit var mtext: TextView
    private lateinit var stext: TextView
    private val ACTIVATION_REQUEST = 17
    private lateinit var adminReciever: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar  = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        htext = findViewById(R.id.hr)
        mtext = findViewById(R.id.min)
        stext = findViewById(R.id.sec)

        val sharedPref = getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(SAVED_TIME, 5).apply()

        adminReciever = ComponentName(this, AdminReceiver::class.java)

        np = findViewById(R.id.num)
        np.setOnTextChangeListner({ text: String, digits_remaining: Int ->
            Log.d("input", text + "  " + digits_remaining)
            setTextArray(getTextDisplay(text))
        })


        val button = findViewById<AppCompatButton>(R.id.button)
        button.setOnClickListener {
            val thread = Thread(Runnable {
                checkPermissions()
                val intent = Intent(applicationContext, LightsService::class.java)
                val time = getTime()
                intent.putExtra("time", time)
                println(time)
                editor.putInt(SAVED_TIME, time.toInt()).apply()
                startService(intent)
            })
            thread.start()
        }
    }

    private fun getTime(): Long {
        println(htext.text.toString() + " " + mtext.text.toString() + " " + stext.text.toString())
        return htext.text.toString().toLong() * 3600 + mtext.text.toString().toLong() * 60 + stext.text.toString().toLong()
    }

    private fun setTextArray(timeArray: Array<String>) {
        htext.text = when(timeArray[0].length) {1 -> "0" + timeArray[0] else -> timeArray[0]}
        mtext.text = when(timeArray[1].length) {1 -> "0" + timeArray[1] else -> timeArray[1]}
        stext.text = when(timeArray[2].length) {1 -> "0" + timeArray[2] else -> timeArray[2]}
    }

    private fun getTextDisplay(text: String?): Array<String> {
        text?.let {
            return when (text.length) {
                in 1..2 -> arrayOf("00", "00", text)
                in 3..4 -> arrayOf("00",
                        text.substring(0, text.length - 2),
                        text.substring(text.length - 2, text.length))
                in 5..6 -> arrayOf(text.substring(0, text.length - 4),
                        text.substring(text.length - 4, text.length - 2),
                        text.substring(text.length - 2, text.length))
                else -> arrayOf("00", "00", "00")
            }
        }

        return arrayOf("00", "00", "00")
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {

        println("Checking permissions")
        val sharedPref = getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        val adminStatus = sharedPref.getBoolean(ADMIN_STATUS, false)
        val rootStatus = sharedPref.getBoolean(ROOT, false)

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, AdminReceiver::class.java)

        if(!rootStatus && (!adminStatus || !dpm.isAdminActive(admin))) {
            println("Starting device admin intent")
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReciever)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Device Administrator allows this app to turn off your screen")
            startActivityForResult(intent, ACTIVATION_REQUEST)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val sharedPref = getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        menu.findItem(R.id.root).isChecked = sharedPref.getBoolean(ROOT, false)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.root -> {
                val sharedPref = getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                item.isChecked = true

                if (RootTools.isAccessGiven()) {
                    editor.putBoolean(ROOT, true).apply()
                } else {
                    editor.putBoolean(ROOT, false).apply()
                    item.isChecked = false
                }


            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ACTIVATION_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    println("Enabled")
                }
            }
        }
    }
}
