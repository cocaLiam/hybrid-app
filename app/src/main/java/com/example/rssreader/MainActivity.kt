package com.example.rssreader

import android.Manifest
import android.bluetooth.BluetoothAdapter
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
import androidx.recyclerview.widget.RecyclerView
import com.example.rssreader.bleModules.ScanListAdapter

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var scanListAdapter: ScanListAdapter = ScanListAdapter()
    private var isPopupVisible = false
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private val MAIN_LOG_TAG = " - MainActivity "

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

        // popup_scan_list.xml 내부 View 초기
        btnConnect = popupView.findViewById(R.id.btn_connect)
        btnClose = popupView.findViewById(R.id.btn_close)
        popupContainer = popupView.findViewById(R.id.popup_container)
        recyclerScanList = popupView.findViewById(R.id.recycler_scan_list)

        // RecyclerView 초기화
        scanListAdapter.setupRecyclerView(recyclerScanList, this@MainActivity)

        // 팝업을 루트 레이아웃에 추가
        rootLayout.addView(popupView)

        // Scan Start 버튼 클릭 리스너
        btnScanStart.setOnClickListener {
            if (checkPermissions()) {
                Log.i(MAIN_LOG_TAG, " SCANING --")
                startBleScan()
            } else {
                Log.i(MAIN_LOG_TAG, " Pemission Requseting --")
                requestPermissions()
            }
        }

        // Close 버튼 클릭 리스너
        btnClose.setOnClickListener {
            stopBleScan()
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

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MAIN_LOG_TAG, "onDestroy")
        stopBleScan() // 스캔 중지
        isPopupVisible = popupView.visibility == View.VISIBLE // 팝업 상태 저장
    }

    override fun onPause() {
        super.onPause()
        Log.i(MAIN_LOG_TAG, "onPause")
        stopBleScan() // 스캔 중지
        isPopupVisible = popupView.visibility == View.VISIBLE // 팝업 상태 저장
    }

    override fun onResume() { //TODO : 앱 켜지면 자동으로 스캔해서 연결까지 동작
        super.onResume()
        Log.i(MAIN_LOG_TAG, "onResume")
//        if (isPopupVisible) { // 팝업 상태 복구
//            popupView.visibility = View.VISIBLE
//            popupContainer.visibility = View.VISIBLE
//            btnScanStart.visibility = View.GONE
//        } else if (scanResults.isEmpty()) { // 스캔 결과가 없으면 스캔 재개
//            startBleScan()
//        }
    }

    private fun startBleScan() {
        Log.i(MAIN_LOG_TAG, "popupContainer : ${popupContainer} ")
        Log.i(MAIN_LOG_TAG, "bluetoothAdapter : ${bluetoothAdapter}")
        Log.i(MAIN_LOG_TAG, "bluetoothAdapter.bluetoothLeScanner : ${bluetoothAdapter?.bluetoothLeScanner}")
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        btnScanStart.visibility = View.GONE
        popupView.visibility = View.VISIBLE
        popupContainer.visibility = View.VISIBLE

        // 10초 후 스캔 중지
        Log.i(MAIN_LOG_TAG, "스캔 타임아웃 제한시간 : ${SCAN_PERIOD/1000}초 ")

        popupContainer.postDelayed({
            stopBleScan()
            Toast.makeText(this, "Scan stopped after 10 seconds", Toast.LENGTH_SHORT).show()
        }, SCAN_PERIOD)
    }

    private fun stopBleScan() {
        popupView.visibility = View.GONE // 팝업 숨김
        popupContainer.visibility = View.GONE // 팝업 컨테이너 숨김
        btnScanStart.visibility = View.VISIBLE // Scan Start 버튼 활성화
        bluetoothAdapter?.bluetoothLeScanner?.apply {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
                Log.e(MAIN_LOG_TAG, "블루투스 스캔 정지 ")
            } catch (e: Exception) {
                Log.e(MAIN_LOG_TAG, "Failed to stop BLE scan: ${e.message}")
            }
        }
        // apply 를 안쓰는 경우
//        val scanner = bluetoothAdapter?.bluetoothLeScanner
//        if (scanner != null ) {
//            try {
//                scanner.stopScan(scanCallback)
//                Log.e(" - MainActivity", "블루투스 스캔 정지 ")
//            } catch (e: Exception) {
//                Log.e("MainActivity", "Failed to stop BLE scan: ${e.message}")
//            }
//        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (result.scanRecord?.deviceName == null){
                // DeviceName 이 Null 인 경우, 스캔리스트에 추가 X
                return
            }else{
                scanListAdapter.addDeviceToAdapt(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(MAIN_LOG_TAG, "onScanFailed called with errorCode: $errorCode")
            when (errorCode) {
                ScanCallback.SCAN_FAILED_ALREADY_STARTED -> Log.e(MAIN_LOG_TAG, "Scan already started")
                ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> Log.e(MAIN_LOG_TAG, "App registration failed")
                ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> Log.e(MAIN_LOG_TAG, "Internal error")
                ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> Log.e(MAIN_LOG_TAG, "Feature unsupported")
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
