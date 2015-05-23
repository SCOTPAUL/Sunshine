package uk.co.paulcowie.sunshine.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import uk.co.paulcowie.sunshine.R;

/**
 * Created by paul on 23/05/15.
 */
public class ForecastListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public ForecastListAdapter(Context context, List<String> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item_forecast, viewGroup, false);
        TextView dateView = (TextView) rowView.findViewById(R.id.left_icon);
        TextView textView = (TextView) rowView.findViewById(R.id.text_box);

        dateView.setText(getSideDateFromString(values.get(i)));
        textView.setText(values.get(i));

        return rowView;
    }

    private String getSideDateFromString(String weatherInfo) {
        return weatherInfo.split(" - ")[0].split(" ")[2];
    }
}
