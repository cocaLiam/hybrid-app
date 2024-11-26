package com.example.rssreader.bleModules

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssreader.R

class ScanListAdapter() :
    RecyclerView.Adapter<ScanListAdapter.ScanViewHolder>() {
    private val devices = mutableListOf<BluetoothDevice>()
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

    // 여기서부턴 커스텀 코드들
    fun setupRecyclerView(
        recyclerScanList: RecyclerView,
        mainActivityContext: Context
    ) {
        // recyclerScanList 가 표출되는 Layout 방식을 리스트 형태로 세로 또는 가로 방향으로 배치하기 위함
        recyclerScanList.layoutManager = LinearLayoutManager(mainActivityContext)
        // recyclerScanList 자체에서 데이터를 직접 관리 X
        // 그래서 데이터를 관리하고 화면에 표시하는 역할의 어댑터를 직접 넣어줘야함
        recyclerScanList.adapter = this
    }

    fun addDeviceToAdapt(deviceInfo: BluetoothDevice){
        if(!devices.contains(deviceInfo)){ // 중복된 MAC 주소의 경우, 추가 Pass
            devices.add(deviceInfo) // 해당 device 정보 추가
            //새로 추가된 항목만 업데이트
            this.notifyItemInserted(devices.size - 1 )
        }
    }
}