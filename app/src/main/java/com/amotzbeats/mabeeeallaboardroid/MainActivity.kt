package com.amotzbeats.mabeeeallaboardroid

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import jp.novars.mabeee.sdk.App
import jp.novars.mabeee.sdk.ui.ScanActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    val startWords = listOf("出発進行", "ドクターイエロー")
    val stopWords = listOf("停車します")

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSpeechVisibility(View.INVISIBLE)

        App.getInstance().initializeApp(applicationContext)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)

        val intent = Intent(RecognizerIntent.getVoiceDetailsIntent(applicationContext))
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, application.packageName)
        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer.setRecognitionListener(SpeechRecognitionListener())

        fab.setOnClickListener {
            recognizer.startListening(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_DENIED) {
            fab.isEnabled = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings -> {
                val intent = Intent(this@MainActivity, ScanActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_emergency -> {
                stopMaBeee()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setSpeechVisibility(visibility: Int) {
        balloonImage.visibility = visibility
        speechText.visibility = visibility
    }

    private fun startMaBeee() {
        setMaBeeePWMDuty(100)
    }

    private fun stopMaBeee() {
        setMaBeeePWMDuty(0)
    }

    private fun setMaBeeePWMDuty(pwmDuty: Int) {
        val devices = App.getInstance().devices
        for (device in devices) {
            device.pwmDuty = pwmDuty
        }
    }

    inner class SpeechRecognitionListener : RecognitionListener {
        override fun onBeginningOfSpeech() {}

        override fun onBufferReceived(buffer: ByteArray) {}

        override fun onEndOfSpeech() {
            setSpeechVisibility(View.INVISIBLE)
        }

        override fun onError(error: Int) {
            print(error)
            setSpeechVisibility(View.INVISIBLE)
        }

        override fun onEvent(eventType: Int, params: Bundle) {}

        override fun onPartialResults(partialResults: Bundle) {}

        override fun onReadyForSpeech(params: Bundle) {
            speechText.text = "アナウンスしてください"
            setSpeechVisibility(View.VISIBLE)
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onResults(results: Bundle) {
            val recData = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
            var result: String = recData.first()
            if (recData.intersect(startWords).size > 0) {
                result = recData.intersect(startWords).first()
                startMaBeee()
            } else if (recData.intersect(stopWords).size > 0) {
                result = recData.intersect(stopWords).first()
                stopMaBeee()
            }
            speechText.text = result
        }
    }

}
