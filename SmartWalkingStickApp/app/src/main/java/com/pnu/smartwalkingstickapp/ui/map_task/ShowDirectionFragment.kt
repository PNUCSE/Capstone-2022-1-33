package com.pnu.smartwalkingstickapp.ui.map_task

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.pnu.smartwalkingstickapp.MainActivity
import com.pnu.smartwalkingstickapp.databinding.FragmentShowDirectionBinding
import com.pnu.smartwalkingstickapp.ui.bluetooth.BluetoothViewModel
import com.pnu.smartwalkingstickapp.ui.map_task.response.path.Feature
import com.pnu.smartwalkingstickapp.ui.map_task.utility.Key
import com.pnu.smartwalkingstickapp.ui.map_task.utility.RetrofitUtil
import com.pnu.smartwalkingstickapp.utils.TTS
import com.skt.Tmap.TMapGpsManager
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import kotlinx.coroutines.*
import java.lang.Math.toRadians
import kotlin.coroutines.CoroutineContext
import kotlin.math.pow


class ShowDirectionFragment : Fragment(), CoroutineScope {

    private val REQUEST_PERMISSION_LOCATION = 101
    private lateinit var ttsSpeaker : TTS

    private var isInit = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tMapView: TMapView
    private lateinit var tMapGps: TMapGpsManager

    private val mapViewModel: MapViewModel by activityViewModels()
    private val showDirectionViewModel: ShowDirectionViewModel by viewModels()
    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    private var _binding: FragmentShowDirectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val TAG = "jiwoo"

    private lateinit var adapter: PathDataRecyclerViewAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for requireContext fragment
        job = Job()
        _binding = FragmentShowDirectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ttsSpeaker = TTS(requireContext())
        showDirectionViewModel.setCurIndex(1)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        initRcvAdapter()
        initNavigateButton()

        val act = activity as MainActivity
        act.showComponent()
        act.hideNav()
        if(showDirectionViewModel.isNavigating) {
            Log.d(TAG, "onViewCreated: ")
            ttsSpeaker.play("경로를 재탐색중입니다. 잠시만 기다려주세요.")
        }
        else{
            Log.d(TAG, "onViewCreated: 12")
            ttsSpeaker.play("경로를 검색중입니다. 잠시만 기다려주세요.")
        }

        if(showDirectionViewModel.isNavigating) {
            binding.btnStartNavigate.text = "길안내 종료"
        }

        if (checkLocationPermission()) {
            bluetoothViewModel.setOnMapPermissionResult()
            startNavigating()
        }

        bluetoothViewModel.onMapPermissionResult.observe(viewLifecycleOwner) {
            if (it) {
                startNavigating()
            } else {
                Toast.makeText(requireContext(), "권한이 거부되어있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initMapView() {
        tMapView = TMapView(requireContext())
        tMapView.setSKTMapApiKey(Key.TMAP_API)

        tMapView.zoomLevel = 17;
        tMapView.setIconVisibility(true);
        tMapView.mapType = TMapView.MAPTYPE_STANDARD
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN)

        binding.linearLayoutMap.addView(tMapView)

        // Request For GPS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_LOCATION
            )
        }

        // GPS using T Map
        tMapGps = TMapGpsManager(requireContext());

        // Initial Setting
        tMapGps.minTime = 1000
        tMapGps.minDistance = 10F
        tMapGps.provider = "network";

