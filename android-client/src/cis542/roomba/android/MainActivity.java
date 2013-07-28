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
        public void onCreate(Bundle bundle) {
                super.onCreate(bundle);
                setContentView(R.layout.main);

                /* Video Surface View */
                /*
                mPreview = (SurfaceView) findViewById(R.id.surface);
                holder = mPreview.getHolder();
                holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                */
                /* Accelerometer */
                /*
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
                */

                this.ipAddress = "";
                this.portNumber = 0;

                /* Show ready dialog to get IP and port number of server */
                Intent i = new Intent(this, TextEntryActivity.class);
                this.startActivityForResult(i, EDIT_ACTION);

                /* TCP client network task */
                //Create initial instance so SendDataToNetwork doesn't throw an error.
                networktask = new NetworkTask();
                if (this.ipAddress.length() > 0 && this.portNumber >0)
                        networktask.execute(this.ipAddress, new Integer(this.portNumber).toString());
        }

        private void roombaForward(){
                networktask.SendDataToNetwork("w");
        }

        private void roombaBackward(){
                networktask.SendDataToNetwork("s");
        }

        private void roombaLeftSpin(){
                networktask.SendDataToNetwork("a");
        }

        private void roombaRightSpin(){
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

        private void roombaStop() {
                // TODO Auto-generated method stub

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
                // TODO Auto-generated method stub

        }
}
