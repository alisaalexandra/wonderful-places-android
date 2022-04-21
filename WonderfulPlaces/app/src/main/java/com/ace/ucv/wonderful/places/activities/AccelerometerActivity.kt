package com.ace.ucv.wonderful.places.activities

import android.hardware.SensorManager
import android.content.Context
import android.hardware.Sensor
import android.os.Bundle
import android.hardware.SensorEvent
import android.widget.TextView
import android.hardware.SensorEventListener
import androidx.appcompat.app.AppCompatActivity
import com.ace.ucv.wonderful.places.R


class AccelerometerActivity : AppCompatActivity(), SensorEventListener {
    //set instances for the sensorManager, accelerometer, and textViews
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var xValue: TextView? = null
    private var yValue: TextView? = null
    private var zValue: TextView? = null
    override fun onSensorChanged(sensorEvent: SensorEvent) {

        //get the current values of the accelerometer for each axis
        val currentXvalue = sensorEvent.values[0]
        val currentYValue = sensorEvent.values[1]
        val currentZValue = sensorEvent.values[2]

        //display the current values of the  accelerometer for each axis onto the
        //textView widgets
        xValue!!.text = resources.getString(R.string.accelerometer_x_value, currentXvalue)
        yValue!!.text = resources.getString(R.string.accelerometer_y_value, currentYValue)
        zValue!!.text = resources.getString(R.string.accelerometer_z_value, currentZValue)
    }

    override fun onAccuracyChanged(sensor: Sensor?, i: Int) {

        //accelerometer does not report accuracy changes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accelerometer)

        //retrieve widgets
        xValue = findViewById(R.id.xValue)
        yValue = findViewById(R.id.yValue)
        zValue = findViewById(R.id.zValue)

        //define instances
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    //register the listener once the activity starts
    override fun onStart() {
        super.onStart()
        if (accelerometer != null) {
            sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    //stop the sensor when the activity stops to reduce battery usage
    override fun onStop() {
        super.onStop()
        sensorManager!!.unregisterListener(this)
    }
}