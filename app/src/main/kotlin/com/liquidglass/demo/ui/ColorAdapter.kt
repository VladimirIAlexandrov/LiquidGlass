package com.liquidglass.demo.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.liquidglass.demo.R

class ColorAdapter(private val items: List<ColorItem>) :
    RecyclerView.Adapter<ColorAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image:    ImageView = view.findViewById(R.id.item_image)
        val title:    TextView  = view.findViewById(R.id.item_title)
        val subtitle: TextView  = view.findViewById(R.id.item_subtitle)
        val checkBox: CheckBox  = view.findViewById(R.id.item_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
    )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.image.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL; setColor(item.color)
        }
        h.image.setColorFilter(lighten(item.color))
        h.title.text = item.title
        h.subtitle.text = item.subtitle
        h.checkBox.setOnCheckedChangeListener(null)
        h.checkBox.isChecked = item.checked
        h.itemView.setBackgroundColor(Color.argb(30,
            Color.red(item.color), Color.green(item.color), Color.blue(item.color)))
    }

    private fun lighten(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = minOf(1f, hsv[2] + 0.4f)
        hsv[1] = maxOf(0f, hsv[1] - 0.3f)
        return Color.HSVToColor(hsv)
    }
}
