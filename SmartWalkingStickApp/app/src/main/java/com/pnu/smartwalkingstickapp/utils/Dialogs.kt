package com.pnu.smartwalkingstickapp.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.speech.tts.TextToSpeech
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import com.pnu.smartwalkingstickapp.databinding.FragmentBasicDialogBinding
import java.util.*
import kotlin.system.exitProcess

class WrappedDialogBasicTwoButton (context: Context) : Dialog(context){
    var clickListener : DialogButtonClickListener ? = null
    var binding : FragmentBasicDialogBinding = FragmentBasicDialogBinding.inflate(layoutInflater)

    interface DialogButtonClickListener {
        fun dialogCloseClickListener()
        fun dialogCustomClickListener()
    }

    init {
        //setCanceledOnTouchOutside(false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        window?.run {
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 50))
            attributes.width = ViewGroup.LayoutParams.MATCH_PARENT
            attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT
        } ?: exitProcess(0)

        binding.run {
//            dialogCloseBtn.text = closeBtnText
//            dialogCustomBtn.text = customBtnText
            dialogContent.addTextChangedListener(PhoneNumberFormattingTextWatcher())
            dialogCloseBtn.setOnClickListener { clickListener?.dialogCloseClickListener()}
            dialogCustomBtn.setOnClickListener { clickListener?.dialogCustomClickListener() }
        }
    }
}
