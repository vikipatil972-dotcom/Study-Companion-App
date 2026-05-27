package com.example.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.util.Random
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

object ImmersiveAudioEngine {
    private const val TAG = "ImmersiveAudioEngine"
    private const val SAMPLE_RATE = 44100

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var audioThread: Thread? = null

    // Track state variables
    @Volatile
    private var activeTrackName: String = "Lofi Focus Beat"

    // Multi-purpose audio generation state
    private val random = Random()
    private var lastPianoTriggerSample: Long = 0
    private var currentPianoFreq = 0f
    private var pianoSampleCounter = 0L

    // Simple IIR filter state for relaxing rain
    private var prevLeftRain = 0f
    private var prevRightRain = 0f

    /**
     * Boosts streaming media volume to maximum programmatically when requested.
     */
    fun maximizeVolume(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            // Programmatically set the stream volume to max
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND)
            Log.d(TAG, "Media volume programmatically elevated to max ($maxVolume)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to programmatically set stream volume", e)
        }
    }

    /**
     * Starts background real-time high-fidelity synthesizer playback.
     */
    @Synchronized
    fun start(context: Context, trackName: String) {
        // Enforce maximum volume
        maximizeVolume(context)

        if (isPlaying) {
            if (activeTrackName == trackName) {
                return // Already playing this track
            }
            activeTrackName = trackName
            return // Switched track name; running loop will adapt in real-time
        }

        activeTrackName = trackName
        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                if (bufferSize > 0) bufferSize else 8192,
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize primary AudioTrack engine. Attempting fallback...", e)
            try {
                // Fallback constructor or buffer
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    16384,
                    AudioTrack.MODE_STREAM
                )
                audioTrack?.play()
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Critical: All AudioTrack initializations failed.", fallbackEx)
                isPlaying = false
                return
            }
        }

        audioThread = Thread({ audioLoop() }, "CosmicStudyAudioEngineThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
        Log.d(TAG, "Audio thread initialized successfully for: $trackName")
    }

    /**
     * Stops the ambient sound synthesizer.
     */
    @Synchronized
    fun stop() {
        if (!isPlaying) return
        isPlaying = false
        try {
            audioThread?.interrupt()
            audioThread = null
            audioTrack?.apply {
                try {
                    stop()
                } catch (e: Exception) { /* ignore */ }
                release()
            }
            audioTrack = null
            Log.d(TAG, "Immersive audio engine stopped successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered while stopping audio engine", e)
        }
    }

    /**
     * Real-time synthesis stereo audio loop feeding the PCM 16-bit buffer.
     */
    private fun audioLoop() {
        val frameCount = 1024
        val shortsBuffer = ShortArray(frameCount * 2) // Stereo frames
        var sampleIndex: Long = 0

        // Reset piano triggers
        lastPianoTriggerSample = 0
        pianoSampleCounter = 0
        currentPianoFreq = 0f

        while (isPlaying) {
            val trackName = activeTrackName

            for (i in 0 until frameCount) {
                val idx = sampleIndex + i
                var leftVal = 0f
                var rightVal = 0f

                when {
                    trackName.contains("Lofi", ignoreCase = true) || trackName.contains("Sunset", ignoreCase = true) -> {
                        // --- LOFI FOCUS BEAT SYNTHESIS ---
                        val bpm = 68.0
                        val samplesPerBeat = (SAMPLE_RATE * 60.0 / bpm).toLong()
                        val beatPos = idx % samplesPerBeat
                        val beatCycle = (idx / samplesPerBeat) % 4

                        // 1. Decaying Kick Drum (Beats 1 and 3)
                        var kick = 0f
                        if (beatCycle == 0L || beatCycle == 2L) {
                            if (beatPos < 9000) {
                                val kickPhase = (beatPos / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * (54.0 - 40.0 * (beatPos / 9000.0))
                                kick = (sin(kickPhase) * exp(-5.0 * beatPos / 9000.0)).toFloat() * 0.65f
                            }
                        }

                        // 2. Crisp Soft Snare (Beats 2 and 4)
                        var snare = 0f
                        if (beatCycle == 1L || beatCycle == 3L) {
                            if (beatPos < 5000) {
                                val noise = random.nextFloat() * 2f - 1f
                                snare = (noise * exp(-5.0 * beatPos / 5000.0) * 0.15f).toFloat()
                                // Add a low body to the snare
                                val snareSubPhase = (beatPos / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * 180.0
                                snare += (sin(snareSubPhase) * exp(-8.0 * beatPos / 5000.0)).toFloat() * 0.12f
                            }
                        }

                        // 3. Ambient Keyboard Chords Changing Every 4 Beats (1 complete measure)
                        val chordSamples = samplesPerBeat * 4
                        val chordCycle = (idx / chordSamples) % 4
                        
                        // Frequencies for a lush jazz progression:
                        // 0: Am9 (A3=220, C4=261.6, E4=329.6, G4=392, B4=493.9)
                        // 1: Dm9 (D3=146.8, F4=349.2, A4=440, C5=523.3, E5=659.3)
                        // 2: G13 (G3=196, B3=246.9, F4=349.2, A4=440, E5=659.3)
                        // 3: Cmaj9 (C3=130.8, E4=329.6, G4=392, B4=493.9, D5=587.3)
                        val chordFreqs = when (chordCycle) {
                            0L -> floatArrayOf(220f, 261.6f, 329.6f, 392f, 493.9f)
                            1L -> floatArrayOf(146.8f, 349.2f, 440f, 523.3f, 659.3f)
                            2L -> floatArrayOf(196f, 246.9f, 349.2f, 440f, 659.3f)
                            else -> floatArrayOf(130.8f, 329.6f, 392f, 493.9f, 587.3f)
                        }

                        var chordSynth = 0f
                        val chordPos = idx % chordSamples
                        // Exponentially fade in and fade out the chords smoothly
                        val chordEnvelope = if (chordPos < 12000) {
                            chordPos / 12000f
                        } else if (chordPos > chordSamples - 15000) {
                            (chordSamples - chordPos) / 15000f
                        } else {
                            1.0f
                        }

                        for (freq in chordFreqs) {
                            val phase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * freq
                            // Summing pure sine wave + soft second harmonic for warmth
                            chordSynth += (sin(phase) + 0.3 * sin(phase * 2.0)).toFloat()
                        }
                        // Normalize the chord
                        chordSynth = (chordSynth / chordFreqs.size) * 0.22f * chordEnvelope

                        // 4. Subtle background vinyl crackle / tape hiss static
                        val crackle = if (random.nextFloat() > 0.999f) (random.nextFloat() * 2f - 1f) * 0.08f else 0f
                        val tapeHiss = (random.nextFloat() * 2f - 1f) * 0.005f

                        val mixedMono = kick + snare + chordSynth + crackle + tapeHiss
                        // Slight stereo panning of chords is simulated by offset phases
                        leftVal = mixedMono
                        rightVal = mixedMono + (chordSynth * 0.2f * sin(idx * 0.0003f)).toFloat()
                    }

                    trackName.contains("Ambient", ignoreCase = true) || trackName.contains("Zen", ignoreCase = true) -> {
                        // --- COSMIC AMBIENT DEEP WORKSPACE SYNTHESIS ---
                        // Lush soundscape made of multi-layered evolving sine sweep oscillators
                        // Osc 1: Deep Root (A2 = 110Hz or C2 = 65.4Hz modulated)
                        // Osc 2: Dominant (E3 = 164.8Hz or G3 = 196Hz)
                        // Osc 3: Ambient Upper 1 (C4 = 261.6Hz)
                        // Osc 4: Evolving Bell (E5 = 659.3Hz modulated by slow LFO)

                        val osc1Phase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * 110.0
                        val osc1 = (sin(osc1Phase) + 0.25 * sin(osc1Phase * 3.0)).toFloat() * 0.35f

                        val osc2Phase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * 164.8
                        val osc2 = sin(osc2Phase).toFloat() * 0.25f

                        val osc3Phase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * 261.6
                        val osc3 = (sin(osc3Phase) + 0.1 * sin(osc3Phase * 2.0)).toFloat() * 0.2f

                        val lfoSlow = sin(idx * 2.0 * Math.PI * 0.02 / SAMPLE_RATE).toFloat() // 50-sec evolution cycle
                        val osc4Freq = 659.3f + 4.0f * lfoSlow
                        val osc4Phase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * osc4Freq
                        val osc4 = sin(osc4Phase).toFloat() * 0.08f * (0.5f + 0.5f * lfoSlow)

                        // Stereo-panned sweeping
                        val sweepL = 0.5f + 0.4f * sin(idx * 2.0 * Math.PI * 0.08 / SAMPLE_RATE).toFloat()
                        val sweepR = 0.5f + 0.4f * cos(idx * 2.0 * Math.PI * 0.08 / SAMPLE_RATE).toFloat()

                        leftVal = (osc1 * 0.8f + osc2 * sweepL + osc3 * 0.5f + osc4 * 0.9f) * 0.5f
                        rightVal = (osc1 * 0.8f + osc2 * sweepR + osc3 * 0.8f + osc4 * 0.1f) * 0.5f
                    }

                    trackName.contains("Forest", ignoreCase = true) || trackName.contains("Rain", ignoreCase = true) -> {
                        // --- DEEP FOREST RAIN + PIANO SYNTHESIS ---
                        // 1. Forest Rain Noise: Generate raw white noise and apply a pleasant sound-softening filter (low-pass)
                        val leftRawNoise = random.nextFloat() * 2f - 1f
                        val rightRawNoise = random.nextFloat() * 2f - 1f

                        // Simple recursive soft filter (creates smooth pink/brown rustle instead of white static)
                        val leftFiltered = 0.93f * prevLeftRain + 0.07f * leftRawNoise
                        val rightFiltered = 0.93f * prevRightRain + 0.07f * rightRawNoise
                        prevLeftRain = leftFiltered
                        prevRightRain = rightFiltered

                        val rainL = leftFiltered * 0.09f
                        val rainR = rightFiltered * 0.09f

                        // 2. Soft Ambient Pentatonic Piano Notes (Triggered occasionally)
                        val samplesSinceLastTrigger = idx - lastPianoTriggerSample
                        if (currentPianoFreq == 0f || samplesSinceLastTrigger > 120000 + random.nextInt(100000)) {
                            // Map of beautiful high soft piano frequencies (Eb major/G minor pentatonic scales)
                            val pentatonicScale = floatArrayOf(
                                392.00f, // G4
                                440.00f, // A4
                                523.25f, // C5
                                587.33f, // D5
                                659.25f, // E5
                                783.99f, // G5
                                880.00f, // A5
                                1046.50f // C6
                            )
                            currentPianoFreq = pentatonicScale[random.nextInt(pentatonicScale.size)]
                            lastPianoTriggerSample = idx
                            pianoSampleCounter = 0
                        }

                        var pianoSynth = 0f
                        if (currentPianoFreq > 0f) {
                            val secondsPlaying = pianoSampleCounter / SAMPLE_RATE.toFloat()
                            if (secondsPlaying < 3.0f) {
                                val pianoPhase = (pianoSampleCounter / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * currentPianoFreq
                                // Soft decaying bell-piano envelope with slight sub-harmonics
                                val pianoEnvelope = exp(-2.3f * secondsPlaying)
                                pianoSynth = (sin(pianoPhase) + 0.25f * sin(pianoPhase * 2.0) + 0.08f * sin(pianoPhase * 3.0)).toFloat() * pianoEnvelope * 0.18f
                                pianoSampleCounter++
                            } else {
                                currentPianoFreq = 0f
                            }
                        }

                        leftVal = rainL + pianoSynth
                        rightVal = rainR + pianoSynth * 0.7f
                    }

                    else -> {
                        // --- BINAURAL FOCUS ALPHA WAVES ---
                        // Brainwave synchronization: Left plays 200Hz, Right plays 210Hz.
                        // Creates a virtual perfect 10Hz Alpha brainwave directly inside the student's inner cortex!
                        val monoCarrierPhase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * 120.0
                        val monoCarrier = sin(monoCarrierPhase).toFloat() * 0.2f

                        val binLPhase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * 200.0
                        val binL = sin(binLPhase).toFloat() * 0.15f

                        val binRPhase = (idx / SAMPLE_RATE.toDouble()) * 2.0 * Math.PI * 210.0
                        val binR = sin(binRPhase).toFloat() * 0.15f

                        leftVal = monoCarrier + binL
                        rightVal = monoCarrier + binR
                    }
                }

                // Interleaved 16-bit PCM shorts limits
                val leftShort = (leftVal.coerceIn(-1.0f, 1.0f) * 32767).toInt().toShort()
                val rightShort = (rightVal.coerceIn(-1.0f, 1.0f) * 32767).toInt().toShort()

                shortsBuffer[i * 2] = leftShort
                shortsBuffer[i * 2 + 1] = rightShort
            }

            try {
                audioTrack?.write(shortsBuffer, 0, shortsBuffer.size)
                sampleIndex += frameCount
            } catch (e: Exception) {
                Log.e(TAG, "Audio write interrupted or failed", e)
                break
            }
        }
    }
}
