package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object HorrorSoundManager {
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    private val _isAmbientPlaying = MutableStateFlow(false)
    val isAmbientPlaying = _isAmbientPlaying.asStateFlow()

    private var ambientJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun toggleSound(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        if (!enabled) {
            stopAmbientDrone()
        }
    }

    fun toggleAmbient() {
        if (_isAmbientPlaying.value) {
            stopAmbientDrone()
        } else {
            startAmbientDrone()
        }
    }

    fun playClickSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playHorrorCreak(durationMs = 90, startFreq = 220.0, endFreq = 140.0, amplitude = 0.35f)
        }
    }

    /**
     * Cinematic procedural gothic horror sound effect for scenario clicks and choices.
     * Features sub-bass crypt thud + dissonant frequency sweep + ominous acoustic beating.
     * Never sounds like a plain electronic beep.
     */
    fun playScenarioChoiceSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playHorrorCreak(
                durationMs = 280,
                startFreq = 340.0,
                endFreq = 95.0,
                amplitude = 0.55f,
                addDissonantOvertone = true,
                addCryptThud = true
            )
        }
    }

    /**
     * Atmospheric supernatural transition effect when advancing stages or switching scenarios.
     */
    fun playScenarioTransitionSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            // Sinister two-stage dimensional sweep
            playHorrorCreak(durationMs = 180, startFreq = 160.0, endFreq = 240.0, amplitude = 0.35f, addDissonantOvertone = true)
            delay(120)
            playHorrorCreak(durationMs = 320, startFreq = 240.0, endFreq = 65.0, amplitude = 0.5f, addCryptThud = true)
        }
    }

    fun playSpookyChime() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequency = 440.0, durationMs = 120, amplitude = 0.3f)
            delay(80)
            playTone(frequency = 330.0, durationMs = 180, amplitude = 0.35f)
            delay(120)
            playTone(frequency = 220.0, durationMs = 350, amplitude = 0.4f)
        }
    }

    fun playHeartbeat() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequency = 75.0, durationMs = 100, amplitude = 0.6f)
            delay(120)
            playTone(frequency = 65.0, durationMs = 140, amplitude = 0.45f)
        }
    }

    fun playStarRatingSound(stars: Int) {
        if (!_isSoundEnabled.value) return
        scope.launch {
            val baseFreq = 300.0 + (stars * 70.0)
            playTone(frequency = baseFreq, durationMs = 140, amplitude = 0.4f)
            delay(60)
            playTone(frequency = baseFreq * 1.25, durationMs = 200, amplitude = 0.3f)
        }
    }

    fun playPageTurnSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequency = 520.0, durationMs = 40, amplitude = 0.2f)
            delay(30)
            playTone(frequency = 380.0, durationMs = 70, amplitude = 0.25f)
        }
    }

    fun playDeathSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            // Terrifying descending doom plunge with deep sub-bass tremor
            playHorrorCreak(durationMs = 450, startFreq = 220.0, endFreq = 48.0, amplitude = 0.65f, addDissonantOvertone = true, addCryptThud = true)
            delay(280)
            playHorrorCreak(durationMs = 700, startFreq = 90.0, endFreq = 35.0, amplitude = 0.7f, addCryptThud = true)
        }
    }

    fun playVictorySound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequency = 260.0, durationMs = 120, amplitude = 0.3f)
            delay(100)
            playTone(frequency = 329.6, durationMs = 140, amplitude = 0.35f)
            delay(110)
            playTone(frequency = 392.0, durationMs = 180, amplitude = 0.4f)
            delay(130)
            playTone(frequency = 523.25, durationMs = 400, amplitude = 0.45f)
        }
    }

    /**
     * Synthesizes rich, atmospheric cinematic horror audio with frequency sweeping,
     * tritone dissonance (the devil's interval), and low-end sub-bass resonance.
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

                // Envelope with soft attack and smooth decay
                val envelope = when {
                    progress < 0.12 -> (progress / 0.12)
                    progress > 0.65 -> ((1.0 - progress) / 0.35)
                    else -> 1.0
                }

                // Primary sweeping horror wave + eerie odd harmonics
                val baseAngle = 2.0 * Math.PI * currentFreq * time
                var sampleVal = sin(baseAngle) * 0.55 + sin(baseAngle * 2.1) * 0.25

                // Tritone / minor second dissonance (creates spine-tingling acoustic dread)
                if (addDissonantOvertone) {
                    val dissonantFreq = currentFreq * 1.4142 // Square root of 2 = Diminished fifth (Tritone)
                    sampleVal += sin(2.0 * Math.PI * dissonantFreq * time) * 0.22
                }

                // Heavy crypt sub-thud (45Hz impact)
                if (addCryptThud) {
                    val thudEnvelope = (1.0 - progress).coerceIn(0.0, 1.0)
                    sampleVal += sin(2.0 * Math.PI * 48.0 * time) * 0.45 * thudEnvelope
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
            // Graceful fallback
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
                // Add soft envelope fade-in / fade-out to prevent popping
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
            
            // Clean up after playback
            scope.launch {
                delay(durationMs.toLong() + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) { }
            }
        } catch (e: Exception) {
            // ToneGenerator fallback
            try {
                val tg = ToneGenerator(AudioManager.STREAM_MUSIC, (amplitude * 100).toInt())
                tg.startTone(ToneGenerator.TONE_PROP_BEEP, durationMs)
            } catch (ignored: Exception) { }
        }
    }

    fun startAmbientDrone() {
        if (!_isSoundEnabled.value || _isAmbientPlaying.value) return
        _isAmbientPlaying.value = true
        ambientJob?.cancel()
        ambientJob = scope.launch {
            val sampleRate = 16000
            val durationMs = 2000
            val numSamples = sampleRate * 2
            val buffer = ShortArray(numSamples)

            while (isActive && _isAmbientPlaying.value && _isSoundEnabled.value) {
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Binaural gothic drone wave: 55Hz (A1) + 57Hz eerie beating tone + 110Hz sub
                    val wave = (sin(2.0 * Math.PI * 55.0 * t) * 0.5 +
                            sin(2.0 * Math.PI * 57.5 * t) * 0.3 +
                            sin(2.0 * Math.PI * 110.0 * t) * 0.2)
                    buffer[i] = (wave * Short.MAX_VALUE * 0.18).toInt().toShort()
                }

                try {
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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

                    track.write(buffer, 0, buffer.size)
                    track.play()
                    delay(1900)
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    delay(2000)
                }
            }
        }
    }

    fun stopAmbientDrone() {
        _isAmbientPlaying.value = false
        ambientJob?.cancel()
        ambientJob = null
    }
}
