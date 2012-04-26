package ownz.johnpmayer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

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
	static final int videoPort = 8003;
	
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
    	DataInputStream dis;
    	SocketAddress remote;
    	
    	public GetVideoTask(SocketAddress remote) {
			this.remote = remote;
		}
    	
		@Override
    	protected Void doInBackground(Void... params) {
			
			Bitmap mBitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.RGB_565);
    		
    		try {
    			videoSocket = new Socket();
    			videoSocket.connect(remote);
    			nis = videoSocket.getInputStream();
    			dis = new DataInputStream(nis);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return null;
    		}
    		
    		
    		
			byte[] rowDataBuf = new byte[640*3];
    		try {
    			while(videoSocket.isConnected()) {
    				
    				int rowId = dis.readInt();
    					
    				nis.read(rowDataBuf, 0, 640*3);
    				
    	    		if (rowId >= mBitmap.getHeight() || rowId < 0) {
    	    			Log.v("Got a bad rowId", Integer.toString(rowId));
    	    			continue;
    	    		}
    	    		
    	    		for (int col = 0; col < 320; col += 1) {
    	    			
    	    			int r, g, b;
    	    			r = (int)rowDataBuf[3*col] & 0xff;
    	    			g = (int)rowDataBuf[3*col+1] & 0xff;
    	    			b = (int)rowDataBuf[3*col+2] & 0xff;
    	    			
    	    			/*
    	    			Log.v("R",Integer.toString(r));
    	    			Log.v("G",Integer.toString(g));
    	    			Log.v("B",Integer.toString(b));
    	    			*/
    	    			
    	    			int color = Color.argb(1,r,g,b);
    	    			
    	    			mBitmap.setPixel(col, rowId, color);
    	    			
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
    		
    		//Log.v("progress", "updating for row");
    		
    		Bitmap b = frames[0];
    		drawingView.setImageBitmap(b);
    		
    	}
    	
    }

}