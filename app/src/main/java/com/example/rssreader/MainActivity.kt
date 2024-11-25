package com.example.rssreader

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssreader.bleModules.ScanListAdapter

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val scanResults = mutableListOf<BluetoothDevice>()
    private var isPopupVisible = false
    private lateinit var scanListAdapter: ScanListAdapter

    // View 변수 선언
    private lateinit var btnScanStart: Button
    private lateinit var btnConnect: Button
    private lateinit var btnClose: Button
    private lateinit var popupContainer: LinearLayout
    private lateinit var recyclerScanList: RecyclerView
    private lateinit var popupView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // activity_main.xml의 View 초기화
        btnScanStart = findViewById(R.id.btn_scan_start)

        // activity_main.xml의 루트 레이아웃 가져오기
        val rootLayout = findViewById<RelativeLayout>(R.id.root_layout) // activity_main.xml의 루트 레이아웃 ID

        // popup_scan_list.xml을 inflate
        popupView = LayoutInflater.from(this)
            .inflate(
                R.layout.popup_scan_list,
                rootLayout,
                false
            )

        // popup_scan_list.xml 내부 View 초기화
        popupContainer = popupView.findViewById(R.id.popup_container)
        btnConnect = popupView.findViewById(R.id.btn_connect)
        btnClose = popupView.findViewById(R.id.btn_close)
        recyclerScanList = popupView.findViewById(R.id.recycler_scan_list)

        // RecyclerView 초기화
        setupRecyclerView()

        // 팝업을 루트 레이아웃에 추가
        rootLayout.addView(popupView)

        // Scan Start 버튼 클릭 리스너
        btnScanStart.setOnClickListener {
            if (checkPermissions()) {
                Log.i(" -- ", " SCANING --")
                startBleScan()
                btnScanStart.visibility = View.GONE
            } else {
                Log.i(" -- ", " SCANING --")
                requestPermissions()
                btnScanStart.visibility = View.GONE
            }
        }

        // Close 버튼 클릭 리스너
        btnClose.setOnClickListener {
            stopBleScan()
            popupView.visibility = View.GONE
            popupContainer.visibility = View.GONE
            btnScanStart.visibility = View.VISIBLE
        }

        // Connect 버튼 클릭 리스너
        btnConnect.setOnClickListener {
            val selectedDevice = scanListAdapter.getSelectedDevice()
            if (selectedDevice !=null) {
                Toast.makeText(this, "Selected: ${selectedDevice.name}", Toast.LENGTH_SHORT).show()
                // BLE GATT 연결 로직은 이후 구현
            } else {
                Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        super.onPause()
//        stopBleScan() // 스캔 중지
//        isPopupVisible = popupView.visibility == View.VISIBLE // 팝업 상태 저장
//        popupView.visibility = View.GONE // 팝업 숨김
//        popupContainer.visibility = View.GONE // 팝업 컨테이너 숨김
//        btnScanStart.visibility = View.VISIBLE
//    }

    override fun onPause() {  //TODO : 앱 켜지면 자동으로 스캔해서 연결까지 동작
        super.onPause()
        stopBleScan() // 스캔 중지
        isPopupVisible = popupView.visibility == View.VISIBLE // 팝업 상태 저장
        popupView.visibility = View.GONE // 팝업 숨김
        popupContainer.visibility = View.GONE // 팝업 컨테이너 숨김
        btnScanStart.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (isPopupVisible) { // 팝업 상태 복구
            popupView.visibility = View.VISIBLE
            popupContainer.visibility = View.VISIBLE
            btnScanStart.visibility = View.GONE
        } else if (scanResults.isEmpty()) { // 스캔 결과가 없으면 스캔 재개
            startBleScan()
        }
    }

    private fun setupRecyclerView() {
        scanListAdapter = ScanListAdapter(scanResults)
        recyclerScanList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = scanListAdapter
        }
    }

    private fun startBleScan() {
        Log.i(" - MainActivity", "popupContainer : ${popupContainer} ")
        Log.i(" - MainActivity", "bluetoothAdapter : ${bluetoothAdapter}")
        Log.i(" - MainActivity", "bluetoothAdapter.bluetoothLeScanner : ${bluetoothAdapter?.bluetoothLeScanner}")
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        popupView.visibility = View.VISIBLE
        popupContainer.visibility = View.VISIBLE

//        // 10초 후 스캔 중지
//        popupContainer.postDelayed({
//            stopBleScan()
//            Toast.makeText(this, "Scan stopped after 10 seconds", Toast.LENGTH_SHORT).show()
//        }, 10000)
    }

//    private fun stopBleScan() {
//        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
//    }

    private fun stopBleScan() {
        bluetoothAdapter?.bluetoothLeScanner?.apply {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
                Log.e(" - MainActivity", "블루투스 스캔 정지 ")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to stop BLE scan: ${e.message}")
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (result.scanRecord?.deviceName == null){
                // DeviceName 이 Null 인 경우, 스캔리스트에 추가 X
                return
            }else{
                // DeviceName 이 Null 이 아닌 경우만 스캔리스트에 추가
                if (!scanResults.contains(device)) {  // 중복된 MAC 주소의 경우, 추가 Pass
                    scanResults.add(device)  // 해당 device 정보 추가 // scanListAdapter = ScanListAdapter(scanResults)
                    //새로 추가된 항목만 업데이트
                    scanListAdapter.notifyItemInserted(scanResults.size - 1)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            when (errorCode) {
                ScanCallback.SCAN_FAILED_ALREADY_STARTED -> Log.e("MainActivity", "Scan already started")
                ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> Log.e("MainActivity", "App registration failed")
                ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> Log.e("MainActivity", "Internal error")
                ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> Log.e("MainActivity", "Feature unsupported")
            }
            Toast.makeText(this@MainActivity, "Scan failed: $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )// TODO: 권한 추가 관련 동작 필요함
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )// TODO: 권한 추가 관련 동작 필요함
    }
}
