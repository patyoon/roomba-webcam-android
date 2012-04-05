package cis542.roomba.android;


import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;


public class MainActivity extends Activity implements
OnBufferingUpdateListener, OnCompletionListener,
OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback,
 SensorEventListener{

	private static final String TAG = "VideoStreamer";
	private int mVideoWidth;
	private int mVideoHeight;
	private MediaPlayer mMediaPlayer;
	private SurfaceView mPreview;
	private SurfaceHolder holder;
	private String path;
	private static final String MEDIA = "media";
	private static final int STREAM_VIDEO = 1;
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;
	private SensorManager mSensorManager;
	private PowerManager mPowerManager;
	private WindowManager mWindowManager;
	private Display mDisplay;
	private WakeLock mWakeLock;
    private Sensor mAccelerometer;
    private float mXOrigin;
    private float mYOrigin;
    private float mSensorX;
    private float mSensorY;
    private long mSensorTimeStamp;
    private long mCpuTimeStamp;

	/* TCP Client Network Task */
	NetworkTask networktask;
	String ipAddress = "";
	int portNumber = -1;

	private static final int EDIT_ACTION = 1;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Intent i = new Intent(this, TextEntryActivity.class);
		this.startActivityForResult(i, EDIT_ACTION);
		setContentView(R.layout.main);

		/* Video Surface View */

		mPreview = (SurfaceView) findViewById(R.id.surface);
		holder = mPreview.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		/* Accelerometer */ 
		// Get an instance of the SensorManager
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Get an instance of the PowerManager
		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		// Get an instance of the WindowManager
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();
		// Create a bright wake lock
		mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
				.getName());
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);


		/* TCP client network task */
		networktask = new NetworkTask(); //Create initial instance so SendDataToNetwork doesn't throw an error.

		/* Show ready dialog to get IP and port number of server */
	}

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        /*
         * record the accelerometer data, the event's timestamp as well as
         * the current time. The latter is needed so we can calculate the
         * "present" time during rendering. In this application, we need to
         * take into account how the screen is rotated with respect to the
         * sensors (which always return data in a coordinate space aligned
         * to with the screen in its native orientation).
         */

        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                mSensorX = event.values[0];
                mSensorY = event.values[1];
                //TODO(yeyoon):play with rotation
                break;
            case Surface.ROTATION_90:
                mSensorX = -event.values[1];
                mSensorY = event.values[0];
                //TODO(yeyoon):play with rotation
                break;
            case Surface.ROTATION_180:
                mSensorX = -event.values[0];
                mSensorY = -event.values[1];
                //TODO(yeyoon):play with rotation
                break;
            case Surface.ROTATION_270:
                mSensorX = event.values[1];
                mSensorY = -event.values[0];
                //TODO(yeyoon):play with rotation
                break;
        }

        mSensorTimeStamp = event.timestamp;
        mCpuTimeStamp = System.nanoTime();
    }
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case EDIT_ACTION:
			try {
				String ip = data.getStringExtra("ip");
				if (ip != null && ip.length() > 0) {
					this.ipAddress = ip;
				}
				int port = data.getIntExtra("port", -1);
				if (port != -1 ) {
					this.portNumber = port;
				}
			} catch (Exception e) {
			}
			break;
		default:
			break;
		}
	}


	private void playVideo(Integer Media) {
		doCleanUp();
		try {
			/*
			 * TODO: Set path variable to progressive streamable mp4 or
			 * 3gpp format URL. Http protocol should be used.
			 * Mediaplayer can only play "progressive streamable
			 * contents" which basically means: 1. the movie atom has to
			 * precede all the media data atoms. 2. The clip has to be
			 * reasonably interleaved.
			 * 
			 */
			//example path
			path = "http://people.sc.fsu.edu/~jburkardt/data/mp4/cvt_size_movie.mp4";
			if (path == "") {
				// Tell the user to provide a media file URL.
				Toast
				.makeText(
						MainActivity.this,
						"Please edit MediaPlayerDemo_Video Activity,"
								+ " and set the path variable to your media file URL.",
								Toast.LENGTH_LONG).show();

			}

			// Create a new media player and set the listeners
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.setDisplay(holder);
			mMediaPlayer.prepare();
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


		} catch (Exception e) {
			Log.e(TAG, "error: " + e.getMessage(), e);
		}
	}

	public void onBufferingUpdate(MediaPlayer arg0, int percent) {
		Log.d(TAG, "onBufferingUpdate percent:" + percent);

	}

	public void onCompletion(MediaPlayer arg0) {
		Log.d(TAG, "onCompletion called");
	}

	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.v(TAG, "onVideoSizeChanged called");
		if (width == 0 || height == 0) {
			Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
			return;
		}
		mIsVideoSizeKnown = true;
		mVideoWidth = width;
		mVideoHeight = height;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void onPrepared(MediaPlayer mediaplayer) {
		Log.d(TAG, "onPrepared called");
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
		Log.d(TAG, "surfaceChanged called");

	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		Log.d(TAG, "surfaceDestroyed called");
	}


	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated called");
		playVideo(MainActivity.STREAM_VIDEO);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		
		//stop accelerometer
        mSensorManager.unregisterListener(this);
		
		doCleanUp();
	}

	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	public void pauseButtonListener(View v) {
		onPause();
	}	
	
	private void doCleanUp() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}

	private void startVideoPlayback() {
		Log.v(TAG, "startVideoPlayback");
		holder.setFixedSize(mVideoWidth, mVideoHeight);
		mMediaPlayer.start();
	}

	public void quitButtonListener(View v) {
		onDestroy();
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Stop the game
		//mSimulationView.stopSimulation();

		// and release our wake-lock
		//mWakeLock.release();

		releaseMediaPlayer();
		doCleanUp();
	}

	/* Roomba locomotion */

	private void roombaStop(){
		networktask = new NetworkTask(); //New instance of NetworkTask
		networktask.execute();
		networktask.SendDataToNetwork("p"); 
	}
	
	private void roombaForward(){
		networktask = new NetworkTask(); //New instance of NetworkTask
		networktask.execute();
		networktask.SendDataToNetwork("w"); 
	}
	
	private void roombaBackward(){
		networktask = new NetworkTask(); //New instance of NetworkTask
		networktask.execute();
		networktask.SendDataToNetwork("s"); 
	}
	
	private void roombaLeftSpin(){
		networktask = new NetworkTask(); //New instance of NetworkTask
		networktask.execute();
		networktask.SendDataToNetwork("a"); 
	}
	
	private void roombaRightSpin(){
		networktask = new NetworkTask(); //New instance of NetworkTask
		networktask.execute();
		networktask.SendDataToNetwork("d"); 
	}
	
	/* Moving Roomba */
	public void forwardButtonListener(View v) {
		roombaForward();
	}

	public void backwardButtonListener(View v) {
		roombaBackward();
	}

	public void leftSpinButtonListener(View v) {
		roombaLeftSpin();
	}

	public void rightButtonListener(View v) {
		roombaRightSpin();
	}
	
	public void stopButtonListener(View v) {
		roombaStop();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}
}