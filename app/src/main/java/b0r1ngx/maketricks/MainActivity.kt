package b0r1ngx.maketricks

import b0r1ngx.maketricks.ui.theme.MakeTricksTheme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.WindowManager

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlin.math.roundToInt

// Two types of play: In place (default, first) / In motion

class MainActivity : ComponentActivity() {
    private var text by mutableStateOf("")
    private var show = true

    private var sensorManager: SensorManager? = null
    private var sensorAccel: Sensor? = null
    private var sensorMagnet: Sensor? = null

    private var rotation: Int? = 0

    private val r = FloatArray(9)
    private val valuesAccel = FloatArray(3)
    private val valuesMagnet = FloatArray(3)
    private val valuesResult = FloatArray(3)

    private val deviceOrientation: Unit
        get() {
            SensorManager.getRotationMatrix(r, null, valuesAccel, valuesMagnet)
            SensorManager.getOrientation(r, valuesResult)
            valuesResult[0] = Math.toDegrees(valuesResult[0].toDouble()).toFloat()
            valuesResult[1] = Math.toDegrees(valuesResult[1].toDouble()).toFloat()
            valuesResult[2] = Math.toDegrees(valuesResult[2].toDouble()).toFloat()
        }

    private val listener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    var i = 0
                    while (i < 3) {
                        valuesAccel[i] = event.values[i]
                        i++
                    }
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    var i = 0
                    while (i < 3) {
                        valuesMagnet[i] = event.values[i]
                        i++
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSensors()
        setContent { MakeTricksTheme(text) }
    }

    private fun initSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccel = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagnet = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        show = true
        sensorManager!!.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager!!.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL)

        lifecycleScope.launch {
            while (show) {
                delay(50)
                withContext(Dispatchers.Main) {
                    collectAndShow()
                }
            }
        }

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.display
            } else {
                windowManager.defaultDisplay
            }
        rotation = display?.rotation
    }

    override fun onPause() {
        super.onPause()
        show = false
        sensorManager!!.unregisterListener(listener)
    }

    private fun collectAndShow() {
        deviceOrientation
        text = format(valuesResult)
    }

    private fun format(values: FloatArray): String =
        "X: ${values[1].roundToInt()}°\n" +
        "Y: ${values[2].roundToInt()}°\n" +
        "Z: ${values[0].roundToInt()}°"
}

@Composable
fun MakeTricksTheme(text: String) {
    var shouldShowOnboarding by remember { mutableStateOf(true) }

    MakeTricksTheme {
        if (shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = { shouldShowOnboarding = false })
        } else {
            Surface { // modifier = Modifier.fillMaxSize()
                Column {
                    Text(modifier = Modifier.align(Alignment.End), text = text)
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(onContinueClicked: () -> Unit) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to onboarding screen, here we show you, how to play this significant game!")
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = onContinueClicked
            ) {
                Text("Continue")
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5", apiLevel = 28, showSystemUi = true,
    name = "Pixel 5 Compose preview")
@Composable
fun DefaultPreview() = MakeTricksTheme("Coor\ndina\ntes")