package com.sc2079.androidcontroller.features.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Text-to-Speech Manager
 * Handles TTS initialization and speech synthesis
 */
class TextToSpeechManager(
    private val context: Context,
    private val onInitialized: (Boolean) -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isSpeaking = false

    init {
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            if (isInitialized) {
                tts?.language = Locale.getDefault()
            }
            onInitialized(isInitialized)
        }
    }

    /**
     * Speak the given text
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || isSpeaking) {
            return
        }

        tts?.let { textToSpeech ->
            isSpeaking = true
            
            // Set up utterance progress listener
            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // Speech started
                }

                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    onComplete?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    onComplete?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?, errorCode: Int) {
                    isSpeaking = false
                    onComplete?.invoke()
                }
            })

            // Speak the text
            val utteranceId = System.currentTimeMillis().toString()
            textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )
        }
    }

    /**
     * Stop speaking
     */
    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    /**
     * Check if currently speaking
     */
    fun isCurrentlySpeaking(): Boolean = isSpeaking

    /**
     * Check if TTS is initialized
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Clean up resources
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        isSpeaking = false
    }
}

/**
 * Composable hook to remember and manage TextToSpeechManager
 */
@Composable
fun rememberTextToSpeechManager(
    onInitialized: (Boolean) -> Unit = {}
): TextToSpeechManager? {
    val context = LocalContext.current
    val updatedOnInitialized by rememberUpdatedState(onInitialized)
    
    val ttsManager = remember {
        TextToSpeechManager(context) { initialized ->
            updatedOnInitialized(initialized)
        }
    }

    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    return ttsManager
}

