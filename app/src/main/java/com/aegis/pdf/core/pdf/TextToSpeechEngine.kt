package com.aegis.pdf.core.pdf

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    data class SpeechState(
        val isSpeaking: Boolean = false,
        val currentPage: Int = 0,
        val totalPages: Int = 0,
        val text: String = ""
    )

    fun initialize(
        onInit: (Boolean) -> Unit,
        onProgress: (SpeechState) -> Unit
    ) {
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            tts?.language = Locale.US
            tts?.setSpeechRate(0.9f)
            tts?.setPitch(1.0f)
            onInit(isInitialized)
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                onProgress(SpeechState(isSpeaking = false))
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onProgress(SpeechState(isSpeaking = false))
            }
        })
    }

    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isInitialized) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }

    fun readPdfAloud(
        file: File,
        onProgress: (SpeechState) -> Unit
    ) {
        try {
            val textExtractor = PdfTextExtractor()
            val text = textExtractor.extractText(file)
            val pages = text.split("\f")

            pages.forEachIndexed { index, pageText ->
                if (pageText.isNotBlank()) {
                    onProgress(
                        SpeechState(
                            isSpeaking = true,
                            currentPage = index + 1,
                            totalPages = pages.size,
                            text = pageText.take(100)
                        )
                    )
                    speak(pageText, "page_$index")
                    Thread.sleep(pageText.length * 30L) // Approximate reading time
                }
            }
            onProgress(SpeechState(isSpeaking = false))
        } catch (e: Exception) {
            onProgress(SpeechState(isSpeaking = false))
        }
    }
}