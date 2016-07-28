package com.example.daxing.qualitytest;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapterSubscription extends BaseAdapter {

    private ArrayList listData;
    private LayoutInflater layoutInflater;
    public CustomAdapterSubscription(){}
    public CustomAdapterSubscription(Context context, ArrayList listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listitemforsubscription, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.img);
            holder.VideoTitleView = (TextView) convertView.findViewById(R.id.VideoTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SubListItem.Item newItem = (SubListItem.Item) listData.get(position);
        holder.VideoTitleView.setText(newItem.snippet.title);
        //holder.VideoDetailView.setText(newItem.contentDetails.newItemCount);
        if (holder.imageView != null) {
            new ImageDownloaderTask(holder.imageView).execute(newItem.snippet.thumbnails.medium.url);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView VideoTitleView;
        ImageView imageView;
    }
}