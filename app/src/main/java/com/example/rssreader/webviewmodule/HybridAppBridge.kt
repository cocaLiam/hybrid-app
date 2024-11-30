package com.example.rssreader.webviewmodule

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.Toast

class HybridAppBridge(private val webView: WebView) {

    /**
     * WebView 초기화
     * Web(client) -> APP(server) WebView 설정
     */
    fun initializeWebView(context: Context) {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true // JavaScript 활성화
        webSettings.domStorageEnabled = true // DOM storage 활성화 // 로컬 스토리지 사용( Token 때문에 필요함 )
        webSettings.allowFileAccess = true // 파일 접근 허용
        webSettings.allowContentAccess = true
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true // JavaScript로 새 창 열기 허용

        // WebView에 JavaScript 인터페이스 추가
        webView.addJavascriptInterface(WebAppInterface(context), "AndroidApi")

        // WebView 클라이언트 설정
        webView.webViewClient = CustomWebViewClient()
        webView.webChromeClient = WebChromeClient()
    }

    /**
     * 특정 URL 로드
     * @param url 로드할 URL
     */
    fun loadUrl(url: String) {
        webView.post {
            webView.loadUrl(url)
        }
    }

    /**
     * WebView 뒤로 가기
     */
    fun goBack() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    /**
     * APP(client) -> Web(server) 데이터 전달
     * App에서 Web으로 데이터를 전달하는 함수
     * @param functionName JavaScript 함수 이름
     * @param data 전달할 데이터 (JSON 형식)
     */
    fun sendDataToWeb(jsFuncitonName: String, data: String) {
        webView.post {
            webView.evaluateJavascript("javascript:$jsFuncitonName('$data')",)
        }
    }

    /**
     * Web(client) -> APP(server) API 호출
     * Web에서 App으로 데이터를 전달받는 인터페이스
     */
    class WebAppInterface(private val context: Context) {

        @JavascriptInterface
        fun showToast(message: String) {
            // Web에서 전달받은 메시지를 Toast로 표시
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun logMessage(message: String) {
            // Web에서 전달받은 메시지를 로그로 출력
            android.util.Log.d("WebAppInterface", message)
        }
    }

    /**
     * WebViewClient 커스터마이징
     */
    private class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            // 모든 URL을 WebView에서 처리
            return false
        }
    }
}
