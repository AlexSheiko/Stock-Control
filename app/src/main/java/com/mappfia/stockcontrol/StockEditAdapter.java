package com.mappfia.stockcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.parse.ParseObject;

import java.text.NumberFormat;
import java.util.Locale;

public class StockEditAdapter extends ArrayAdapter<ParseObject> {

    public StockEditAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.list_item_stock_edit, parent, false);

        ParseObject stock = getItem(position);

        EditText quantityField = (EditText) rootView.findViewById(R.id.quantityField);
        NumberFormat formatter = NumberFormat.getInstance(Locale.UK);
        quantityField.setText(formatter.format(stock.getInt("quantity")));

        EditText priceField = (EditText) rootView.findViewById(R.id.priceField);
        formatter = NumberFormat.getCurrencyInstance(Locale.UK);
        priceField.setText(formatter.format(stock.getNumber("price")));

        return rootView;
    }
}
