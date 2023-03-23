package com.gabyperez.futbolitopocket

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val gravity = FloatArray(3)
    private  val linearAcceleration = FloatArray(3)
    private var sensorAccelerometer: Sensor? = null
    private var mySensor: Sensor? = null
    private lateinit var sensorManager: SensorManager
    private var myLight: Sensor? = null


    private val sensorEventListener : SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {

            val alpha = 0.8f

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event!!.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            // Remove the gravity contribution with the high-pass filter.
            linearAcceleration[0] = event.values[0] - gravity[0]
            linearAcceleration[1] = event.values[1] - gravity[1]
            linearAcceleration[2] = event.values[2] - gravity[2]

            Log.d("Acceleration", "x=${linearAcceleration[0]} ; y=${linearAcceleration[1]} ; " +
                    "z=${linearAcceleration[2]}")

        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            //TODO("Not yet implemented")
        }

    }

    private lateinit var myDrawView: MyDrawView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myDrawView = MyDrawView(this)

        setContentView(myDrawView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val deviceSensors =   sensorManager.getSensorList(Sensor.TYPE_ALL)

        deviceSensors.forEach {
            Log.i("MySensors", it.toString())
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            // Success! There's a magnetometer.
            Log.i("MySensors", "MAGNETOMETER FOUND")
        } else {
            // Failure! No magnetometer.
            Log.i("MySensors", "MAGNETOMETER NOT FOUND")
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            val gravSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_GRAVITY)
            // Use the version 3 gravity sensor.
            mySensor = gravSensors.firstOrNull { it.vendor.contains("Google LLC") && it.version == 3 }
        }
        if (mySensor == null) {
            // Use the accelerometer.
            mySensor = if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            } else {
                // Sorry, there are no accelerometers on your device.
                // You can't play this game.
                null
            }
        }

        Log.i("MySensors", mySensor.toString())

        myLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    override fun onResume() {
        super.onResume()
        myLight?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
            /*sensorManager.registerListener(sensorEventListener,
                light, SensorManager.SENSOR_DELAY_NORMAL)*/
        }
        sensorAccelerometer?.also {
            sensorManager.registerListener(myDrawView,it,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        sensorManager.unregisterListener(myDrawView)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val lux =  p0!!.values[0]
        //(findViewById(R.id.txt) as TextView).text = lux.toString()
        Log.i("LUX", lux.toString())

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }

}

class  MyDrawView (ctx: Context) : View(ctx), SensorEventListener {

    private var xPos = 0f
    private var xAcceleration: Float = 0f
    private var xVelocity: Float = 0.0f
    private var yPos = 0f
    private var yAcceleration: Float = 0f
    private var yVelocity: Float = 0.0f

    private var brush = Paint()
    private var gravity = FloatArray(3)
    private  var linearAcceleration = FloatArray(3)

    init {
        brush.color = Color.RED
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //canvas!!.drawLine(200F, 200F, 500F, 200F, brush)
        canvas!!.drawCircle(xPos, yPos,50.0F, brush)
        //canvas.drawText("Draw Text",400F,400F,brush)

        invalidate()
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val alpha = 0.8f

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event!!.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        // Remove the gravity contribution with the high-pass filter.
        linearAcceleration[0] = event.values[0] - gravity[0]   //x
        linearAcceleration[1] = event.values[1] - gravity[1]    //y
        linearAcceleration[2] = event.values[2] - gravity[2]   //z

        Log.d("Acceleration", "x=${linearAcceleration[0]} ; y=${linearAcceleration[1]} ; " +
                "z=${linearAcceleration[2]}")

        moveBall(linearAcceleration[0], linearAcceleration[1] * -1)

    }

    private fun moveBall( xOrientation: Float,  yOrientation: Float) {
        xAcceleration = xOrientation
        yAcceleration = yOrientation
        updateX()
        updateY()

    }

    private fun updateX() {
        xVelocity -= xAcceleration * 0.3f
        xPos += xVelocity
    }

    private fun updateY() {
        yVelocity -= yAcceleration * 0.3f
        yPos += yVelocity
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
}