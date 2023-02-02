package com.example.quzbus.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.quzbus.R

class CustomButton(context: Context, attributeSet: AttributeSet): LinearLayout(context, attributeSet) {

    private var arrowImage: View
    private var busNumber: TextView
    private var busOnline: TextView
    private var card: CardView

    init {
        //Inflate layout
        View.inflate(context, R.layout.item_custom_button, this)
        arrowImage = findViewById(R.id.view_direction)
        busNumber = findViewById(R.id.tv_bus_number)
        busOnline = findViewById(R.id.tv_bus_online)
        card = findViewById(R.id.card)

        //set on click listener
        arrowImage.setOnClickListener {
            if (arrowImage.foreground == resources.getDrawable(R.drawable.arrow_up)) {
                arrowImage.foreground = resources.getDrawable(R.drawable.arrow_up)
            } else {
                arrowImage.foreground = resources.getDrawable(R.drawable.arrow_down)
            }
        }
    }

    fun setText(text: String) {
        busNumber.text = text
    }
}
