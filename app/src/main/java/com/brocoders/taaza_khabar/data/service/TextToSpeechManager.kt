package com.brocoders.taaza_khabar.data.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(
    private val context: Context
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TextToSpeechManager"
        private const val UTTERANCE_ID = "NEWS_ARTICLE"
    }

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    // TTS State Management
    private val _ttsState = MutableStateFlow(TTSState.IDLE)
    val ttsState: StateFlow<TTSState> = _ttsState

    private val _isInitializing = MutableStateFlow(false)
    val isInitializing: StateFlow<Boolean> = _isInitializing

    private val _currentText = MutableStateFlow("")
    val currentText: StateFlow<String> = _currentText

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    // Initialize TTS
    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        if (textToSpeech == null && !_isInitializing.value) {
            _isInitializing.value = true
            Log.d(TAG, "Initializing TextToSpeech...")
            textToSpeech = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        _isInitializing.value = false
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                // Set language to English (fallback to default if not available)
                val languageResult = tts.setLanguage(Locale.ENGLISH)
                if (languageResult == TextToSpeech.LANG_MISSING_DATA || 
                    languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "English language not supported, using default")
                    tts.setLanguage(Locale.getDefault())
                }

                // Configure TTS settings for better readability
                tts.setSpeechRate(0.9f) // Slightly slower for clarity
                tts.setPitch(1.0f) // Normal pitch

                // Set utterance progress listener
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "TTS Started")
                        _ttsState.value = TTSState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "TTS Completed")
                        _ttsState.value = TTSState.IDLE
                        _progress.value = 1f
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "TTS Error")
                        _ttsState.value = TTSState.ERROR
                    }

                    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                        super.onRangeStart(utteranceId, start, end, frame)
                        // Calculate progress based on text position
                        val currentTextLength = _currentText.value.length
                        if (currentTextLength > 0) {
                            _progress.value = start.toFloat() / currentTextLength.toFloat()
                        }
                    }
                })

                isInitialized = true
                _ttsState.value = TTSState.IDLE
                Log.d(TAG, "TextToSpeech initialized successfully")
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed")
            _ttsState.value = TTSState.ERROR
        }
    }

    fun speakArticle(title: String, description: String?, content: String? = null) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized")
            return
        }

        // Prepare the full text to speak
        val textToSpeak = buildString {
            append("News Article. ")
            append("Title: $title. ")
            if (!description.isNullOrBlank()) {
                append("Description: $description. ")
            }
            if (!content.isNullOrBlank()) {
                append("Full content: $content")
            }
        }

        speakText(textToSpeak)
    }

    fun speakText(text: String) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized")
            return
        }

        textToSpeech?.let { tts ->
            _currentText.value = text
            _progress.value = 0f
            
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
            }
            
            val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID)
            
            if (result == TextToSpeech.SUCCESS) {
                _ttsState.value = TTSState.SPEAKING
                Log.d(TAG, "Started speaking text")
            } else {
                _ttsState.value = TTSState.ERROR
                Log.e(TAG, "Failed to start speaking")
            }
        }
    }

    fun pauseSpeaking() {
        if (!isInitialized) return
        
        textToSpeech?.let { tts ->
            if (tts.isSpeaking) {
                tts.stop()
                _ttsState.value = TTSState.PAUSED
                Log.d(TAG, "TTS paused")
            }
        }
    }

    fun stopSpeaking() {
        if (!isInitialized) return
        
        textToSpeech?.let { tts ->
            tts.stop()
            _ttsState.value = TTSState.IDLE
            _progress.value = 0f
            _currentText.value = ""
            Log.d(TAG, "TTS stopped")
        }
    }

    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }

    fun isReady(): Boolean {
        return isInitialized && textToSpeech != null
    }

    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate.coerceIn(0.1f, 3.0f))
    }

    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch.coerceIn(0.1f, 2.0f))
    }

    fun getAvailableLanguages(): Set<Locale>? {
        return textToSpeech?.availableLanguages
    }

    fun setLanguage(locale: Locale): Int? {
        return textToSpeech?.setLanguage(locale)
    }

    fun shutdown() {
        textToSpeech?.let { tts ->
            tts.stop()
            tts.shutdown()
            Log.d(TAG, "TTS shutdown")
        }
        textToSpeech = null
        isInitialized = false
        _ttsState.value = TTSState.IDLE
    }
}

enum class TTSState {
    IDLE,
    SPEAKING,
    PAUSED,
    ERROR
} 