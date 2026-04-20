package com.vanoprojects.voxera.audio

import android.media.MediaRecorder
import java.io.File

/**
 * Записывает аудио в OGG (Opus в OGG-контейнере) через MediaRecorder.
 * 16 kHz, моно — подходит для речи.
 */
class OggRecorder(
    private val outputFile: File
) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val channels = 1
    private val bitRate = 32000

    fun start() {
        if (isRecording) return

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.OGG)
            setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            setOutputFile(outputFile.absolutePath)
            setAudioChannels(channels)
            setAudioSamplingRate(sampleRate)
            setAudioEncodingBitRate(bitRate)
            prepare()
            start()
        }
        isRecording = true
    }

    fun stop(): File {
        if (!isRecording) return outputFile
        isRecording = false
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        return outputFile
    }
}
