package cis542.roomba.android;

import edu.upenn.cis542.MenuActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/* activity for initial IP and port number input */

public class TextEntryActivity extends Activity {
    private EditText ipEditText;
    private EditText portEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ip_entry);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        // title
        ipEditText = (EditText) findViewById(R.id.IPEditText);
        portEditText = (EditText) findViewById(R.id.portEditText);
        portEditText.setText(8001);
        portEditText.setText(8002);
    }
    
    public void submitButtonListener(View v) {
        executeDone();
	}

    @Override
    public void onBackPressed() {
        executeDone();
        super.onBackPressed();
    }

    private void executeDone() {
    	Intent resultIntent = new Intent(this.getApplicationContext(), MenuActivity.class);
    	Bundle bundle = new Bundle();
    	bundle.putString("ip", TextEntryActivity.this.ipEditText.getText().toString());
    	bundle.putString("portcom", TextEntryActivity.this.portEditText.getText().toString());
    	bundle.putString("portvid", TextEntryActivity.this.portEditText2.getText().toString());
        resultIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}