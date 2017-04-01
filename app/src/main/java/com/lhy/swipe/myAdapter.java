package com.lhy.swipe;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 创 建 人: 路好营
 * 创建日期: 2017/3/30 17:49
 * 添加备注:
 */
public class myAdapter extends BaseAdapter {
    private String[] names;
    private Context context;
    private List<SwipeLayout> swipeLayoutList;

    myAdapter(Context context, String[] names) {
        this.names = names;
        this.context = context;
        swipeLayoutList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = View.inflate(context, R.layout.item_swipe, null);
        } else {
            view = convertView;
        }
        SwipeLayout swipeLayout = (SwipeLayout) view;
        swipeLayout.setOnSwipeListener(new SwipeLayout.onSwipeListener() {
            @Override
            public void onClosed(SwipeLayout swipeLayout) {
                swipeLayoutList.remove(swipeLayout);
            }

            @Override
            public void onOpened(SwipeLayout swipeLayout) {
                swipeLayoutList.add(swipeLayout);
            }

            @Override
            public void onStartOpen(SwipeLayout swipeLayout) {
                for (SwipeLayout layout : swipeLayoutList) {
                    layout.closeSideContent(true);
                }
                swipeLayoutList.clear();
            }

            @Override
            public void onStartClose(SwipeLayout swipeLayout) {

            }
        });
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        tvName.setText(names[position]);
        return view;
    }
}
