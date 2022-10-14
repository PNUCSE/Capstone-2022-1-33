package com.pnu.smartwalkingstickapp.ui.map_task

import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pnu.smartwalkingstickapp.MainActivity
import com.pnu.smartwalkingstickapp.R
import com.pnu.smartwalkingstickapp.databinding.FragmentMapBinding
import com.pnu.smartwalkingstickapp.ui.map_task.response.search.Poi
import com.pnu.smartwalkingstickapp.ui.map_task.utility.RetrofitUtil
import com.pnu.smartwalkingstickapp.utils.TTS
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MapFragment : Fragment(), CoroutineScope {
    private val mapViewModel: MapViewModel by activityViewModels()
    private lateinit var ttsSpeaker : TTS
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var apiKey: String
    private lateinit var adapter: PoiDataRecyclerViewAdapter
    private var binding: FragmentMapBinding? = null
    private lateinit var tts: TextToSpeech
    private var text: String = ""

    private var state: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        apply {
            apiKey = getString(R.string.TmapAPIKey)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)
        job = Job()

        return binding!!.root
    }

    val TAG = "ABCDE"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Map onViewCreated: ")
        val act = activity as MainActivity
        act.showComponent()
        ttsSpeaker = TTS(requireContext())
        initFindingDirectionButton()
        initRcvAdapter()
        initButton()
    }

    private fun initFindingDirectionButton() {
        binding!!.btnFindPath.setOnClickListener {
            with(mapViewModel) {
                if(destPoi != null) {
                    val text = "현재위치에서 " + destPoi!!.name +"까지 길찾기를 시작합니다."
                    ttsSpeaker.play(text)
                    ttsSpeaker.ttsState.observe(viewLifecycleOwner) {
                        Log.d(TAG, "initFindingDirectionButton: $it ")
                        if(it == 0) {
                            findNavController().navigate(R.id.action_nav_map_fragment_to_showDirectionFragment)
                        }
                    }
                }
            }
        }
    }

    private fun initButton() {
        with(binding!!) {
            btnDestPOI.setOnClickListener {
                ttsSpeaker.play("${etvDestination.text} 으로 검색을 시작합니다. ")
                state = "dest"
                getPOIData(etvDestination.text.toString())
            }
        }
    }

    private fun initRcvAdapter() {
        adapter = PoiDataRecyclerViewAdapter().apply {
            setOnStateInterface(object : OnPoiDataItemClick {
                override fun sendData(data: Poi) {
                    when (state) {
                        "dest" -> {
                            mapViewModel.destPoi = data
                            binding!!.etvDestination.setText(data.name)
                            binding!!.etvDestination.setSelection(data.name!!.length)
                        }

                    }
                }
            })
        }
        with(binding!!) {
            rcvPoiData.layoutManager = LinearLayoutManager(activity)
            rcvPoiData.adapter = adapter
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun speakOut() {
        tts.setPitch(0.6F)
        tts.setSpeechRate(0.1F)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    private fun setPoiData(poiList: List<Poi>) {
        adapter.setData(poiList.toMutableList())
    }

    private fun getPOIData(keyword: String) {
        launch(coroutineContext) {
            try {
                withContext(Dispatchers.IO) {
                    val response = RetrofitUtil.apiService.getSearchLocation(
                        keyword = keyword
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        withContext(Dispatchers.Main) {
                            setPoiData(body!!.searchPoiInfo.pois.poi)
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun actionToCameraXFragment(bundle: Bundle) {
        findNavController().navigate(R.id.action_nav_map_fragment_to_nav_camera_x_fragment, bundle)
    }
}