// 각종 OS 및 개발 핸들러
package com.example.simplebleapp.bleModules
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.Manifest
import android.app.Activity
import android.os.Handler
import android.os.Build

// UI 관련
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.LinearLayout
import android.widget.Toast
import android.content.Context

// 기능 관련
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher

//  블루투스 권한 요청에 필요 한 import
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import androidx.annotation.RequiresPermission

class BleController(private val applicationContext: Context) {
    // BLE 관련 멤버 변수
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null

    // ActivityResultLauncher를 클래스의 멤버 변수로 선언
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>

    // 스캔 제한 시간
    private val SCAN_PERIOD: Long = 10000
    private val BLECONT_LOG_TAG = " - BleController"

    // 권한 상태를 저장하는 Map
    private val permissionStatus = mutableMapOf(
        "블루투스 활성화" to false,
        "블루투스 스캔 권한" to false,
        "위치 권한" to false,
        "블루투스 연결 권한" to false
    )

    /**
     * BLE 모듈 초기화
     */
    fun setBleModules() {
        // getSystemService는 Context가 완전히 초기화된 후에 호출되어야 함
        bluetoothManager   = applicationContext.getSystemService(BluetoothManager::class.java)
//        bluetoothAdapter   = bluetoothManager.getAdapter()  // 이전 버전
        bluetoothAdapter   = bluetoothManager.adapter
//        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner()   // 이전 버전
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }

    /**
     * BLE 권한 요청 런처 설정
     */
    fun setBlePermissionLauncher(launcher: ActivityResultLauncher<Intent>) {
        enableBluetoothLauncher = launcher
    }

    /**
     * BLE 지원 여부 확인
     */
    fun checkBleOperator() {
        val bluetoothLEAvailable =
            applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        if (!bluetoothLEAvailable) {
            Log.e(BLECONT_LOG_TAG, "기기의 BLE 지원 여부 확인 : $bluetoothLEAvailable")
            // TODO: BLE 미지원 기기에 대한 에러 처리 필요
        }

//        if (bluetoothAdapter == null) {
//            Log.e(BLECONT_LOG_TAG, "기기의 BLE 및 Bluetooth Classic 기능 지원 여부 확인 : $bluetoothAdapter")
//            // TODO: Bluetooth 미지원 기기에 대한 에러 처리 필요
//        }
    }

    /**
     * BLE 권한 요청
     */
    fun requestBlePermission(activity: Activity): MutableMap<String, Boolean>{
        //        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        // 1. 블루투스 활성화 요청 <System Setting>
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent) // registerForActivityResult로 처리
            permissionStatus["블루투스 활성화"] = false // 활성화 여부는 런처 결과에서 처리
        } else {
            permissionStatus["블루투스 활성화"] = true
        }

        // 2. Android 12(API 31) 이상에서 BLE 스캔 권한 요청 <Permission Request>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    101 // 요청 코드
                )
                permissionStatus["블루투스 스캔 권한"] = false
            } else permissionStatus["블루투스 스캔 권한"] = true
        } else permissionStatus["블루투스 스캔 권한"] = true

        // 3. 위치 정보 권한 요청 <Permission Request>
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                102 // 요청 코드
            )
            permissionStatus["위치 권한"] = false
        } else {
            permissionStatus["위치 권한"] = true
        }

        // 4. 블루투스 연결 권한 요청 <Permission Request>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    103 // 요청 코드
                )
                permissionStatus["블루투스 연결 권한"] = false
            } else permissionStatus["블루투스 연결 권한"] = true
        } else permissionStatus["블루투스 연결 권한"] = true

        return permissionStatus
    }

    /**
     * BLE 스캔 시작
     */
    fun <T> startBleScan(scanCallback: ScanCallback, popupContainer: T) {
        bluetoothLeScanner.startScan(scanCallback)

        // 10초 후 스캔 중지
        Log.i(BLECONT_LOG_TAG, "스캔 타임아웃 제한시간 : ${SCAN_PERIOD / 1000}초 ")
        when (popupContainer) {
            is LinearLayout -> {  // 특정 팝업창에 Text로 UI 표현하는 경우
                popupContainer.postDelayed({
                    stopBleScan(scanCallback)
                }, SCAN_PERIOD)
            }
            is Handler -> {  // 일반 화면에 Text로 UI 표현하는 경우
                popupContainer.postDelayed({ // SCAN_PERIOD 시간후에 발동되는 지연 함수
                    Log.w(BLECONT_LOG_TAG, "--스캔 타임아웃-- ")
                    bluetoothLeScanner.stopScan(scanCallback)
                }, SCAN_PERIOD)
            }
        }
    }


    /**
     * BLE 스캔 중지
     */
    fun stopBleScan(scanCallback: ScanCallback) {
        try {
            bluetoothLeScanner.stopScan(scanCallback)
            Log.e(BLECONT_LOG_TAG, "블루투스 스캔 정지")
        } catch (e: Exception) {
            Log.e(BLECONT_LOG_TAG, "Failed to stop BLE scan: ${e.message}")
        }
    }



    /**
     * BLE 장치 연결
     */
    @RequiresPermission(value = Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice, onConnectionStateChange: (Boolean) -> Unit) {
        bluetoothGatt = device.connectGatt(applicationContext, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(BLECONT_LOG_TAG, "GATT 서버에 연결되었습니다.")
                    onConnectionStateChange(true)

                    // Android 12(API 31) 이상에서만 BLUETOOTH_CONNECT 권한 확인
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e(BLECONT_LOG_TAG, "BLUETOOTH_CONNECT 권한이 없습니다. discoverServices()를 호출할 수 없습니다.")
                            Toast.makeText(applicationContext, "BLUETOOTH_CONNECT 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                            // 권한이 없으면 discoverServices()를 호출하지 않고 종료
                            return
                        }
                    }

                    // GATT 서비스 검색 시작
                    Log.i(BLECONT_LOG_TAG,"gatt.discoverServices 시작")
                    gatt.discoverServices()
                    Log.i(BLECONT_LOG_TAG,"gatt.discoverServices 시작")

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(BLECONT_LOG_TAG, "Disconnected from GATT server.")
                    onConnectionStateChange(false)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(BLECONT_LOG_TAG, "Services discovered: ${gatt.services}")
                } else {
                    Log.w(BLECONT_LOG_TAG, "onServicesDiscovered received: $status")
                }
            }
        })
    }

    /**
     * BLE 연결 해제
     */
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }




// Callback or 비슷한 함수 들
    /**
     * Bluetooth 활성화 요청 결과 처리
     */
    fun handleBluetoothIntentResult(result: ActivityResult) {
        // 특정 작업(예: 권한 요청, 다른 Activity 호출 등)의 결과를 처리할 Callback 함수
        if (result.resultCode == Activity.RESULT_OK) {
            Log.i(BLECONT_LOG_TAG, "블루투스가 활성화되었습니다.")
            permissionStatus["블루투스 권한"] = true
        } else {
            Log.i(BLECONT_LOG_TAG, "블루투스 활성화가 취소되었습니다.")
            permissionStatus["블루투스 권한"] = false
        }
    }
}