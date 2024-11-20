package com.example.rssreader

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge


//  블루투스 권한 요청에 필요 한 import
import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.recyclerview.widget.RecyclerView

// Custom Package
import com.example.rssreader.bleModules.LeDeviceListAdapter



class MainActivity : ComponentActivity() {
    // 1. ActivityResultLauncher를 클래스의 멤버 변수로 선언합니다.
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothManager   : BluetoothManager
    private lateinit var bluetoothAdapter   : BluetoothAdapter
    private lateinit var bluetoothLeScanner : BluetoothLeScanner
    private var scanning = false
    private val handler = Handler()
    private val leDeviceListAdapter = LeDeviceListAdapter()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 0. BluetoothManager 및 BluetoothAdapter 초기화
        // getSystemService는 Context가 완전히 초기화된 후에 호출되어야 함
        bluetoothManager   = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter   = bluetoothManager.getAdapter()  // 이전 버전
//        bluetoothAdapter   = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner()
//        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        Log.i(" - MainActivity", "recyclerView.adapter : ${recyclerView.adapter}")
        Log.i(" - MainActivity", "leDeviceListAdapter : ${leDeviceListAdapter}")
        recyclerView.adapter = leDeviceListAdapter

//        // 블루투스 클래식 기능이 핸드폰에 있는지 확인
//        val bluetoothAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

        // 1. 기기의 BLE 지원 여부 확인
        val bluetoothLEAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        if (!bluetoothLEAvailable){
            Log.e(" - MainActivity", "기기의 BLE 지원 여부 확인 : $bluetoothLEAvailable")
            //TODO: 현재기기 사용불가 에러 핸들링 표시 필요
        }

        // 2. 기기의 BLE 및 Bluetooth Classic 기능 지원 여부 확인
//        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
//        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.e(" - MainActivity", "기기의 BLE 및 Bluetooth Classic 기능 지원 여부 확인 : $bluetoothAdapter")
            //TODO: 현재기기 사용불가 에러 핸들링 표시 필요
        }

        // 3_1. registerForActivityResult 함수로 결과를 처리할 콜백을 등록
        enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::handleBluetoothIntentResult // 콜백 함수 참조
        )

        // 3_2. Bluetooth 가 비활성화된 경우 활성화를 요청
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }else{
            // 4_4 Bluetooth가 이미 활성화된 경우 스캔 시작
            checkPermissions()
        }
        Log.e(" - MainActivity", "앱 END Point")
    }

    // 3_1 블루투스 활성화 콜백함수
    private fun handleBluetoothIntentResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            // Bluetooth가 활성화되었습니다.
            Log.i(" - MainActivity", "블루투스가 활성화되었습니다.")
        } else {
            // Bluetooth 활성화가 취소되었습니다.
            Log.i(" - MainActivity", "블루투스 활성화가 취소되었습니다.")
        }
    }

    // 4_1 블루투스 스캔 콜백 함수 등록
    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            leDeviceListAdapter.addDevice(result.device)
            leDeviceListAdapter.notifyDataSetChanged() // 데이터 변경 알림
        }
    }

    // 4_2 블루투스 스캔 함수 정의
    private fun scanLeDevice() {
        Log.i(" - MainActivity", "scanning 상태: $scanning")
        if (bluetoothLeScanner == null){
            Log.w(" - MainActivity", "블루투스 생성 이전에 호출 에러처리")
            return
        }
        Log.i(" - MainActivity", "bluetoothLeScanner:  ${bluetoothLeScanner}")
        if (!scanning) { // 타임아웃 스캔 시작
            Log.i(" - MainActivity", "스캔 타임아웃 제한시간 : ${SCAN_PERIOD/1000}초 ")
            handler.postDelayed({ // SCAN_PERIOD 시간후에 발동되는 지연 함수
                scanning = false
                Log.w(" - MainActivity", "--스캔 타임아웃-- ")
                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            Log.i(" - MainActivity", "스캔 시작 ")
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {  // 현재 스캔 종료 처리
            Log.i(" - MainActivity", "스캔 취소 1 ")
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
            Log.i(" - MainActivity", "스캔 취소 2 ")
        }
    }

    // 4_3 블루투스 스캔전 LOCATION 권한 검사
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            // 권한이 이미 허용된 경우 BLE 스캔 시작
            scanLeDevice()
        }
    }

}