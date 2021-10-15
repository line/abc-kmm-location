package com.linecorp.abc.sharedlocation.android

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.linecorp.abc.location.ABCLocation
import com.linecorp.abc.location.android.R
import com.linecorp.abc.location.extension.processRequestPermissionsResult

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById<Button>(R.id.text_view)

        /* Retrieve Current Location */
        findViewById<Button>(R.id.current_location_button).run {
            setOnClickListener {
                ABCLocation
                    .onPermissionUpdated("SingleRequest") { isGranted ->
                        if (!isGranted) {
                            showDialog("onPermissionUpdated NotGranted")
                        }
                    }
                    .onLocationUnavailable("SingleRequest") {
                        showDialog("onLocationUnavailable")
                    }
                    .currentLocation { data ->
                        Log.d("Single", data.coordinates.toString())
                        textView.text = "Single: " + data.coordinates.toString() + "\n" + textView.text
                    }
            }
        }

        /* Start Polling */
        findViewById<Button>(R.id.polling_location_button).run {
            setOnClickListener {
                ABCLocation
                    .onPermissionUpdated(this@MainActivity) { isGranted ->
                        if (!isGranted) {
                            showDialog("onPermissionUpdated NotGranted")
                        }
                    }
                    .onLocationUnavailable(this@MainActivity) {
                        showDialog("onLocationUnavailable")
                    }
                    .onLocationUpdated(this@MainActivity) { data ->
                        Log.d("Continuously", data.coordinates.toString())
                        textView.text = "Continuously: " + data.coordinates.toString() + "\n" + textView.text
                    }
                    .startLocationUpdating()
            }
        }

        /* Stop Polling */
        findViewById<Button>(R.id.stop_polling_button).run {
            setOnClickListener {
                textView.text = "Location"
                ABCLocation.removeListeners(this@MainActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ABCLocation.stopLocationUpdating()
    }

    private fun showDialog(message: String) {
        val myAlertBuilder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        myAlertBuilder.setTitle("")
        myAlertBuilder.setMessage(message)
        myAlertBuilder.setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ -> })
        myAlertBuilder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ABCLocation.processRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}