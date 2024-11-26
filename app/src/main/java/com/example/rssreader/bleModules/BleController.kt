// 각종 OS 및 개발 핸들러
package com.example.rssreader.bleModules
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
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

// Custom Package
import com.example.rssreader.bleModules.LeDeviceListAdapter

class BleController(private val applicationContext: Context) {
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
    private val mutableMap = mutableMapOf("블루투스 권한" to false,
        "위치 권한" to false)

    fun setBleModules(){
        // getSystemService는 Context가 완전히 초기화된 후에 호출되어야 함
        bluetoothManager   = applicationContext.getSystemService(BluetoothManager::class.java)
//        bluetoothAdapter   = bluetoothManager.getAdapter()  // 이전 버전
        bluetoothAdapter   = bluetoothManager.adapter
//        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner()   // 이전 버전
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    fun setBlePermissionLauncher(launcher: ActivityResultLauncher<Intent>) {
        // BLE 권한 요청 런처
        enableBluetoothLauncher = launcher
    }

    fun checkBleOperator(){
//        블루투스 클래식 기능이 핸드폰에 있는지 확인
//        val bluetoothAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

        // 1. 기기의 BLE 지원 여부 확인
        val bluetoothLEAvailable = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        if (!bluetoothLEAvailable){
            Log.e(" - BleController", "기기의 BLE 지원 여부 확인 : $bluetoothLEAvailable")
            //TODO: 현재기기 사용불가 에러 핸들링 표시 필요
        }

        // 2. 기기의 BLE 및 Bluetooth Classic 기능 지원 여부 확인
//        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
//        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.e(" - BleController", "기기의 BLE 및 Bluetooth Classic 기능 지원 여부 확인 : $bluetoothAdapter")
            //TODO: 현재기기 사용불가 에러 핸들링 표시 필요
        }
    }

    fun requestBlePermission(activity: Activity): MutableMap<String, Boolean>{
        //        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val mutableList = mutableListOf(false,false)
        // 블루투스가 활성화가 안된 경우, 활성화 요청
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }else mutableMap["블루투스 권한"] = true
        // 위치정보 권한 검사

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {  // 블루투스 스캔 및 연결 작업을 위해 위치정보 권한 획득 필요함
            val locationOk = ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            Log.i(" - BleController", "locationOk : ${locationOk}")
            // TODO : LOCATION 권한 허용 안한 상황에 대한 핸들링 코드 필요
            mutableMap["위치 권한"] = true
        } else mutableMap["위치 권한"] = true
        return mutableMap
    }

    fun <T>startBleScan(
        scanCallback: ScanCallback,
        popupContainer: T,
    ) {
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)

        // 10초 후 스캔 중지
        Log.i(" - BleController", "스캔 타임아웃 제한시간 : ${SCAN_PERIOD / 1000}초 ")
        when (popupContainer) {
            is LinearLayout -> {
                popupContainer.postDelayed({
                    stopBleScan(scanCallback)
                    Toast.makeText(applicationContext, "Scan stopped after 10 seconds", Toast.LENGTH_SHORT).show()
                }, SCAN_PERIOD)
            }
            is Handler -> {
                popupContainer.postDelayed({ // SCAN_PERIOD 시간후에 발동되는 지연 함수
                    Log.w(" - MainActivity", "--스캔 타임아웃-- ")
                    bluetoothLeScanner?.stopScan(scanCallback)
                }, SCAN_PERIOD)
            }
        }
    }

    fun stopBleScan(scanCallback: ScanCallback) {
        bluetoothAdapter?.bluetoothLeScanner?.apply {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
                Log.e(" - MainActivity", "블루투스 스캔 정지 ")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to stop BLE scan: ${e.message}")
            }
        }
    }

    fun handleBluetoothIntentResult(result: ActivityResult) {
        // 특정 작업(예: 권한 요청, 다른 Activity 호출 등)의 결과를 처리할 Callback 함수
        if (result.resultCode == Activity.RESULT_OK) {
            // Bluetooth가 활성화되었습니다.
            Log.i(" - BleController", "블루투스가 활성화되었습니다.")
            mutableMap["블루투스 권한"] = true
        } else {
            // Bluetooth 활성화가 취소되었습니다.
            Log.i(" - BleController", "블루투스 활성화가 취소되었습니다.")
            mutableMap["블루투스 권한"] = false
        }
    }
}