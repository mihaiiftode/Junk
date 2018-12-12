package itu.mihai.htmaebt

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import java.util.ArrayList
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.DiscoveryCallback
import java.lang.Compiler.enable


class DevicesActivity : AppCompatActivity() {

    private lateinit var pairedDeviceList: ListView
    private lateinit var pairedListAdapter: ArrayAdapter<String>
    private lateinit var bluetooth: Bluetooth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)
        bluetooth = Bluetooth(applicationContext)

        pairedDeviceList = findViewById(R.id.activity_scan_paired_list)
        pairedListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())
        pairedDeviceList.adapter = pairedListAdapter
        pairedDeviceList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("device_address", bluetooth.pairedDevices[i].address)
            Toast.makeText(applicationContext, "Connecting to ${bluetooth.pairedDevices[i].name}", Toast.LENGTH_SHORT).show()

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    fun getPairedDevices() {
        val items = ArrayList<String>()
        for (device in bluetooth.pairedDevices) {
            items.add(device.address + " : " + device.name)
        }

        pairedListAdapter.addAll(items)
        pairedListAdapter.notifyDataSetChanged()
    }

    @Override
    override fun onStart() {
        super.onStart()
        bluetooth.onStart()
        bluetooth.enable()
        getPairedDevices()

    }

    @Override
    override fun onStop() {
        super.onStop()
        bluetooth.onStop()
    }
}
