package cis542.roomba.android;

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
        Intent resultIntent = new Intent();
        resultIntent.putExtra("ip", TextEntryActivity.this.ipEditText.getText().toString());
        resultIntent.putExtra("port", TextEntryActivity.this.portEditText.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}