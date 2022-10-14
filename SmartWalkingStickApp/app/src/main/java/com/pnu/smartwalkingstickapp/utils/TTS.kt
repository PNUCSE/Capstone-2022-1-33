package com.pnu.smartwalkingstickapp.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.*

class TTS (context: Context) {
    private lateinit var textToSpeech: TextToSpeech
    val ttsState = MutableLiveData<Int>(0)
    val params = Bundle()

    init {
        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                // TODO: 언어 선택
                val result = textToSpeech.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS","해당언어는 지원되지 않습니다.")
                    return@OnInitListener
                }
            }
        })
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(p0: String?) {
                ttsState.postValue(1)
            }

            override fun onDone(p0: String?) {
                ttsState.postValue(0)
            }

            override fun onError(p0: String?) {
                ttsState.postValue(-1)
            }
        })
    }

    fun play(msg: String) {
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1");
        if (!textToSpeech.isSpeaking) {
            Log.d("jiwoo", "play: speaking123")
            ttsState.value = 1
            textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, params, "1")
            textToSpeech?.playSilentUtterance(750, TextToSpeech.QUEUE_ADD,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)
        }
        else{
            Log.d("jiwoo", "play: speaking")
        }
    }
}