package com.mappfia.stockcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private StockAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText quantityField = (EditText) findViewById(R.id.quantityField);
        final EditText priceField = (EditText) findViewById(R.id.priceField);
        priceField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        enterPressed(event)) {
                    String quantity = quantityField.getText().toString();
                    String price = priceField.getText().toString();
                    if (price.isEmpty()) {
                        priceField.setError("Cannot be empty");
                        return true;
                    } else if (quantity.isEmpty()) {
                        quantityField.setError("Cannot be empty");
                        return true;
                    }
                    saveEntry(Integer.parseInt(quantity), Integer.parseInt(price));
                    hideKeyboard();
                    priceField.setText("");
                    quantityField.setText("");
                    return true;
                }
                return false;
            }
        });

        ListView stockList = (ListView) findViewById(R.id.stockList);
        mAdapter = new StockAdapter(this);
        stockList.setAdapter(mAdapter);
        populateList();
    }

    private boolean enterPressed(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_DOWN;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void saveEntry(int quantity, int price) {
        ParseObject stock = new ParseObject("Stock");
        stock.put("quantity", quantity);
        stock.put("price", price);
        stock.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    populateList();
                } else {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateList() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stock");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> stocks, ParseException e) {
                if (e == null) {
                    Collections.reverse(stocks);
                    mAdapter.clear();
                    mAdapter.addAll(stocks);
                } else {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
