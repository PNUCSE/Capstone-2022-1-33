package com.pnu.smartwalkingstickapp.ui.bluetooth

import androidx.core.widget.ListViewAutoScrollHelper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel() : ViewModel() {

    private val _onReceiveRunEmergencyCall = MutableLiveData<Boolean>(false)
    val onReceiveRunEmergencyCall: LiveData<Boolean> = _onReceiveRunEmergencyCall

    private val _onReceiveRunCamera = MutableLiveData<String>("")
    val onReceiveRunCamera: LiveData<String> = _onReceiveRunCamera

    private val _onRequestCameraPermissionsResult = MutableLiveData<Boolean>(false)
    val onRequestCameraPermission: LiveData<Boolean> = _onRequestCameraPermissionsResult

    private val _onRequestCallPermissionsResult = MutableLiveData<Boolean>(false)
    val onRequestCallPermissionsResult:LiveData<Boolean> = _onRequestCallPermissionsResult

    private val _onRequestBluetoothPermissionsResult = MutableLiveData<Boolean>(false)
    val onRequestBluetoothPermissionsResult:LiveData<Boolean> = _onRequestBluetoothPermissionsResult

    private val _onMapPermissionResult = MutableLiveData<Boolean>(false)
    val onMapPermissionResult:LiveData<Boolean> = _onMapPermissionResult

    fun setOnMapPermissionResult() {
        _onMapPermissionResult.value = true
    }

    fun runEmergencyCall() {
        _onReceiveRunEmergencyCall.postValue(true)
    }

    fun runCamera(feature: String) {
        _onReceiveRunCamera.postValue(feature)
    }

    fun setRequestCameraPermissionsResult(flag: Boolean) {
        _onRequestCameraPermissionsResult.postValue(flag)
    }

    fun setRequestCallPermissionsResult(flag: Boolean) {
        _onRequestCallPermissionsResult.postValue(flag)
    }

    fun setRequestBluetoothPermissionsResult(flag: Boolean) {
        _onRequestBluetoothPermissionsResult.postValue(flag)
    }


}