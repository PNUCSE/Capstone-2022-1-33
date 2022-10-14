package com.pnu.smartwalkingstickapp.ui.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pnu.smartwalkingstickapp.MainActivity
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.utils.TTS
import com.pnu.smartwalkingstickapp.utils.WrappedDialogBasicTwoButton
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*


class BluetoothFragment : Fragment() {
    val bluetoothViewModel: BluetoothViewModel by activityViewModels()
    private lateinit var sharedPref: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var emergencyCallNum: TextView
    private lateinit var connectedThread: ConnectedThread

    private lateinit var textToSpeech: TTS

    private lateinit var connectedDeviceCell: ConstraintLayout
    private lateinit var connectedDeviceName: TextView
    private var flag = false

    //private var binding: FragmentBluetoothBinding? = null
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_ALL_PERMISSION = 2
    private val REQUEST_CALL = 4
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )

    private val MESSAGE_READ = 2 // used in bluetooth handler to identify message update
    private val CONNECTING_STATUS = 3 // used in bluetooth handler to identify message status


    var BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // BLE Gatt 추가하기

    private var bleGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanning: Boolean = false
    private var devicesArr = ArrayList<BluetoothDevice>()
    private val SCAN_PERIOD = 1000
    private var handler = Handler()
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private val mLeScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("scanCallback", "BLE Scan Failed : " + errorCode)
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.let {
                // results is not null
                for (result in it) {
                    if (!devicesArr.contains(result.device) && result.device.name != null) devicesArr.add(
                        result.device
                    )
                }

            }
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let {
                // result is not null
                if (!devicesArr.contains(it.device) && it.device.name != null) devicesArr.add(it.device)
                recyclerViewAdapter.notifyDataSetChanged()
            }
        }

    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanDevice(state: Boolean) = if (state) {
        handler.postDelayed({
            scanning = false
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
        }, SCAN_PERIOD)
        scanning = true
        devicesArr.clear()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)
    } else {
        scanning = false
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
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

    // Permission check
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        Log.v("juyong: 1-", requestCode.toString())
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

    @SuppressLint("MissingPermission")
    fun bluetoothOnOff(isChecked: Boolean) {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.d("bluetoothAdapter", "Device doesn't support Bluetooth")
        } else {
            if (isChecked) { // 블루투스 꺼져 있으면 블루투스 활성화
                recyclerView.visibility = View.VISIBLE
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else { // 블루투스 켜져있으면 블루투스 비활성화
                bluetoothAdapter?.disable()
                recyclerView.visibility = View.INVISIBLE
                connectedDeviceCell.visibility = View.GONE
                connectedDeviceName.text = ""
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> scanDevice(true)
        }
    }

    class RecyclerViewAdapter(private val myDataset: ArrayList<BluetoothDevice>) :
        RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

        var mListener: OnItemClickListener? = null

        interface OnItemClickListener {
            fun onClick(view: View, position: Int)
        }

        inner class MyViewHolder(val linearView: LinearLayout) : RecyclerView.ViewHolder(linearView)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerViewAdapter.MyViewHolder {
            // create a new view
            val linearView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false) as LinearLayout
            return MyViewHolder(linearView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val itemName: TextView = holder.linearView.findViewById(R.id.item_name)
            val itemAddress: TextView = holder.linearView.findViewById(R.id.item_address)
            itemName.text = myDataset[position].name
            itemAddress.text = "연결안됨"
            // 아래부터 추가코드
            if (mListener != null) {
                holder?.itemView?.setOnClickListener { v ->
                    mListener?.onClick(v, position)
                }
            }
        }

        override fun getItemCount() = myDataset.size
    }


    private fun Handler.postDelayed(function: () -> Unit?, scanPeriod: Int) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

    @SuppressLint("HandlerLeak")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val act = activity as MainActivity
        act.showComponent()
        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)!!
        textToSpeech = context?.let { TTS(it) }!!

        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_READ) {
                    try {
                        val readMessage = String((msg.obj as ByteArray), Charsets.UTF_8)
                        when (readMessage[0]) {
                            '1' -> bluetoothViewModel.runEmergencyCall()
                            '2' -> bluetoothViewModel.runCamera("detect")
                            '3' -> bluetoothViewModel.runCamera("text")
                            else -> {
                            }
                        }
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        connectedDeviceName = view.findViewById<TextView>(R.id.connected_device_name)
        connectedDeviceCell = view.findViewById<ConstraintLayout>(R.id.connected_device_cell)
        recyclerViewAdapter = RecyclerViewAdapter(devicesArr)
        recyclerViewAdapter.mListener = object : RecyclerViewAdapter.OnItemClickListener {
            @SuppressLint("MissingPermission")
            override fun onClick(view: View, position: Int) {
                scanDevice(false) // scan 중지

                // Spawn a new thread to avoid blocking the GUI one
                object : Thread() {
                    override fun run() {
                        var fail = false
                        val device: BluetoothDevice = devicesArr[position]
                        var mSocket: BluetoothSocket? = null
                        try {
                            mSocket = createBluetoothSocket(device)
                        } catch (e: IOException) {
                            fail = true
                            Toast.makeText(context, "Socket creation failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                        // Establish the Bluetooth socket connection.
                        try {
                            mSocket?.connect()
                        } catch (e: IOException) {
                            try {
                                fail = true
                                mSocket?.close()
                                handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget()
                            } catch (e2: IOException) {
                                //insert code to deal with this
                                Toast.makeText(
                                    context,
                                    "Socket creation failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (!fail) {
                            connectedDeviceCell.visibility = View.VISIBLE
                            connectedDeviceName.text = device.name
                            connectedThread = ConnectedThread(mSocket!!, handler)
                            connectedThread.start()
                            flag = true
                            handler.obtainMessage(CONNECTING_STATUS, 1, -1, name).sendToTarget()
                            textToSpeech.play("연결되었습니다.")
                        }
                    }
                }.start()
            }
        }
        val bleOnOffBtn: Switch = view.findViewById(R.id.ble_on_off_btn)
        val emergencyCallCell: ConstraintLayout = view.findViewById(R.id.emergency_call_cell)
        val findCaneCell: ConstraintLayout = view.findViewById(R.id.find_cane_cell)

        emergencyCallNum = view.findViewById(R.id.emergency_call_number)
        initEmergencyCallView()

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        viewManager = LinearLayoutManager(requireContext())
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = viewManager
            adapter = recyclerViewAdapter
        }

        bleOnOffBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !hasPermissions(requireContext(), PERMISSIONS)) {
                requestBluetoothPermission()
                bluetoothViewModel.onRequestBluetoothPermissionsResult.observe(viewLifecycleOwner) {
                    if (it) {
                        bluetoothOnOff(isChecked)
                    }
                }
            }
            else {
                bluetoothOnOff(isChecked)
            }
        }

        emergencyCallCell.setOnClickListener {
            when (val phonePermission =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)) {
                PackageManager.PERMISSION_GRANTED -> showDialog()
                else -> requestCallPhonePermission()
            }
            bluetoothViewModel.onRequestCallPermissionsResult.observe(viewLifecycleOwner) {
                if (it) {
                    showDialog()
                }
            }
        }

        findCaneCell.setOnClickListener {
            if (flag) {
                val writeMessage = "f".toByteArray()
                connectedThread.write(writeMessage)
            }
        }
    }
    private fun requestBluetoothPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            PERMISSIONS,
            REQUEST_ALL_PERMISSION
        )
    }
    private fun requestCallPhonePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CALL_PHONE),
            REQUEST_CALL
        )
    }

    private fun initEmergencyCallView() {
        val defaultValue = "없음"
        val phoneNum = sharedPref.getString("number", defaultValue)
        emergencyCallNum.text = phoneNum
    }

    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID)
        //creates secure outgoing connection with BT device using UUID
    }

    private fun showDialog() {
        val registerPhoneNumDialog = context?.let { it ->
            WrappedDialogBasicTwoButton(it).apply {
                clickListener = object : WrappedDialogBasicTwoButton.DialogButtonClickListener {
                    override fun dialogCloseClickListener() {
                        dismiss()
                    }

                    override fun dialogCustomClickListener() {
                        with(sharedPref.edit()) {
                            putString("number", binding.dialogContent.text.toString())
                            apply()
                            Toast.makeText(context, "번호가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                            dismiss()
                            initEmergencyCallView()
                        }
                    }
                }
            }
        }
        registerPhoneNumDialog?.show()
    }
}