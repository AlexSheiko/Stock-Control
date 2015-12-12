package com.mappfia.stockcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private LinearLayout mContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mContainerView = (LinearLayout) findViewById(R.id.listContainer);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Stock");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> stocks, ParseException e) {
                Collections.reverse(stocks);
                for (int i = 0; i < stocks.size(); i++) {
                    ParseObject stock = stocks.get(i);
                    mContainerView.addView(getStockView(stock, i));
                }
            }
        });
    }

    private View getStockView(ParseObject stock, int position) {
        LayoutInflater inflater = getLayoutInflater();
        View rootView = inflater.inflate(R.layout.list_item_stock_edit, null);

        ((TextView) rootView.findViewById(R.id.positionLabel)).setText((position + 1) + ")");

        EditText quantityField = (EditText) rootView.findViewById(R.id.quantityField);
        quantityField.setText(stock.getInt("quantity") + "");
        quantityField.setTag("quantity" + position);

        EditText priceField = (EditText) rootView.findViewById(R.id.priceField);
        priceField.setText(stock.getNumber("price").toString());
        priceField.setTag("price" + position);

        return rootView;
    }

    public void onClickSave(View view) {
        ParseObject.unpinAllInBackground();

        List<ParseObject> stocks = new ArrayList<>();
        for (int i = 0; i < mContainerView.getChildCount(); i++) {
            EditText quantityField = (EditText) mContainerView.findViewWithTag("quantity" + i);
            EditText priceField = (EditText) mContainerView.findViewWithTag("price" + i);
            String quantity = quantityField.getText().toString();
            String price = priceField.getText().toString();

            ParseObject stock = new ParseObject("Stock");
            if (!quantity.isEmpty()) {
                stock.put("quantity", Integer.parseInt(quantity));
            }
            if (!price.isEmpty()) {
                stock.put("price", Float.parseFloat(price));
            }
            stocks.add(stock);
        }
        Collections.reverse(stocks);
        ParseObject.pinAllInBackground(stocks, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(EditActivity.this, "Saved successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
