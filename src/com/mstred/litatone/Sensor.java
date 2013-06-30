package com.mstred.litatone;

import java.text.MessageFormat;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class Sensor extends Activity implements SensorEventListener {

	private android.hardware.Sensor lightSensor;
	private SensorManager sensorManager;
	private Switch swth;
	private TextView text;
	private ToneGenerator toneGen;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
		toneGen = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
		text = (TextView) findViewById(R.id.text);
		swth = (Switch) findViewById(R.id.sensor_switch);
		swth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					onResume();
				else
					onPause();
			}
		});
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		lightSensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sensor, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(android.hardware.Sensor arg0, int arg1) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent evt) {
		int value = Math.round(evt.values[0] * 100);
		text.setText(MessageFormat.format("Light amount: {0} (lux * 100)", value));
		toneGen.startTone(getToneTypeByLux(value));
	}
	
	public int getToneTypeByLux(int value) {
		int tone = 0;
		if (value < 300) {
			tone = ToneGenerator.TONE_DTMF_0;
		} else if (value >= 300 && value < 600) {
			tone = ToneGenerator.TONE_DTMF_1;
		} else if (value >= 600 && value < 900) {
			tone = ToneGenerator.TONE_DTMF_2;
		} else if (value >= 900 && value < 1200) {
			tone = ToneGenerator.TONE_DTMF_3;
		} else if (value >= 1200 && value < 1500) {
			tone = ToneGenerator.TONE_DTMF_4;
		} else if (value >= 1500 && value < 1800) {
			tone = ToneGenerator.TONE_DTMF_5;
		} else if (value >= 1800 && value < 2100) {
			tone = ToneGenerator.TONE_DTMF_6;
		}
		return tone;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
		toneGen.stopTone();
	}
}