        tMapView.setCenterPoint(
            showDirectionViewModel.curPosition.value!!.first,
            showDirectionViewModel.curPosition.value!!.second
        )
        tMapView.setLocationPoint(
            showDirectionViewModel.curPosition.value!!.first,
            showDirectionViewModel.curPosition.value!!.second
        )
        tMapGps.OpenGps();
    }

    private fun initNavigateButton() {
        binding.btnStartNavigate.setOnClickListener {
            if (!showDirectionViewModel.isNavigating) { // 현재 길안내중이 아니라면
                startGuiding() // 길안내시작
                binding.btnStartNavigate.text = "길안내 종료"
            } else {
                endNavigating()
                binding.btnStartNavigate.text = "길안내 시작"
            }
        }
        binding.btnStartNavigate.setOnLongClickListener {
            val msg = if(showDirectionViewModel.isNavigating) {
                "길안내 종료"
            } else{
                "길안내 시작"
            }
            ttsSpeaker.play(msg)
            true
        }
    }


    // 권한체크 --> 권한 Observing --> 위치가져오기 --> 위치 가져와지면 --> getPath --> initMapView

    private fun checkLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "checkPermissionForLocation() 권한 상태 : O")
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                Log.d(TAG, "checkPermissionForLocation() 권한 상태 : X")
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
            }
        } else {
            true
        }
    }

    private fun getPathInformation() {
        with(mapViewModel) {
            launch(coroutineContext) {
                try {
                    withContext(Dispatchers.IO) {
                        val response = RetrofitUtil.apiService.getPath(
                            startX = showDirectionViewModel.curPosition.value!!.first.toFloat(),
                            startY = showDirectionViewModel.curPosition.value!!.second.toFloat(),
                            startName = "현재위치",
                            endX = destPoi!!.frontLon,
                            endY = destPoi!!.frontLat,
                            endName = destPoi!!.name!!
                        )
                        if (response.isSuccessful) {
                            val body = response.body()
                            withContext(Dispatchers.Main) {
                                setData(body!!.features)
                                if (adapter.dataSet.isNotEmpty()) {
                                    setMapPolyLine(body.features)
                                } else {
                                    Log.d(TAG, "initNavigateButton: empty")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "$e")
                }
            }
        }
    }

    private fun setMapPolyLine(features: List<Feature>) {
        Log.d(TAG, "setMapPolyLine: ")
        val line = TMapPolyLine()
        features.forEach { feature ->
            val coordinate = getCoordinate(feature)!!
            line.addLinePoint(TMapPoint(coordinate.first, coordinate.second))
        }
        line.id = "line1"
        line.lineColor = Color.BLUE
        line.lineWidth = 2.0F
        tMapView.addTMapPolyLine(line.id, line)
    }

    private fun initRcvAdapter() {
        binding.rcvPathData.isNestedScrollingEnabled = false
        adapter = PathDataRecyclerViewAdapter(requireContext())
        with(binding) {
            rcvPathData.layoutManager = LinearLayoutManager(activity)
            rcvPathData.adapter = adapter
        }

    }

    private fun setData(featureList: List<Feature>) {
        //adapter.setData(featureList)
        adapter.setData(featureList.filter { it.geometry.type == "Point" })
    }

    @SuppressLint("MissingPermission")
    private fun startNavigating() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mLocationRequest = LocationRequest.create().apply {
            interval = 3000 // 업데이트 간격 단위(밀리초)
            fastestInterval = 3000 // 가장 빠른 업데이트 간격 단위(밀리초)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 정확성
            maxWaitTime = 5000 // 위치 갱신 요청 최대 대기 시간 (밀리초)
        }
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun startGuiding() {
        showDirectionViewModel.isNavigating = true
        ttsSpeaker.play("경로 안내를 시작합니다.")
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            val location = locationResult.lastLocation!!
            if (!isInit) {
                showDirectionViewModel.setPosition(Pair(location.longitude, location.latitude))
                initMapView()
                getPathInformation()
                isInit = true
            } else {
                showDirectionViewModel.setPosition(Pair(location.longitude, location.latitude))
            }
            tMapView.setLocationPoint(location.longitude, location.latitude)
            tMapView.setCenterPoint(location.longitude, location.latitude)
            if (showDirectionViewModel.isNavigating) {
                announceNavigate(location.latitude, location.longitude)
            }
        }
    }

    private fun getCoordinate(feature: Feature): Pair<Double, Double>? {
        return when (feature.geometry.type) {
            "LineString" -> {
                val temp = feature.geometry.coordinates as List<*>
                val t = temp[0] as List<*>
                val lon = t[0] as Double
                val lat = t[1] as Double
                Pair(lat, lon)
            }
            "Point" -> {
                val temp = feature.geometry.coordinates as List<*>
                val lon = temp[0] as Double
                val lat = temp[1] as Double
                Pair(lat, lon)
            }
            else -> {
                null
            }
        }
    }

    private fun announceNavigate(newCurLon: Double, newCurLat: Double) {
        val navigatePosition = showDirectionViewModel.getCurIndex()
        if (navigatePosition < adapter.dataSet.size) {
            val destFeature = adapter.dataSet[navigatePosition] // 현재 가려고 하는 경유지
            val destPos = getCoordinate(destFeature) // 현재 가려고 하는 경유지의 좌표
            val distance = getDistance(Pair(newCurLon, newCurLat), destPos!!)
            Log.e(TAG, "distance : $distance")
            if (distance < 5) {
                Log.e(TAG, destFeature.properties.description)
                // TODO 음성안내
                if (navigatePosition == adapter.dataSet.size - 1) { // 목적지에 도착한 경우
                    Log.e(TAG, "목적지에 도착했습니다. 경로 안내를 종료합니다.")
                    endNavigating()
                }
                else {
                    showDirectionViewModel.setCurIndex(navigatePosition + 1)
                    ttsSpeaker.play("${destFeature.properties.description}")
                }
            }
        }
    }

    private fun getDistance(start: Pair<Double, Double>, end: Pair<Double, Double>): Int {
        Log.d(TAG, "start $start end $end")
        val R = 6372.8 * 1000
        val dLat = toRadians(end.second - start.second)
        val dLon = toRadians(end.first - start.first)
        val a =
            kotlin.math.sin(dLat / 2).pow(2.0) + kotlin.math.sin(dLon / 2)
                .pow(2.0) * kotlin.math.cos(
                toRadians(start.second)
            ) * kotlin.math.cos(
                toRadians(end.second)
            )
        val c = 2 * kotlin.math.asin(kotlin.math.sqrt(a))
        return (R * c).toInt()
    }

    override fun onDestroyView() {
        _binding = null
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
        isInit = false
        showDirectionViewModel.setCurIndex(1)
        Log.d(TAG, "onDestroyView: ")
        super.onDestroyView()
    }

    private fun endNavigating() {
        if (showDirectionViewModel.isNavigating) {
            showDirectionViewModel.isNavigating = false
        }
    }

    override fun onStop() {
        super.onStop()
        // 위치 업데이터를 제거 하는 메서드
        // 지정된 위치 결과 리스너에 대한 모든 위치 업데이트를 제거
        //endNavigating()
    }
}
