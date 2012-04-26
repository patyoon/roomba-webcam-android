package ownz.johnpmayer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

public class Android_tcp_stream_testActivity extends Activity {
    
	ImageView drawingView;
	Socket senderSocket;
	GetVideoTask vt;
	
	static final int commandPort = 8001;
	static final int videoPort = 8002;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String ip = "158.130.104.5";
        
        // set up the drawing view
        drawingView = (ImageView) findViewById(R.id.surface);
        
        // Set up socket connection for commands to roomba
        SocketAddress addr1 = new InetSocketAddress(ip, commandPort);
        SenderSocketOpenerTask opener1 = new SenderSocketOpenerTask(addr1);
        opener1.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
        
        // Start up the video task
        vt = new GetVideoTask(new InetSocketAddress(ip, videoPort));
        vt.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
        
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
    		
    		DatagramPacket pack = new DatagramPacket(rowDataBuf, PAYLOAD);
    		
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

}