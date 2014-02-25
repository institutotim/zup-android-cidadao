package br.com.ntxdev.zup.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import br.com.ntxdev.zup.R;
import br.com.ntxdev.zup.util.FontUtils;
import br.com.ntxdev.zup.util.GeoUtils;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
	private List<Address> resultList;

	public AutoCompleteAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	@Override
	public int getCount() {
        if (resultList == null) return 0;
		return resultList.size();
	}

	@Override
	public String getItem(int index) {
        if (index > resultList.size() - 1) return "";

		return resultList.get(index).getAddressLine(0);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (position > resultList.size() - 1) return new View(getContext());

		View view = LayoutInflater.from(getContext()).inflate(R.layout.autocomplete_list_item, parent, false);
		TextView text = (TextView) view.findViewById(R.id.text);
		text.setText(resultList.get(position).getAddressLine(0));
		text.setTypeface(FontUtils.getRegular(getContext()));
		return view;
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					resultList = autocomplete(constraint.toString());

					filterResults.values = resultList;
					filterResults.count = resultList.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
		return filter;
	}

	private List<Address> autocomplete(String input) {
		try {
			return new Geocoder(getContext()).getFromLocationName(input, 4);
		} catch (Exception e) {
            Log.w("ZUP", e.getMessage());
            return new ArrayList<Address>();
		}
	}
}

