package com.example.rssreader.bleModules

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rssreader.R

class ScanListAdapter(private val devices: List<BluetoothDevice>) :
    RecyclerView.Adapter<ScanListAdapter.ScanViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_list, parent, false)
        return ScanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name ?: "Unknown Device"
        holder.radioButton.isChecked = position == selectedPosition

        holder.itemView.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = devices.size

    fun getSelectedDevice(): BluetoothDevice? {
        return if (selectedPosition != -1) devices[selectedPosition] else null
    }

    class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val radioButton: RadioButton = itemView.findViewById(R.id.radio_button)
    }
}