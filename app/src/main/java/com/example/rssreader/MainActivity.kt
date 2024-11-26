// 각종 OS 및 개발 핸들러
package com.example.rssreader
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.Manifest
import android.app.Activity
import android.os.Handler
import android.os.Bundle

// UI 관련
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// 기능 관련
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

//  블루투스 권한 요청에 필요 한 import
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.widget.Toast

// Custom Package
import com.example.rssreader.bleModules.LeDeviceListAdapter
import com.example.rssreader.bleModules.BleController



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

        // 1. BluetoothManager 및 BluetoothAdapter 초기화
        val bleController = BleController(this) // MainActivity는 Context를 상속받음
        bleController.setBleModules()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        Log.i(" - MainActivity", "recyclerView.adapter : ${recyclerView.adapter}")
        Log.i(" - MainActivity", "leDeviceListAdapter : ${leDeviceListAdapter}")
        recyclerView.adapter = leDeviceListAdapter

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

        // 4. Bluetooth 가 비활성화 된 경우 활성화 요청
        val permissionOk = bleController.requestBlePermission(this)
        for ((key, value) in permissionOk) {
            if (!value){
                Log.e(" - MainActivity", "권한 없음 : ${key}")
                Toast.makeText(this, "${key}이 활성화되지 않았습니다.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 5
        bleController.startBleScan(leScanCallback, handler)
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

}