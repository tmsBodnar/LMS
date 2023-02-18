package com.kalandlabor.ledmessengerstrip.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupMenu;

import com.kalandlabor.ledmessengerstrip.MainActivity;
import com.kalandlabor.ledmessengerstrip.R;

import java.util.List;

/**
 * Custom adapter for Buttonlist, with OnClickListener
 * and OnLongClickListener for removing Button
 */
public class CustomGridAdapter extends BaseAdapter {

    MainActivity mainActivity;
    List<Button> items;
    LayoutInflater inflater;

    public CustomGridAdapter(MainActivity mainActivity, List<Button> items) {
        this.mainActivity = mainActivity;
        this.items = items;
        inflater = (LayoutInflater) this.mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cell, null);
        }
        final Button button = convertView.findViewById(R.id.grid_item);
        button.setText(items.get(position).getText());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.itemClicked(position);
            }
        });
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mainActivity,v);
                popupMenu.inflate(R.menu.context_menu);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if  (mainActivity.removeItem(button)){
                            notifyDataSetChanged();
                        }
                        return true;
                    }
                });
                return true;
            }
        });
        return convertView;
    }


}
