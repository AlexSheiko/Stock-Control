package com.mappfia.stockcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Collections;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private ListView mListView;
    private StockEditAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mAdapter = new StockEditAdapter(this);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mListView.scrollListBy(mListView.getCount() - 1);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stock");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> quotes, ParseException e) {
                Collections.reverse(quotes);
                mAdapter.addAll(quotes);
            }
        });
    }

    public void onClickSave(View view) {
        ParseObject.unpinAllInBackground();

        Log.d("mine", mListView.getChildCount()+"");
        for (int i = 0; i < mListView.getChildCount(); i++) {
            EditText quantityField = (EditText) mListView.findViewWithTag("quantity" + i);
            EditText priceField = (EditText) mListView.findViewWithTag("price" + i);
                String quantity = quantityField.getText().toString();
                String price = priceField.getText().toString();

                ParseObject stock = new ParseObject("Stock");
                if (!quantity.isEmpty()) {
                    stock.put("quantity", Integer.parseInt(quantity));
                }
                if (!price.isEmpty()) {
                    stock.put("price", Float.parseFloat(price));
                }
                final int finalI = i;
                stock.pinInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (finalI == (mListView.getChildCount() - 1)) {
                            Toast.makeText(EditActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                });
        }
    }
}
