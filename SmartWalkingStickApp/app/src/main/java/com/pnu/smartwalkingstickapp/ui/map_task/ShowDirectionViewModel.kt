package com.pnu.smartwalkingstickapp.ui.map_task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnu.smartwalkingstickapp.ui.map_task.response.path.FeatureCollection
import com.pnu.smartwalkingstickapp.ui.map_task.response.search.Poi
import com.pnu.smartwalkingstickapp.ui.map_task.utility.RetrofitUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowDirectionViewModel : ViewModel() {

    var isNavigating : Boolean = false

    private var curIndex: Int = 1

    private var _featureCollection = MutableLiveData<FeatureCollection>()
    val featureCollection: LiveData<FeatureCollection> get() = _featureCollection

    private var _curPosition = MutableLiveData<Pair<Double, Double>>()
    val curPosition: LiveData<Pair<Double, Double>> get() = _curPosition

    fun setPosition(pos: Pair<Double, Double>) {
        _curPosition.value = pos
    }

    fun setCurIndex(idx: Int) {
        curIndex = idx
    }

    fun getCurIndex(): Int {
        return curIndex
    }

    fun getPathInformation(startLon : Float, startLat : Float, destPoi: Poi) {
        viewModelScope.launch {
            try {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val response =
                        RetrofitUtil.apiService.getPath(
                            startX = startLon,
                            startY = startLat,
                            startName = "현재위치",
                            endX = destPoi.frontLon,
                            endY = destPoi.frontLat,
                            endName = destPoi.name!!
                        )
                    if (response.isSuccessful) {
                        val body = response.body()
                        _featureCollection.value = body!!
                    }
                }
            } catch (e: Exception) {
            }
        }

    }

}