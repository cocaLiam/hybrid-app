package com.example.simplebleapp

// Operator Pack
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// UI Pack
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

// BLE Pack
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Handler

// Util Pack
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

// Custom Package
import com.example.simplebleapp.bleModules.ScanListAdapter
import com.example.simplebleapp.bleModules.BleController

class MainActivity : AppCompatActivity() {
    // 1. ActivityResultLauncher를 클래스의 멤버 변수로 선언합니다.
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private val bleController = BleController(this) // MainActivity는 Context를 상속받음
    private val handler = Handler()
//    private val leDeviceListAdapter = LeDeviceListAdapter()

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

// BLE 초기화 완료 -----------------------------------------------------------------------------------
        // 1. BluetoothManager 및 BluetoothAdapter 초기화
        bleController.setBleModules()

        // 2_1. 권한요청 Launcher 등록
//        registerForActivityResult 설명 >>
//        Activity나 Fragment의 생명주기에 맞춰 실행되는 결과 처리 메커니즘을 제공하는 함수
//        특정 작업(예: 권한 요청, 다른 Activity 호출 등)의 결과를 비동기적으로 처리하기 위해 사용됨
        enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            { result: ActivityResult ->
                // 결과를 bleController로 전달
                bleController.handleBluetoothIntentResult(result)
            }
        )

        // 2_2. Launcher 객체 BleController 에 전달
        bleController.setBlePermissionLauncher(enableBluetoothLauncher)

        // 3. BLE 기능 검사
        bleController.checkBleOperator()

        // 권한 요청 메서드 수정
        fun permissionRequest(onPermissionGranted: () -> Unit) {
            val permissionOk = bleController.requestBlePermission(this)
            var allPermissionsGranted = true

            for ((key, value) in permissionOk) {
                if (!value) {
                    Log.e(MAIN_LOG_TAG, "권한 없음 : $key")
                    Toast.makeText(this, "${key}이 활성화되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    allPermissionsGranted = false
                }
            }

            if (allPermissionsGranted) {
                // 모든 권한이 허용된 경우 콜백 실행
                onPermissionGranted()
            } else {
                Log.i(MAIN_LOG_TAG, "권한 요청 중...")
            }
        }


//        fun permissionRequest(): Boolean {
//            // 4. Bluetooth 가 비활성화 된 경우 활성화 요청
//            val permissionOk = bleController.requestBlePermission(this)
//            for ((key, value) in permissionOk) {
//                if (!value){
//                    Log.e(MAIN_LOG_TAG, "권한 없음 : ${key}")
//                    Toast.makeText(this, "${key}이 활성화되지 않았습니다.", Toast.LENGTH_SHORT).show()
//                    return false
//                }
//            }
//            return true
//        }

// BLE 초기화 완료 -----------------------------------------------------------------------------------

// UI 초기화 완료 ------------------------------------------------------------------------------------
        // activity_main.xml의 View 초기화
        btnScanStart = findViewById(R.id.btn_scan_start)

        // activity_main.xml의 루트 레이아웃 가져오기
        val rootLayout =
            findViewById<RelativeLayout>(R.id.root_layout) // activity_main.xml의 루트 레이아웃 ID

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
        // Scan Start 버튼 클릭 리스너 수정
        btnScanStart.setOnClickListener {
            permissionRequest {
                // 권한이 허용된 경우에만 BLE 스캔 시작
                startBleScan()
            }
        }

//        btnScanStart.setOnClickListener {
//            if (permissionRequest()) {
//                Log.i(MAIN_LOG_TAG, " SCANING --")
//                startBleScan()
//            } else {
//                Log.i(MAIN_LOG_TAG, " Pemission Requseting --")
//                permissionRequest()
//            }
//        }

        // Close 버튼 클릭 리스너
        btnClose.setOnClickListener {
            stopBleScan()
        }

        // Connect 버튼 클릭 리스너
        btnConnect.setOnClickListener {
            val selectedDevice = scanListAdapter.getSelectedDevice()
            if (selectedDevice != null) {
                Toast.makeText(this, "Selected: ${selectedDevice?.name}", Toast.LENGTH_SHORT).show()
                // TODO : BLE GATT 연결 로직은 이후 구현
            } else {
                Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
            }
        }

// UI 초기화 완료 ------------------------------------------------------------------------------------
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.i(MAIN_LOG_TAG, "onDestroy")
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show()
        stopBleScan()
        isPopupVisible = popupView.visibility == View.VISIBLE // 팝업 상태 저장
    }

    override fun onPause() {
        super.onPause()
        Log.i(MAIN_LOG_TAG, "onPause")
//        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show()
        stopBleScan()
        isPopupVisible = popupView.visibility == View.VISIBLE // 팝업 상태 저장
    }

    override fun onResume() { //TODO : 앱 켜지면 자동으로 스캔해서 연결까지 동작
        super.onResume()
        Log.i(MAIN_LOG_TAG, "onResume")
//        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show()
//        if (isPopupVisible) { // 팝업 상태 복구
//            popupView.visibility = View.VISIBLE
//            popupContainer.visibility = View.VISIBLE
//            btnScanStart.visibility = View.GONE
//        } else if (scanResults.isEmpty()) { // 스캔 결과가 없으면 스캔 재개
//            startBleScan()
//        }
    }

    private fun startBleScan() {
        try {
            bleController.startBleScan(scanCallback, popupContainer)
            btnScanStart.visibility = View.GONE
            popupView.visibility = View.VISIBLE
            popupContainer.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(MAIN_LOG_TAG, "Failed to stop BLE scan: ${e.message}")
        }
    }

    private fun stopBleScan() {
        try {
            bleController.stopBleScan(scanCallback)
            Log.i(MAIN_LOG_TAG, "블루투스 스캔 정지 ")
            btnScanStart.visibility = View.VISIBLE // Scan Start 버튼 활성화
            popupView.visibility = View.GONE // 팝업 숨김
            popupContainer.visibility = View.GONE // 팝업 컨테이너 숨김
        } catch (e: Exception) {
            Log.e(MAIN_LOG_TAG, "Failed to stop BLE scan: ${e.message}")
        }
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

}
