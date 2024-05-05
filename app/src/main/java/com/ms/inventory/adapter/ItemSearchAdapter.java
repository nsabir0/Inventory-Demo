package com.ms.inventory.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ms.inventory.model.ItemSearch;

import java.util.List;

public class ItemSearchAdapter extends BaseAdapter {

	List<ItemSearch> list;
	Context context;
	int resource;

	public ItemSearchAdapter(Context context, List<ItemSearch> list) {
		this.context = context;
		this.list = list;
		//this.resource = R.layout.row_item_search_view;
		this.resource = android.R.layout.simple_list_item_2;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;

		ViewHolder holder = null;

		if (view == null) {
			view = View.inflate(context, resource, null);
			holder = new ViewHolder(view);
			view.setTag(holder);

		} else {

			holder = (ViewHolder) view.getTag();

		}

		ItemSearch item = list.get(position);

		holder.textView1.setText(item.name);

		if (!item.itemCode.isEmpty())
			holder.textView2.setText("Barcode: "+item.barcode);

		return view;
	}


	class ViewHolder {
		TextView textView1, textView2;

		ViewHolder(View v) {
			textView1 = (TextView) v.findViewById(android.R.id.text1);
			textView2 = (TextView) v.findViewById(android.R.id.text2);
		}
	}
}
