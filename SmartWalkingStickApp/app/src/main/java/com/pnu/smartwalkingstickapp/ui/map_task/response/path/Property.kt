package com.pnu.smartwalkingstickapp.ui.map_task.response.path

data class Property(
    val index : Int,
    val pointIndex : Int,
    val name : String,
    val guidePointName : String,
    val description : String,
    val direction : String,
    val intersectionName : String,
    val nearPoiName : String,
    val nearPoiX : String,
    val nearPoiY : String,
    val crossName : String,
    val turnType : Int,
    val pointType : String
)
