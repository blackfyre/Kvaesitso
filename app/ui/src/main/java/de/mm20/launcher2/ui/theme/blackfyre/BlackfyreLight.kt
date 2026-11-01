package de.mm20.launcher2.ui.theme.blackfyre

import android.animation.ValueAnimator
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Fork (blackfyre-depth): where the light sits, as the design system's --bf-cx/--bf-cy. Each is
 * within -1 to 1 and says which way, on screen, shadows fall: positive [cx] to the right, positive
 * [cy] down. (0, 0) is the light straight above.
 */
data class Light(val cx: Float, val cy: Float) {
    companion object {
        val Overhead = Light(0f, 0f)
    }
}

/**
 * The light for a light fixed above the device in the world, from a gravity sample. Android reports
 * gravity as the reaction vector, which points up: its x is positive when the device's right edge
 * is up. [rotation] is the display rotation ([Surface.ROTATION_0] …), so the result is in screen
 * axes. Shadows fall the opposite way to the vector.
 */
fun lightFromGravity(gx: Float, gy: Float, gz: Float, rotation: Int): Light {
    val g = sqrt(gx * gx + gy * gy + gz * gz)
    if (g == 0f) return Light.Overhead
    // Screen axes: x to the right, y down. At ROTATION_90 the device's top edge points to the
    // screen's left (screen right is device -y, screen up is device +x); at ROTATION_270 to its right.
    val (sx, sy) = when (rotation) {
        Surface.ROTATION_90 -> -gy to -gx
        Surface.ROTATION_180 -> -gx to gy
        Surface.ROTATION_270 -> gy to gx
        else -> gx to -gy
    }
    return Light((-sx / g).coerceIn(-1f, 1f), (-sy / g).coerceIn(-1f, 1f))
}

/** The time constant of the light's lag (design system: the shadows lag the pointer by 250 ms). */
const val LightLagNanos = 250_000_000L

/**
 * One step of a first-order low-pass filter with a [LightLagNanos] time constant: [previous]
 * moved towards [sample] after [elapsedNanos]. Without a previous value, the sample is taken as is.
 */
fun smoothLight(previous: Light?, sample: Light, elapsedNanos: Long): Light {
    if (previous == null) return sample
    val a = 1f - exp(-elapsedNanos.coerceAtLeast(0L).toDouble() / LightLagNanos).toFloat()
    return Light(
        previous.cx + (sample.cx - previous.cx) * a,
        previous.cy + (sample.cy - previous.cy) * a,
    )
}

/**
 * Fork (blackfyre-depth): the light, following the device's tilt. The gravity sensor is registered
 * only while [light] has subscribers (depth panels on a resumed screen), and never when the system's
 * animations are off or there is no gravity sensor: the light then stays overhead.
 */
class BlackfyreLight(context: Context, scope: CoroutineScope) {
    val light: StateFlow<Light> = gravityLight(context)
        .stateIn(scope, SharingStarted.WhileSubscribed(), Light.Overhead)
}

private fun gravityLight(context: Context): Flow<Light> = flow {
    val sensorManager = context.getSystemService<SensorManager>()
    val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    if (sensorManager == null || sensor == null || !ValueAnimator.areAnimatorsEnabled()) {
        emit(Light.Overhead)
        return@flow
    }
    emitAll(callbackFlow {
        var last: Light? = null
        var lastTime = 0L
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                val rotation = ContextCompat.getDisplayOrDefault(context).rotation
                val (x, y, z) = event.values
                val light = smoothLight(last, lightFromGravity(x, y, z, rotation), event.timestamp - lastTime)
                last = light
                lastTime = event.timestamp
                trySend(light)
            }
        }
        if (!sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)) {
            Log.w("BlackfyreLight", "Could not register the gravity sensor")
            trySend(Light.Overhead)
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    })
}
