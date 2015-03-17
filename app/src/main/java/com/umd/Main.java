package com.umd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

@SuppressLint("InlinedApi")
public class Main extends Activity implements SensorEventListener {

	private float mLastX, mLastY, mLastZ;
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private final float NOISE = (float) 2.0;
	private AudioManager amg;
	private ArrayList<AccelData> sensorData;
	private ArrayList<Integer> count;
	private SeekBar sb;
	private SurfaceView sv;
	private int max;
	int delay;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		amg = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		sb = (SeekBar) findViewById(R.id.seekBar1);
		count = new ArrayList<Integer>();
		sv = (SurfaceView) findViewById(R.id.surfaceView1);
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				max = progress;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});



		Button close = (Button) findViewById(R.id.button1);
		close.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.exit(0);
			}
		});

		sensorData = new ArrayList<AccelData>();
	}

	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				max = progress;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	/*
	 * protected void onPause() { super.onPause(); }
	 */

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be ignored
	}

	public void onSensorChanged(SensorEvent event) {
		TextView vol = (TextView) findViewById(R.id.volume);
		TextView choT = (TextView) findViewById(R.id.choice);
		TextView numbers = (TextView) findViewById(R.id.textView1);
		int run = amg.getStreamVolume(AudioManager.STREAM_MUSIC);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		long timestamp = System.currentTimeMillis();

		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);
			if (deltaX < NOISE)
				deltaX = (float) 0.0;
			if (deltaY < NOISE)
				deltaY = (float) 0.0;
			if (deltaZ < NOISE)
				deltaZ = (float) 0.0;

			if (delay % 100 == 0) {
				AccelData data = new AccelData(timestamp, x, y, z);
				sensorData.add(data);
			}
			if (delay % 10 == 0) {
				count.add(amg.getStreamVolume(AudioManager.STREAM_MUSIC));
			}
			delay++;

			mLastX = x;
			mLastY = y;
			mLastZ = z;
			vol.setText("Run: " + Integer.toString(run));
			choT.setText("Max: " + Integer.toString(max));

			if (deltaX > 4 || deltaY > 4 || deltaZ > 4) {
				if (amg.getStreamVolume(AudioManager.STREAM_MUSIC) >= max) {
					amg.adjustStreamVolume(AudioManager.STREAM_MUSIC,
							AudioManager.ADJUST_SAME, AudioManager.FLAG_VIBRATE);
				} else {
					amg.adjustStreamVolume(AudioManager.STREAM_MUSIC,
							AudioManager.ADJUST_RAISE,
							AudioManager.FLAG_VIBRATE);
				}
			} else if (deltaX < 2.0) {
				amg.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE);
			}
			String s = "";
			for (int i = 0; i < count.size(); i++) {
				s += " " + count.get(i);
			}
			if (count.get(count.size() - 1) != 0) {
				if (count.get(count.size() - 1) >= max / 2)
					sv.setBackgroundColor(Color.GREEN);
				else {
					sv.setBackgroundColor(Color.YELLOW);
				}
			} else {
				sv.setBackgroundColor(Color.RED);
			}
			sv.setVisibility(View.VISIBLE);
			numbers.setText(s);
		}
	}
}