package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

object HorrorSoundManager {
    private val _isSoundEnabled = MutableStateFlow(false)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    private val _isAmbientPlaying = MutableStateFlow(false)
    val isAmbientPlaying = _isAmbientPlaying.asStateFlow()

    private var ambientJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun toggleSound(enabled: Boolean) {
        _isSoundEnabled.value = false
        stopAmbientDrone()
    }

    fun toggleAmbient() {
        stopAmbientDrone()
    }

    /**
     * All sound effects are permanently disabled per user instructions.
     */
    fun playClickSound() {
        // Disabled completely
    }

    fun playScenarioChoiceSound() {
        // Disabled completely
    }

    fun playScenarioTransitionSound() {
        // Disabled completely
    }

    fun playSpookyChime() {
        // Disabled completely
    }

    fun playHeartbeat() {
        // Disabled completely
    }

    fun playDeathSound() {
        // Disabled completely
    }

    fun playScreamShort() {
        // Disabled completely
    }

    fun playVictorySound() {
        // Disabled completely
    }

    fun playCreakingDoorSound() {
        // Disabled completely
    }

    fun playPageTurnSound() {
        // Disabled completely
    }

    fun playStarRatingSound(star: Int = 5) {
        // Disabled completely
    }

    /**
     * Synthesizes atmospheric gothic horror audio with frequency sweeping,
     * tritone dissonance (diminished fifth), and sub-bass impact.
     */
    private fun playHorrorCreak(
        durationMs: Int,
        startFreq: Double,
        endFreq: Double,
        amplitude: Float = 0.4f,
        addDissonantOvertone: Boolean = false,
        addCryptThud: Boolean = false
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val time = i.toDouble() / sampleRate

                val envelope = when {
                    progress < 0.10 -> (progress / 0.10)
                    progress > 0.60 -> ((1.0 - progress) / 0.40)
                    else -> 1.0
                }

                val baseAngle = 2.0 * Math.PI * currentFreq * time
                var sampleVal = sin(baseAngle) * 0.55 + sin(baseAngle * 2.1) * 0.25

                if (addDissonantOvertone) {
                    val dissonantFreq = currentFreq * 1.4142 // Tritone
                    sampleVal += sin(2.0 * Math.PI * dissonantFreq * time) * 0.25
                }

                if (addCryptThud) {
                    val thudEnvelope = (1.0 - progress).coerceIn(0.0, 1.0)
                    sampleVal += sin(2.0 * Math.PI * 45.0 * time) * 0.5 * thudEnvelope
                }

                val finalSample = (sampleVal * Short.MAX_VALUE * amplitude * envelope).toInt()
                buffer[i] = finalSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            scope.launch {
                delay(durationMs.toLong() + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (ignored: Exception) { }
            }
        } catch (e: Exception) {
            playTone(startFreq, durationMs, amplitude)
        }
    }

    private fun playTone(frequency: Double, durationMs: Int, amplitude: Float = 0.4f) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val envelope = when {
                    i < numSamples * 0.1f -> i / (numSamples * 0.1f)
                    i > numSamples * 0.7f -> (numSamples - i) / (numSamples * 0.3f)
                    else -> 1.0f
                }
                val angle = 2.0 * Math.PI * frequency * time
                val sample = (sin(angle) * Short.MAX_VALUE * amplitude * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            
            scope.launch {
                delay(durationMs.toLong() + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (ignored: Exception) { }
            }
        } catch (ignored: Exception) { }
    }

    /**
     * Background ambient music has been removed per user request.
     */
    fun startAmbientDrone() {
        _isAmbientPlaying.value = false
        ambientJob?.cancel()
        ambientJob = null
    }

    fun stopAmbientDrone() {
        _isAmbientPlaying.value = false
        ambientJob?.cancel()
        ambientJob = null
    }
}
