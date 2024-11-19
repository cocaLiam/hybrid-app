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
//
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
//
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
//
import com.example.rssreader.ui.theme.RSSReaderTheme
import kotlin.reflect.typeOf


//// 사용자 정의 정수 [ 0 이상이면 Ok ]
//const val REQUEST_ENABLE_BT = 100
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//
////        // 블루투스 클래식 기능이 핸드폰에 있는지 확인
////        val bluetoothAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
//
//        // BLE 기능이 핸드폰에 있는지 확인
//        val bluetoothLEAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
//        Log.i(" - MainActivity", "블루투스 기능이 있는지 : $bluetoothLEAvailable")
//
//        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
//        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
//        if (bluetoothAdapter == null) {
//            // Device doesn't support Bluetooth
//            Log.e(" - MainActivity", "블루투스 기능 없음")
//            System.exit(0)
//        }
//
//        Log.i(" - MainActivity", "블루투스 상태 : ${bluetoothAdapter?.isEnabled} << false면 Off")
//        if (bluetoothAdapter?.isEnabled == false) {
//            // 권장사항이 아님
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
//            Log.i(" - MainActivity", "블루투스가 활성화 : ${enableBtIntent}")
////            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
////            enableBluetoothLauncher.launch(enableBtIntent)
//        }
//    }
//
//}









class MainActivity : ComponentActivity() {
    // 1. ActivityResultLauncher를 클래스의 멤버 변수로 선언합니다.
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

//        // 블루투스 클래식 기능이 핸드폰에 있는지 확인
//        val bluetoothAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)

        // 1. 기기의 BLE 지원 여부 확인
        val bluetoothLEAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        if (!bluetoothLEAvailable){
            Log.e(" - MainActivity", "기기의 BLE 지원 여부 확인 : $bluetoothLEAvailable")
        }

        // 2. 기기의 BLE 및 Bluetooth Classic 기능 지원 여부 확인
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.e(" - MainActivity", "기기의 BLE 및 Bluetooth Classic 기능 지원 여부 확인 : $bluetoothAdapter")
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
            Log.i(" - MainActivity", "블루투스 활성화 요청: $enableBtIntent")

        }
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

}
