package itu.mihai.htmaebt

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlinx.android.synthetic.main.activity_main.*
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.DeviceCallback


class MainActivity : AppCompatActivity() {
    lateinit var bluetooth: Bluetooth
    private lateinit var statusTextView: TextView
    private lateinit var forwardButton: Button
    private lateinit var backwardButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var joystickView: JoystickView
    private lateinit var objectDetectedTextView: TextView
    private lateinit var objectDetectedImageView: ImageView
    var connected: Boolean = false
    var angleToSend: String = ""
    var lastAngle: String = ""
    var lastTime: Long = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        bluetooth = Bluetooth(applicationContext)
        bluetooth.setCallbackOnUI(this)
        statusTextView = findViewById(R.id.textView)
        forwardButton = findViewById(R.id.forward_button)
        backwardButton = findViewById(R.id.backward_button)
        disconnectButton = findViewById(R.id.disconnect_button)
        joystickView = findViewById(R.id.joystickView)
        objectDetectedTextView = findViewById(R.id.distance_object)
        objectDetectedImageView = findViewById(R.id.distance_object_image)
        bluetooth.setDeviceCallback(BluetoothDeviceCallBack())

        disconnectButton.setOnClickListener {
            bluetooth.disconnect()
        }

        joystickView.setOnMoveListener({ angle, strength ->
            if (connected) {
                if (angle == 0 && strength == 0)
                    angleToSend = "st90;"
                else if (angle >= 270 || angle <= 90)
                    angleToSend = "st120;"
                else if (angle in 90..270)
                    angleToSend = "st60;"

                val time = System.currentTimeMillis()
                if (time - lastTime > 1000 && lastAngle != angleToSend) {
                    lastTime = System.currentTimeMillis()
                    bluetooth.send(angleToSend)
                    bluetooth.socket.outputStream.flush()
                    lastAngle = angleToSend
                }

            }
        }, 20)

        forwardButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (connected) {
                    bluetooth.send("f;")
                }
                Toast.makeText(applicationContext, "DOWN", Toast.LENGTH_SHORT).show()
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (connected) {
                    bluetooth.send("s;")
                }
                Toast.makeText(applicationContext, "UP", Toast.LENGTH_SHORT).show()
            }
            bluetooth.socket.outputStream.flush()

            true
        }

        backwardButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (connected) {
                    bluetooth.send("b;")
                }
                Toast.makeText(applicationContext, "DOWN", Toast.LENGTH_SHORT).show()
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (connected) {
                    bluetooth.send("s;")
                }
                Toast.makeText(applicationContext, "UP", Toast.LENGTH_SHORT).show()
            }
            bluetooth.socket.outputStream.flush()

            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, DevicesActivity::class.java)
                startActivityForResult(intent, 2)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Override
    override fun onStart() {
        super.onStart()
        bluetooth.onStart()
        bluetooth.enable()
    }

    @Override
    override fun onStop() {
        super.onStop()
        bluetooth.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (!bluetooth.isConnected)
            statusTextView.text = "Not connected"
    }

    @Override
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val returnedResult = data!!.getStringExtra("device_address")
            val device = bluetooth.pairedDevices.first { bluetoothDevice -> bluetoothDevice.address == returnedResult }
            connect(device)
        }
    }

    private fun connect(device: BluetoothDevice?) {
        bluetooth.connectToDevice(device)
    }

    inner class BluetoothDeviceCallBack : DeviceCallback {
        override fun onDeviceConnected(device: BluetoothDevice) {
            val text = "Connected ${device.name}"
            statusTextView.text = "Connected to ${device.name}"
            connected = true
            runOnUiThread {
                Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
            }

        }

        override fun onDeviceDisconnected(device: BluetoothDevice, message: String) {
            runOnUiThread {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
            connected = false
            statusTextView.text = "Not connected"
        }

        @SuppressLint("SetTextI18n")
        override fun onMessage(message: String) {
            if (message.startsWith("D")) {
                val distance = message.subSequence(1, message.length).toString().toInt()
                objectDetectedTextView.text = "Distance: $distance"
                if (distance <= 100) {
                    objectDetectedImageView.setColorFilter(applicationContext.resources.getColor(R.color.red));
                } else {
                    objectDetectedImageView.setColorFilter(applicationContext.resources.getColor(R.color.green));
                }

            }
        }

        override fun onError(message: String) {
            runOnUiThread {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onConnectError(device: BluetoothDevice, message: String) {
            runOnUiThread {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}


