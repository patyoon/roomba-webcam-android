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
    
	SendCommandTask sender;
	Socket senderSocket;
	Socket videoSocket;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sender = new SendCommandTask();
        /*
        SocketAddress addr1 = new InetSocketAddress("158.130.107.65", 8001);
        SocketOpenerTask1 opener1 = new SocketOpenerTask1(addr1);
        opener1.execute();
        */
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
    		return null;
    	}
    	
    }

    public void leftButtonListener(View v) {
    	sender.execute('a');
    }
    
    public void upButtonListener(View v) {
    	sender.execute('w');
    }
    
    public void downButtonListener(View v) {
    	
    }
    
    public void rightButtonListener(View v) {
    	
    }
    
    public void pauseButtonListener(View v) {
    	sender.execute('p');
    }
    
    public void stopButtonListener(View v) {
    	
    }
    
    public void quitButtonListener(View v) {
    	
	}

    public class SendCommandTask extends AsyncTask<Character, Void, Boolean> {

    	OutputStream nos;
    	
    	@Override
    	protected Boolean doInBackground(Character... params) {
    		try {
    			if (nos == null) {
    				nos = senderSocket.getOutputStream();
    			}
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
    
    public class GetVideoTask extends AsyncTask<Void, Void, Void> {
    	
    	InputStream nis;
    	
    	@Override
    	protected Void doInBackground(Void... params) {
    		try {
    			if (nis == null) {
    				nis = videoSocket.getInputStream();
    			}
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return null;
    		}
    		
    		try {
    			nis.read();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return null;
    		}
    		
    		return null;
    		
    	}
    	
    }

}