package com.pnu.smartwalkingstickapp.ui.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.pnu.smartwalkingstickapp.R


class test : Fragment() {

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_ALL_PERMISSION = 2
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var scanning: Boolean = false
    private var devicesArr = ArrayList<BluetoothDevice>()
    private val SCAN_PERIOD = 1000
    private val handler = Handler()


    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object: ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult> ?) {
            super.onBatchScanResults(results)
            Log.d("TAG", "onScanResult: 123131231")
            results?.let {
                // results is not null
                for(result in it) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d("TAG", "onBatchScanResults: check permission")
                        return
                    }
                    if(!devicesArr.contains(result.device) && result.device.name!=null) devicesArr.add(result.device)
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // result is not null
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("TAG", "onScanResult: not permission")
                    return
                }
                if(!devicesArr.contains(it.device) && it.device.name!=null) devicesArr.add(it.device)
                Log.d("TAG", "onScanResult: $result")
                //recyclerViewAdapter.notifyDataSetChanged()
            }
            Log.d("TAG", "onScanResult: 123")
        }
    }

    //scan results
    var scanResults: ArrayList<BluetoothDevice>? = ArrayList()

    //ble adapter
    private var bleAdapter: BluetoothAdapter? = null

    // BLE Gatt
    private var bleGatt: BluetoothGatt? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanDevice(state:Boolean) = if(state) {
        handler.postDelayed({
            scanning = false
            bleAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        }, SCAN_PERIOD.toLong())
        scanning = true
        devicesArr.clear()
        Log.d("TAG", "scanDevice: start")
        bleAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
    }
    else {
        scanning = false
        Log.d("TAG", "scanDevice: stop")
        bleAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBluetooth()
        checkBluetoothPermission()

    }

    private fun checkBluetoothPermission() {
        if (!hasPermissions(requireContext(), PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }
        scanDevice(true)
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permissions granted!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(
                        requireContext(),
                        "Permissions must be granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun initBluetooth() {
        val bluetoothManager: BluetoothManager =
            getSystemService(requireContext(), BluetoothManager::class.java)!!
        bleAdapter = bluetoothManager.adapter
        if (bleAdapter == null) {
            Toast.makeText(requireContext(), "Bluetoothe 를 사용할 수 없는 모델입니다", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (bleAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
}