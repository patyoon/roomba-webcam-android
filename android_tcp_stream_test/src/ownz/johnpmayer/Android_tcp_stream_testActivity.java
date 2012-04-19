package ownz.johnpmayer;

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
	Socket videoSocket;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SocketAddress addr1 = new InetSocketAddress("158.130.107.65", 8001);
        SocketOpenerTask1 opener1 = new SocketOpenerTask1(addr1);
        opener1.execute();
        
        SocketAddress addr2 = new InetSocketAddress("158.130.107.65", 8002);
        SocketOpenerTask2 opener2 = new SocketOpenerTask2(addr2);
        opener2.execute();
        
    }
    
    public class SocketOpenerTask1 extends AsyncTask<Void, Void, Void> {
    	
    	SocketAddress remote;
    	
    	SocketOpenerTask1(SocketAddress remote) {
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
    
    public class SocketOpenerTask2 extends AsyncTask<Void, Void, Void> {
    	
    	SocketAddress remote;
    	
    	SocketOpenerTask2(SocketAddress remote) {
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
    		videoSocket = s;
    		videoSocket.notifyAll();
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
    	
    	InputStream nis;
    	
    	@Override
    	protected Void doInBackground(Void... params) {
    		try {
    			videoSocket.wait();
    			nis = videoSocket.getInputStream();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return null;
    		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		byte[] rowIdBuf = new byte[4];
			byte[] rowDataBuf = new byte[640*3];
    		try {
    			while(videoSocket.isConnected()) {
    				nis.read(rowIdBuf, 0, 4);
    				nis.read(rowDataBuf, 0, 640*3);
    				VideoFrame vf = new VideoFrame();
    				vf.row = Integer.parseInt(new String(rowIdBuf)); // big ToDo
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