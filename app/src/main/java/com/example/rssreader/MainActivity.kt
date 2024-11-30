package com.example.rssreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.rssreader.webviewmodule.HybridAppBridge

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var hybridAppBridge: HybridAppBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // WebView 초기화
        webView = findViewById(R.id.webView) // activity_main.xml에 정의된 WebView ID
        hybridAppBridge = HybridAppBridge(webView)

        // WebView 설정
        hybridAppBridge.initializeWebView(this)

        // 특정 URL 로드
        val url = "https://app.cocabot.com" // 원하는 URL로 변경
        hybridAppBridge.loadUrl(url)

        // Web으로 데이터 전달 예제
        sendDataToWebExample()


        webView.webViewClient = WebViewClient() // 링크 클릭 시 새 창이 아닌 WebView 내에서 열리도록 설정

        webView.loadUrl("https://app.cocabot.com/") // 원하는 URL로 변경

    }

    override fun onStart() {
        super.onStart()
        // 액티비티가 화면에 나타날 준비가 되었을 때 수행할 작업
        //TODO : 시작시 회사 로고 보이게
    }

    override fun onResume() {
        super.onResume()
        // UI 업데이트나 리스너 등록
        //TODO : 앱이 백그라운드 -> 포그라운드때 핸들링
    }

    //TODO : (SharedPreferences 사용)로그인정보 기억하다가 앱실행시, 자동 로그인

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            // 권한 요청 결과 처리
//            //TODO : 앱에서 권한이 필요 할 때 요청하는 핸들러
//        }
//    }

    /**
     * Web으로 데이터 전달 예제
     */
    private fun sendDataToWebExample() {
        val functionName = "receiveDataFromApp" // JavaScript 함수 이름
        val data = "{\"key\":\"value\"}" // JSON 형식의 데이터
        hybridAppBridge.sendDataToWeb(functionName, data)
    }

    /**
     * 뒤로가기 버튼 처리
     */
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
