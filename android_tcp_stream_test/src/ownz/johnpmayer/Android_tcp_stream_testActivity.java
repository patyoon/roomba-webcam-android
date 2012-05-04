package ownz.johnpmayer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class Android_tcp_stream_testActivity extends Activity implements SensorEventListener 
{
	private SensorManager mSensorManager;
	private WindowManager mWindowManager;
	private Sensor mAccelerometer;


	private Display mDisplay;
	private boolean mInitialized;	

	ImageView drawingView;
	Socket senderSocket;
	GetVideoTask vt;

	static  int commandPort = 8001;
	static  int videoPort = 8002;
	
	//x, y, z value for sensor
	float mLastX, mLastY, mLastZ; 

	private final float NOISE = (float) 2.0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Bundle bundle = this.getIntent().getExtras();
		String ip =bundle.getString("ip");
		commandPort = Integer.parseInt(bundle.getString("portcom"));
		videoPort = Integer.parseInt(bundle.getString("portvid"));

		Log.d("commandPort", Integer.toString(commandPort));
		Log.d("videoPort", Integer.toString(videoPort));
		Log.d("ip", ip);
		//String ip = "192.168.1.3";

		// set up the drawing view
		drawingView = (ImageView) findViewById(R.id.surface);

		// Set up socket connection for commands to roomba
		SocketAddress addr1 = new InetSocketAddress(ip, commandPort);
		SenderSocketOpenerTask opener1 = new SenderSocketOpenerTask(addr1);
		opener1.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);

		// Start up the video task
		vt = new GetVideoTask(new InetSocketAddress(ip, videoPort));
		vt.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);


		//Configure Accelerometer
		mSensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		// add listener. The listener will be HelloAndroid (this) class
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this,
				mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

		/*	More sensor speeds (taken from api docs)
		    SENSOR_DELAY_FASTEST get sensor data as fast as possible
		    SENSOR_DELAY_GAME	rate suitable for games
		 	SENSOR_DELAY_NORMAL	rate (default) suitable for screen orientation changes
		 */
		// Get an instance of the WindowManager
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mDisplay = mWindowManager.getDefaultDisplay();
		mInitialized = false;
	}

	public class SenderSocketOpenerTask extends AsyncTask<Void, Void, Void> {

		SocketAddress remote;

		SenderSocketOpenerTask(SocketAddress remote) {
			super();
			this.remote = remote;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Socket s = new Socket();
			try {
				s.connect(remote);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			senderSocket = s;
			return null;
		}

	}

	public void leftButtonListener(View v) {
		(new SendCommandTask()).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR,'a');
	}

	public void upButtonListener(View v) {
		(new SendCommandTask()).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR,'w');
	}

	public void downButtonListener(View v) {
		(new SendCommandTask()).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR,'s');
	}

	public void rightButtonListener(View v) {
		(new SendCommandTask()).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR,'d');
	}

	public void pauseButtonListener(View v) {
		//(new SendCommandTask()).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR,'p');
	}

	public void stopButtonListener(View v) {
		(new SendCommandTask()).executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR,'p');
	}

	public void quitButtonListener(View v) {
		// ToDo
		finish();
	}

	public class SendCommandTask extends AsyncTask<Character, Void, Boolean> {

		OutputStream nos;

		@Override
		protected Boolean doInBackground(Character... params) {


			if (senderSocket == null) {
				Log.v("send command do in back","null sender");
				return false;
			}

			try {
				nos = senderSocket.getOutputStream();	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

			if (params.length != 1) {
				return false;
			}

			char command = params[0].charValue();
			byte[] buf = new byte[]{(byte)command};

			try {
				nos.write(buf);
				Log.v("send-command","" + command);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

			return true;

		}

	}

	public class GetVideoTask extends AsyncTask<Void, Bitmap, Void> {

		Socket videoSocket;
		InputStream nis;
		OutputStream nos;
		DataInputStream dis;
		SocketAddress remote;
		//InetAddress me;
		//DatagramSocket udpStreamSocket;

		public GetVideoTask(SocketAddress remote) {
			this.remote = remote;
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				videoSocket = new Socket();
				videoSocket.connect(remote);
				nis = videoSocket.getInputStream();

				nos = videoSocket.getOutputStream();
				//dis = new DataInputStream(nis);
				/*
    			//InetAddress me;

    			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
    	            NetworkInterface intf = en.nextElement();
    	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
    	                InetAddress inetAddress = enumIpAddr.nextElement();
    	                if (!inetAddress.isLoopbackAddress()) {
    	                    me = inetAddress;
    	                }
    	            }
    	        }

    			udpStreamSocket = new DatagramSocket(9999);


    			String host = me.getHostAddress().toString();

    			Log.v("in background my ip", host);

    			nos.write(host.getBytes(), 0, host.length());
				 */

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			int chunk = 8;
			int W = 640 / chunk;
			int H = 480 / chunk;
			int PAYLOAD = 3*W;

			Bitmap mBitmap = Bitmap.createBitmap(W, H, Bitmap.Config.RGB_565);
			//Buffer mBuffer = ByteBuffer.allocate(PAYLOAD);
			//byte[] mByteArray = (byte[]) mBuffer.array();

			//byte[] dataBuf = new byte[W*3];
			//byte[] rowIdBuf = new byte[1];

			byte[] rowDataBuf = new byte[PAYLOAD];

			//DatagramPacket pack = new DatagramPacket(rowDataBuf, PAYLOAD);

			try {
				while(videoSocket.isConnected()) {


					byte[] buf = new byte[1];
					buf[0] = 'z';
					nos.write(buf);


					//udpStreamSocket.receive(pack);

					/*
    				nis.read(rowIdBuf, 0, 1);
    				int rowId = (int)(rowIdBuf[0] & 0xFF);
					 */

					//int size = 
					//mBitmap.copyPixelsFromBuffer(mBuffer);

					//

					/*
    	    		if (rowId >= H || rowId < 0) {
    	    			Log.v("Got a bad rowId", Integer.toString(rowId));
    	    			continue;
    	    		}
					 */

					for (int row = 0; row < H; row += 1) {

						int size = nis.read(rowDataBuf, 0, PAYLOAD);
						Log.v("vid background","got row " + Integer.toString(row) 
								+ " frame data: " + Integer.toString(size));

						for (int col = 0; col < W; col += 1) {

							int r, g, b;
							r = (int)rowDataBuf[3*col] & 0xff;
							g = (int)rowDataBuf[3*col+1] & 0xff;
							b = (int)rowDataBuf[3*col+2] & 0xff;

							int color = Color.argb(1,r,g,b);

							mBitmap.setPixel(col, row, color);
						}
					}

					this.publishProgress(mBitmap);
				}
				nis.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			return null;

		}

		@Override
		protected void onProgressUpdate(Bitmap... frames) {

			Log.v("progress", "updating for row");

			Bitmap b = frames[0];
			drawingView.setImageBitmap(b);

		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// check sensor type
		float mSensorX = 0, mSensorY = 0;
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);
			if (deltaX < NOISE) deltaX = (float)0.0;
			if (deltaY < NOISE) deltaY = (float)0.0;
			if (deltaZ < NOISE) deltaZ = (float)0.0;
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			
			if (deltaX > deltaY) {
				//move left right
			} else if (deltaY > deltaX) {
				// move up down
			} else {
			}
		}

		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			switch (mDisplay.getRotation()) {
			case Surface.ROTATION_0:
				Log.d("onSensorChanged", "ROTATION_0");
				mSensorX = event.values[0];
				mSensorY = event.values[1];
				break;
			case Surface.ROTATION_90:
				Log.d("onSensorChanged", "ROTATION_90");
				mSensorX = -event.values[1];
				mSensorY = event.values[0];
				break;
			case Surface.ROTATION_180:
				Log.d("onSensorChanged", "ROTATION_180");
				mSensorX = -event.values[0];
				mSensorY = -event.values[1];
				break;
			case Surface.ROTATION_270:
				Log.d("onSensorChanged", "ROTATION_270");
				mSensorX = event.values[1];
				mSensorY = -event.values[0];
				break;
			}
		}
		Log.d("mSensorX", Float.toString(mSensorX));
		Log.d("mSensorY", Float.toString(mSensorY));
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onStop() {
		//is called when an activity is no longer visible to, or interacting with, the user
		super.onStop();
		mSensorManager.unregisterListener(this);
	}


}