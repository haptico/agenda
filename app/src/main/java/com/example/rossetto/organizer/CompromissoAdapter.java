package com.example.rossetto.organizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by admin on 13/06/2016.
 */
public class CompromissoAdapter extends ArrayAdapter<CompromissoItem> {
    private LayoutInflater inflater;
    private List<CompromissoItem> objects;

    public CompromissoAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CompromissoAdapter(Context context, int resource, List<CompromissoItem> items) {
        super(context, resource, items);
        objects = items;
    }

    public int getCount() {
        if (objects != null) {
            return objects.size();
        }else{
            return 0;
        }
    }

    public CompromissoItem getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_item_compromisso, null);
        }

        CompromissoItem c = getItem(position);

        if (c != null) {
            TextView compromissoView = (TextView) v.findViewById(R.id.list_item_compromisso_textview);
            TextView dataView = (TextView) v.findViewById(R.id.list_item_data_textview);
            TextView horaView = (TextView) v.findViewById(R.id.list_item_hora_textview);
            TextView localView = (TextView) v.findViewById(R.id.list_item_local_textview);

            if (compromissoView != null) {
                compromissoView.setText(c.getCompromisso());
            }

            if (dataView != null) {
                dataView.setText(c.getData());
            }

            if (horaView != null) {
                horaView.setText(c.getHora());
            }

            if (localView != null) {
                localView.setText(c.getLocal());
            }
        }

        return v;

    }
}
