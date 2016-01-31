package com.mstred.litatone;

import java.text.MessageFormat;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

//@SuppressWarnings("unused")
public class Sensor extends Activity implements SensorEventListener {

	private static final int DEFAULT_STREAM = AudioManager.STREAM_MUSIC;
	
	private android.hardware.Sensor lightSensor;
	private SensorManager sensorManager;
	private Switch swth;
	private TextView text;
	private ToneGenerator toneGen;
	
	private boolean itRuns = true;
	private static final int SAMPLE_RATE = 44100;
	private Thread thread;
	private AudioTrack track;
	private int bufferSize;
	private float value;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);
		toneGen = new ToneGenerator(DEFAULT_STREAM, 100);
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
		value = Math.round(evt.values[0] * 100);
		text.setText(MessageFormat.format("Light amount: {0} (lux * 100)", value));
		
		/* DTMF tone generation */
		//toneGen.startTone(getToneTypeByLux((int) value));
		
		/* PCM tone generation */
		thread = new Thread() {
			@Override
			public void run() {
				this.setPriority(MAX_PRIORITY);
				bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
	                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
				track = new AudioTrack(DEFAULT_STREAM, SAMPLE_RATE,
					AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, 
					bufferSize, AudioTrack.MODE_STREAM);
				writeAudioTrack(bufferSize, track);
				track.stop();
				track.release();
			}
		};
		thread.start();
	}
	
	public synchronized void writeAudioTrack(int bufferSize, AudioTrack track) {
		short samples[] = new short[bufferSize];
		double frequency = 440.0;
		double fac = 0.0;

		track.play();
		do {
			frequency = 440 + 440 * value;
			for (int i = 0; i < bufferSize; i++) {
				samples[i] = (short) (10000 * Math.sin(fac));
				fac += 8.0 * Math.atan(1.0) * frequency / SAMPLE_RATE;
			}
			track.write(samples, 0, bufferSize);
		} while (itRuns);
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
		track.stop();
		track.release();
	}
}
