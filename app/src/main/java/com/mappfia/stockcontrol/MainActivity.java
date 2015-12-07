package com.mappfia.stockcontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOGIN = 101;
    private static final int REQUEST_CODE_EDIT = 102;

    final static private String APP_KEY = "kt9dk46uf0uxcov";
    final static private String APP_SECRET = "jxtjwoxq3mc7rtw";
    private DropboxAPI<AndroidAuthSession> mDBApi;

    private StockAdapter mAdapter;
    private SharedPreferences mPrefs;
    private boolean mUpload;

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
                        priceField.requestFocus();
                        priceField.setError("Cannot be empty");
                        return true;
                    } else if (quantity.isEmpty()) {
                        quantityField.requestFocus();
                        quantityField.setError("Cannot be empty");
                        return true;
                    }
                    saveEntry(Integer.parseInt(quantity), Float.parseFloat(price));
                    priceField.setText("");
                    quantityField.setText("");
                    quantityField.requestFocus();
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


        ((TextView) findViewById(R.id.idLabel)).setText("Device ID: " + getDeviceId());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private boolean enterPressed(KeyEvent event) {
        return event != null
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() == KeyEvent.ACTION_DOWN;
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

    public void onClickExport(View view) {
        shareDropbox();
    }

    public void onClickEdit(View view) {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_LOGIN) {
                Intent editIntent = new Intent(this, EditActivity.class);
                startActivityForResult(editIntent, REQUEST_CODE_EDIT);
            } else if (requestCode == REQUEST_CODE_EDIT) {
                populateList();
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
            mUpload = true;
            mDBApi.getSession().startOAuth2Authentication(this);
        }
    }

    protected void onResume() {
        super.onResume();

        if (mUpload && mDBApi != null && mDBApi.getSession() != null && mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
                mPrefs.edit().putString("token", accessToken).apply();
                new ExportQuotesTask().execute();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
            mUpload = false;
        }
    }

    private File getStockFile() {
        File file = new File(getFilesDir() + "/Stocks.csv");
        return file;
    }

    public String getDeviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).substring(0,6);
    }

    public String getTimeStamp() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy-kk:mm");
        return formatter.format(date);
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
                DropboxAPI.Entry response = mDBApi.putFile("/Stocks-" + getDeviceId() + "-" + getTimeStamp() + ".csv", inputStream,
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
