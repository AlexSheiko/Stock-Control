package com.mappfia.stockcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseObject;

public class StockEditAdapter extends ArrayAdapter<ParseObject> {

    public StockEditAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.list_item_stock_edit, parent, false);

        ParseObject stock = getItem(position);
        ((TextView) rootView.findViewById(R.id.positionLabel)).setText((position + 1) + ")");

        EditText quantityField = (EditText) rootView.findViewById(R.id.quantityField);
        quantityField.setText(stock.getInt("quantity") + "");

        EditText priceField = (EditText) rootView.findViewById(R.id.priceField);
        priceField.setText(stock.getNumber("price") + "");

        return rootView;
    }
}
