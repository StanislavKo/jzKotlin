package com.hsd.jz.android.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.Window
import android.widget.LinearLayout
import com.hsd.jz.android.R


class DialogVoiceFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this!!.activity!!)
        builder.setMessage("Скажите, что вы ищете\r\nИ подождите 10 секунд")
                .setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        return builder.create()
    }

    fun onCreateDialog2(savedInstanceState: Bundle?): Dialog {
        val view = getActivity()!!.getLayoutInflater().inflate(R.layout.fragment_dialog_voice, LinearLayout(getActivity()), false)

        // Build dialog
        val builder = Dialog(getActivity())
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.getWindow().setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        builder.setContentView(view)
        return builder
    }
}