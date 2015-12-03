package com.mappfia.stockcontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EDIT = 101;
    private static final int REQUEST_CODE_EXPORT = 102;

    final static private String APP_KEY = "60l8q30so9udvpj";
    final static private String APP_SECRET = "1tzhvf1qxmu04tc";
    private DropboxAPI<AndroidAuthSession> mDBApi;

    private StockAdapter mAdapter;
    private SharedPreferences mPrefs;

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
                    saveEntry(Integer.parseInt(quantity), Float.parseFloat(price));
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
        stockList.setEmptyView(findViewById(android.R.id.empty));
        populateList();


        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private boolean enterPressed(KeyEvent event) {
        return event != null
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_DOWN;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void saveEntry(int quantity, float price) {
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
                if (stocks.size() > 0) {
                    Collections.reverse(stocks);
                    mAdapter.clear();
                    mAdapter.addAll(stocks);

                    findViewById(R.id.editButton).setEnabled(true);
                    findViewById(R.id.exportButton).setEnabled(true);
                }
            }
        });
    }

    public void export(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_EXPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_EDIT) {
                // todo add edit activity
            } else if (requestCode == REQUEST_CODE_EXPORT) {
                shareDropbox();
            }
        }
    }

    private void shareDropbox() {
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);

        String token = mPrefs.getString("token", null);
        if (token != null) {
            AndroidAuthSession session = new AndroidAuthSession(appKeys, token);
            mDBApi = new DropboxAPI<>(session);
            new ExportQuotesTask().execute();
        } else {
            AndroidAuthSession session = new AndroidAuthSession(appKeys);
            mDBApi = new DropboxAPI<>(session);
            mDBApi.getSession().startOAuth2Authentication(this);
        }
    }

    protected void onResume() {
        super.onResume();

        if (mDBApi != null && mDBApi.getSession() != null && mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                mPrefs.edit().putString("token", accessToken).apply();
                new ExportQuotesTask().execute();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    private File getStockFile() {
        File file = new File(getFilesDir() + "/Stocks.csv");
        return file;
    }

    private class ExportQuotesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Uploading file...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                createStockFile();
                File file = getStockFile();
                FileInputStream inputStream = new FileInputStream(file);
                DropboxAPI.Entry response = mDBApi.putFile("/Stocks.csv", inputStream,
                        file.length(), null, null);
                Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "Successfully uploaded", Toast.LENGTH_LONG).show();
        }
    }

    private void createStockFile() throws Exception {
        String csv = "";

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stock");
        query.fromLocalDatastore();
        List<ParseObject> stocks = query.find();
        for (ParseObject stock : stocks) {
            csv += stock.getInt("quantity") + "," + stock.getNumber("price") + "\n";
        }

        FileOutputStream outputStream;

        outputStream = openFileOutput("Stocks.csv", Context.MODE_PRIVATE);
        outputStream.write(csv.getBytes());
        outputStream.close();
    }
}
