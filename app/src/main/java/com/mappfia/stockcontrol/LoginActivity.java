package com.mappfia.stockcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || enterPressed(event)) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    String password = passwordField.getText().toString();
                    if (password.isEmpty()) {
                        passwordField.setError("Cannot be empty");
                        return true;
                    }
                    if (password.toLowerCase().equals("grayrabbit")) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        passwordField.selectAll();
                        passwordField.setError("Password is incorrect");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private boolean enterPressed(KeyEvent event) {
        return event != null
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_DOWN;
    }
}
