package com.example.rssreader.bleModules

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rssreader.R

class LeDeviceListAdapter : RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder>() {

    private val leDevices: MutableList<BluetoothDevice> = mutableListOf()

    // ViewHolder 클래스 정의
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(android.R.id.text1)
        val deviceAddress: TextView = view.findViewById(android.R.id.text2)
    }

    // 새로운 ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    // ViewHolder에 데이터를 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = leDevices[position]
        holder.deviceName.text = device.name ?: "Unknown Device"
        holder.deviceAddress.text = device.address
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int {
        return leDevices.size
    }

    // 새로운 장치를 목록에 추가
    fun addDevice(device: BluetoothDevice) {
        if (!leDevices.contains(device)) {
            leDevices.add(device)
        }
    }

    // 목록 초기화
    fun clear() {
        leDevices.clear()
    }

}