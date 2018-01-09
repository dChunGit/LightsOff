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
import android.view.Menu
import android.view.MenuItem
import com.shawnlin.numberpicker.NumberPicker
import com.stericson.RootTools.RootTools

class MainActivity : AppCompatActivity() {
    private lateinit var np: NumberPicker
    private val ACTIVATION_REQUEST = 17
    private lateinit var adminReciever: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar  = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        val sharedPref = getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(SAVED_TIME, 5).apply()


        np = findViewById(R.id.number_picker)
        adminReciever = ComponentName(this, AdminReceiver::class.java)

        val button = findViewById<AppCompatButton>(R.id.button)
        button.setOnClickListener {
            val thread = Thread(Runnable {
                checkPermissions()
                val intent = Intent(applicationContext, LightsService::class.java)
                intent.putExtra("time", (np.value).toLong())
                println(np.value)
                editor.putInt(SAVED_TIME, np.value).apply()
                startService(intent)
            })
            thread.start()
        }
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
        menu.findItem(R.id.root).setChecked(sharedPref.getBoolean(ROOT, false))
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
                item.setChecked(true)

                if (RootTools.isAccessGiven()) {
                    editor.putBoolean(ROOT, true).apply()
                } else {
                    editor.putBoolean(ROOT, false).apply()
                    item.setChecked(false)
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
