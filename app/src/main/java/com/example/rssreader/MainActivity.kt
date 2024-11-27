// 각종 OS 및 개발 핸들러
package com.example.rssreader
import android.content.Intent
import android.util.Log
import android.os.Handler
import android.os.Bundle

// UI 관련
import androidx.recyclerview.widget.RecyclerView

// 기능 관련
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

//  블루투스 권한 요청에 필요 한 import
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.widget.Toast

// Custom Package
import com.example.rssreader.bleModules.LeDeviceListAdapter
import com.example.rssreader.bleModules.BleController



class MainActivity : ComponentActivity() {
    // 1. ActivityResultLauncher를 클래스의 멤버 변수로 선언합니다.
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private val bleController = BleController(this) // MainActivity는 Context를 상속받음
    private val handler = Handler()
    private val leDeviceListAdapter = LeDeviceListAdapter()

    private val MAIN_LOG_TAG = " - MainActivity "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. BluetoothManager 및 BluetoothAdapter 초기화
        bleController.setBleModules()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        Log.i(MAIN_LOG_TAG, "recyclerView.adapter : ${recyclerView.adapter}")
        Log.i(MAIN_LOG_TAG, "leDeviceListAdapter : ${leDeviceListAdapter}")
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
                Log.e(MAIN_LOG_TAG, "권한 없음 : ${key}")
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

}