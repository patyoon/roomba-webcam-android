package ownz.johnpmayer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class Android_tcp_stream_testActivity extends Activity {
    
	Socket senderSocket;
	GetVideoTask vt;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        SocketAddress addr1 = new InetSocketAddress("158.130.104.218", 8001);
        SenderSocketOpenerTask opener1 = new SenderSocketOpenerTask(addr1);
        opener1.execute();
        
        
        vt = new GetVideoTask(
        new InetSocketAddress("158.130.104.218", 8002));
        //vt.execute();
        
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
    	(new SendCommandTask()).execute('a');
    }
    
    public void upButtonListener(View v) {
    	(new SendCommandTask()).execute('w');
    }
    
    public void downButtonListener(View v) {
    	(new SendCommandTask()).execute('s');
    }
    
    public void rightButtonListener(View v) {
    	(new SendCommandTask()).execute('d');
    }
    
    public void pauseButtonListener(View v) {
    	//(new SendCommandTask()).execute('p');
    }
    
    public void stopButtonListener(View v) {
    	(new SendCommandTask()).execute('p');
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
    
    public class VideoFrame {
    	
    	public int row;
    	public byte[] payload;
    	
    }
    
    public class GetVideoTask extends AsyncTask<Void, VideoFrame, Void> {
    	
    	Socket videoSocket;
    	InputStream nis;
    	DataInputStream dis;
    	SocketAddress remote;
    	
    	public GetVideoTask(SocketAddress remote) {
			this.remote = remote;
		}

		@Override
    	protected Void doInBackground(Void... params) {
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
    		
    		//byte[] rowIdBuf = new byte[4];
			byte[] rowDataBuf = new byte[640*3];
    		try {
    			while(videoSocket.isConnected()) {
    				boolean skip = false;
    				
    				//nis.read(rowIdBuf, 0, 4);
    				
    				int rowId = dis.readInt();
    				
    						
    				/*
    				for (int i = 0; i < 4; i++) {
    					if(rowIdBuf[i] < (byte)'0' || rowIdBuf[i] > (byte)'9') {
    						rowIdBuf[i] = (byte)'\0';
    					}
    				}
    				*/
    						
    				nis.read(rowDataBuf, 0, 640*3);
    				
    				//String rowIdString = new String(rowIdBuf);
    				
    				VideoFrame vf = new VideoFrame();
    				vf.row = rowId;//Integer.parseInt(rowIdString); // big ToDo
    				vf.payload = rowDataBuf.clone();
    				
    				this.publishProgress(vf);
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
    	protected void onProgressUpdate(VideoFrame... frames) {
    		Log.v("video", "would update canvas here");
    	}
    	
    }

}