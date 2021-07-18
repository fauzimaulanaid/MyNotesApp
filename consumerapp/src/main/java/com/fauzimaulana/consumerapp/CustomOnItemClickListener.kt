package com.fauzimaulana.consumerapp

import android.view.View

class CustomOnItemClickListener(private val position: Int, private val onItemClickCallback: OnItemClickCallback): View.OnClickListener {
    interface OnItemClickCallback {
        fun onItemClick(view: View, position: Int)
    }

    override fun onClick(view: View) {
        onItemClickCallback.onItemClick(view, position)
    }
}