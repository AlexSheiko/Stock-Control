package com.mappfia.stockcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.text.NumberFormat;
import java.util.Locale;

public class StockAdapter extends ArrayAdapter<ParseObject> {

    public StockAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rootView = inflater.inflate(R.layout.list_item_stock, parent, false);
        } else {
            rootView = convertView;
        }

        ParseObject stock = getItem(position);

        TextView quantityLabel = (TextView) rootView.findViewById(R.id.quantity);
        NumberFormat formatter = NumberFormat.getInstance(Locale.UK);
        try {
            quantityLabel.setText(formatter.format(stock.getInt("quantity")));
        } catch (IllegalArgumentException e) {
            quantityLabel.setText("0");
        }

        TextView priceLabel = (TextView) rootView.findViewById(R.id.price);
        formatter = NumberFormat.getCurrencyInstance(Locale.UK);
        try {
        priceLabel.setText(formatter.format(stock.getNumber("price")));
        } catch (IllegalArgumentException e) {
            priceLabel.setText(formatter.format(0));
        }

        return rootView;
    }
}
