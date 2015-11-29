package com.mappfia.stockcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

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
        quantityLabel.setText(String.valueOf(stock.getInt("quantity")));

        TextView priceLabel = (TextView) rootView.findViewById(R.id.price);
        priceLabel.setText(String.valueOf(stock.getInt("price")));

        return rootView;
    }
}
