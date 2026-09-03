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
        } else {
            startAmbientDrone()
        }
    }

    fun toggleAmbient() {
        if (_isAmbientPlaying.value) {
            stopAmbientDrone()
        } else {
            startAmbientDrone()
        }
    }

    /**
     * Eerie low-pitch bone click / crypt tap for general UI navigation.
     */
    fun playClickSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playHorrorCreak(
                durationMs = 95,
                startFreq = 180.0,
                endFreq = 65.0,
                amplitude = 0.35f,
                addCryptThud = true
            )
        }
    }

    /**
     * Heavy Gothic crypt thud + dissonant frequency sweep for scenario decisions.
     */
    fun playScenarioChoiceSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playHorrorCreak(
                durationMs = 320,
                startFreq = 260.0,
                endFreq = 50.0,
                amplitude = 0.6f,
                addDissonantOvertone = true,
                addCryptThud = true
            )
        }
    }

    /**
     * Sinister dimensional shift for screen changes and stage transitions.
     */
    fun playScenarioTransitionSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playHorrorCreak(durationMs = 220, startFreq = 140.0, endFreq = 220.0, amplitude = 0.4f, addDissonantOvertone = true)
            delay(100)
            playHorrorCreak(durationMs = 360, startFreq = 220.0, endFreq = 42.0, amplitude = 0.55f, addCryptThud = true)
        }
    }

    /**
     * Supernatural cursed chime: minor chords with dissonant overtones (E minor / Tritone).
     */
    fun playSpookyChime() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequency = 329.63, durationMs = 180, amplitude = 0.35f) // E4
            delay(90)
            playTone(frequency = 392.00, durationMs = 220, amplitude = 0.35f) // G4
            delay(110)
            playTone(frequency = 466.16, durationMs = 400, amplitude = 0.4f)  // A#4 (Tritone dread)
        }
    }

    /**
     * Realistic slow panic heartbeat sound effect (lub-dub).
     */
    fun playHeartbeat() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequency = 52.0, durationMs = 110, amplitude = 0.7f)
            delay(140)
            playTone(frequency = 44.0, durationMs = 160, amplitude = 0.55f)
        }
    }

    /**
     * Terrifying ghostly scream / doom plunge when walking into a death trap.
     */
    fun playDeathSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            // Shrieking high dissonant tritone descending rapidly
            playHorrorCreak(durationMs = 500, startFreq = 660.0, endFreq = 85.0, amplitude = 0.75f, addDissonantOvertone = true)
            delay(260)
            // Heavy crypt earthquake doom impact
            playHorrorCreak(durationMs = 850, startFreq = 80.0, endFreq = 30.0, amplitude = 0.85f, addCryptThud = true)
        }
    }

    /**
     * Eerie relief / ancient temple blessing sound.
     */
    fun playVictorySound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playTone(frequency = 220.0, durationMs = 150, amplitude = 0.35f)
            delay(120)
            playTone(frequency = 277.18, durationMs = 170, amplitude = 0.38f)
            delay(130)
            playTone(frequency = 329.63, durationMs = 220, amplitude = 0.4f)
            delay(150)
            playTone(frequency = 440.00, durationMs = 500, amplitude = 0.45f)
        }
    }

    /**
     * Creaking haunted door opening / closing sound.
     */
    fun playCreakingDoorSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playHorrorCreak(
                durationMs = 650,
                startFreq = 380.0,
                endFreq = 160.0,
                amplitude = 0.45f,
                addDissonantOvertone = true
            )
        }
    }

    /**
     * Ancient parchment flip / rustle sound with low crypt whisper.
     */
    fun playPageTurnSound() {
        if (!_isSoundEnabled.value) return
        scope.launch {
            playHorrorCreak(
                durationMs = 180,
                startFreq = 320.0,
                endFreq = 90.0,
                amplitude = 0.38f,
                addDissonantOvertone = false,
                addCryptThud = false
            )
        }
    }

    /**
     * Haunted star blessing sound with eerie harmonic overtones.
     */
    fun playStarRatingSound(star: Int = 5) {
        if (!_isSoundEnabled.value) return
        val baseFreq = 220.0 + (star * 45.0)
        scope.launch {
            playTone(frequency = baseFreq, durationMs = 160, amplitude = 0.35f)
            delay(80)
            playTone(frequency = baseFreq * 1.4142, durationMs = 260, amplitude = 0.38f) // Dissonant mystical chime
        }
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
     * Continuously synthesizes an authentic gothic horror ambient loop in background:
     * - Haunting howling winter wind noise with slow sweeping modulation
     * - Eerie sub-harmonic crypt drones (43Hz + 55Hz + 61Hz binaural beats)
     * - Periodic distant ghostly whispers & antique wood creaks
     */
    fun startAmbientDrone() {
        if (!_isSoundEnabled.value || _isAmbientPlaying.value) return
        _isAmbientPlaying.value = true
        ambientJob?.cancel()
        ambientJob = scope.launch {
            val sampleRate = 16000
            val chunkDurationMs = 2500
            val numSamples = (sampleRate * (chunkDurationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var cycleCount = 0

            while (isActive && _isAmbientPlaying.value && _isSoundEnabled.value) {
                cycleCount++
                val windBaseFreq = 90.0 + sin(cycleCount * 0.3) * 35.0
                val hasCreak = cycleCount % 4 == 0
                val hasWhisper = cycleCount % 3 == 0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val chunkProgress = i.toDouble() / numSamples

                    // Smooth crossfade envelope to ensure seamless looping without clicks
                    val loopEnvelope = when {
                        chunkProgress < 0.12 -> (chunkProgress / 0.12)
                        chunkProgress > 0.88 -> ((1.0 - chunkProgress) / 0.12)
                        else -> 1.0
                    }

                    // 1. Deep Sub-Bass Crypt Resonance (43Hz + 55Hz + 58Hz eerie acoustic beating)
                    val subDrone = (sin(2.0 * Math.PI * 43.0 * t) * 0.35 +
                            sin(2.0 * Math.PI * 55.0 * t) * 0.25 +
                            sin(2.0 * Math.PI * 58.5 * t) * 0.20)

                    // 2. Haunting Howling Wind (Sweeping filtered white noise with wind gusting)
                    val gustEnvelope = 0.5 + 0.5 * sin(2.0 * Math.PI * 0.4 * t + cycleCount)
                    val noise = (Random.nextFloat() * 2.0 - 1.0)
                    val windTone = sin(2.0 * Math.PI * windBaseFreq * t) * 0.35
                    val windSound = (noise * 0.25 + windTone * 0.75) * gustEnvelope * 0.35

                    // 3. Spooky distant creak / whisper overlay on specific cycles
                    var fxSound = 0.0
                    if (hasCreak && chunkProgress in 0.3..0.7) {
                        val creakProg = (chunkProgress - 0.3) / 0.4
                        val creakFreq = 280.0 - creakProg * 140.0
                        fxSound += sin(2.0 * Math.PI * creakFreq * t) * 0.18 * sin(creakProg * Math.PI)
                    }
                    if (hasWhisper && chunkProgress in 0.4..0.8) {
                        val whisperProg = (chunkProgress - 0.4) / 0.4
                        fxSound += (Random.nextFloat() * 2.0 - 1.0) * 0.14 * sin(whisperProg * Math.PI)
                    }

                    val mixed = (subDrone * 0.45 + windSound * 0.40 + fxSound) * loopEnvelope * 0.28
                    buffer[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
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
                    delay(chunkDurationMs - 120L)
                    try {
                        track.stop()
                        track.release()
                    } catch (ignored: Exception) { }
                } catch (e: Exception) {
                    delay(chunkDurationMs.toLong())
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
