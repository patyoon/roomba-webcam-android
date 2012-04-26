package ownz.johnpmayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/* activity for initial IP and port number input */

public class TextEntryActivity extends Activity {
    public static final int ACTIVITY_Android_tcp_stream_testActivity =1;
	private EditText ipEditText;
    private EditText portEditText;
    private EditText portEditText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ip_entry);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        // title
        ipEditText = (EditText) findViewById(R.id.IPEditText);
        portEditText = (EditText) findViewById(R.id.portEditText);
        portEditText.setText("8001");
        portEditText2 = (EditText) findViewById(R.id.portEditText2);
        portEditText2.setText("8002");
    }
    
    public void submitButtonListener(View v) {
        executeDone();
	}

    @Override
    public void onBackPressed() {
        executeDone();
    }

    private void executeDone() {
    	Intent resultIntent = new Intent(this.getApplicationContext(), Android_tcp_stream_testActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putString("ip", TextEntryActivity.this.ipEditText.getText().toString());
    	bundle.putString("portcom", TextEntryActivity.this.portEditText.getText().toString());
    	bundle.putString("portvid", TextEntryActivity.this.portEditText2.getText().toString());
        resultIntent.putExtras(bundle);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(resultIntent, TextEntryActivity.ACTIVITY_Android_tcp_stream_testActivity);
    }
}