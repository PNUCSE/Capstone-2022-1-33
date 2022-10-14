package com.pnu.smartwalkingstickapp.ui.map_task

import com.pnu.smartwalkingstickapp.ui.map_task.response.search.Poi

interface OnPoiDataItemClick {
    fun sendData(data : Poi)
}