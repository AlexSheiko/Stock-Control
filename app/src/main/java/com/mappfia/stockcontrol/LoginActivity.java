package com.mappfia.stockcontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null) {
            setResult(RESULT_OK);
            finish();
        }

        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || enterPressed(event)) {
                    hideKeyboard();

                    String password = passwordField.getText().toString();
                    if (password.isEmpty()) {
                        passwordField.setError("Cannot be empty");
                        return true;
                    }
                    ParseUser.logInInBackground("admin", password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                passwordField.setError("Password is incorrect");
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }

    private boolean enterPressed(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_DOWN;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}
