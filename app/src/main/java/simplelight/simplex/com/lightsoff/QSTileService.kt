package simplelight.simplex.com.lightsoff

import android.content.Context
import android.service.quicksettings.TileService
import android.service.quicksettings.Tile
import android.content.Intent
import android.widget.Toast
import android.graphics.drawable.Icon
import android.support.annotation.RequiresApi
import android.util.Log
import com.stericson.RootTools.RootTools

@RequiresApi(api = 24)
class QSTileService: TileService() {
    private val TAG = "QSTILE"

    override fun onTileAdded() {
        Log.i(TAG, "Method: onTileAdded()")
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.i(TAG, "Method: onTileRemoved()")
    }

    override fun onStartListening() {
        super.onStartListening()
        changeTileState(qsTile.state)
        Log.i(TAG, "Method: onStartListening()")
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.i(TAG, "Method: onStopListening()")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Method: onCreate()")
    }

    override fun onClick() {
        super.onClick()
        Log.i(TAG, "Tile State: " + qsTile.state)

        if (!isLocked) {
            updateTile()
        } else {
            unlockAndRun { updateTile() }
        }
    }

    private fun updateTile() {
        if (Tile.STATE_ACTIVE == qsTile.state) {
            Toast.makeText(this, "New State: INACTIVE", Toast.LENGTH_SHORT).show()
            changeTileState(Tile.STATE_INACTIVE)

        } else if (Tile.STATE_INACTIVE == qsTile.state) {
            Toast.makeText(this, "New State: ACTIVE", Toast.LENGTH_SHORT).show()
            changeTileState(Tile.STATE_ACTIVE)

            val sharedPref = getSharedPreferences(DEVICE_STORE, Context.MODE_PRIVATE)
            if (sharedPref.getBoolean(ADMIN_STATUS, false) ||
                    (sharedPref.getBoolean(ROOT, false) && RootTools.isAccessGiven())) {

                val intent = Intent(applicationContext, LightsService::class.java)
                val time = sharedPref.getInt(SAVED_TIME, 5)
                intent.putExtra("time", time.toLong())
                startService(intent)

            } else {
                Toast.makeText(this, "Enable Device Administrator or Root " +
                        "Before Using Tile", Toast.LENGTH_LONG).show()
            }
            //startActivityAndCollapse()

        }
    }

    private fun changeTileState(newState: Int) {
        qsTile.icon = Icon.createWithResource(this,
                if (newState == Tile.STATE_INACTIVE) R.drawable.ic_tile else R.drawable.ic_tile)
        qsTile.state = newState
        qsTile.updateTile()
    }
}