package com.example.dan.castdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dan.castdemo.widgetList.ItemTouchHelperAdapter;
import com.example.dan.castdemo.widgetList.ItemTouchHelperViewHolder;
import com.example.dan.castdemo.widgetList.OnStartDragListener;
import com.example.dan.castdemo.widgets.CalendarWidget;
import com.example.dan.castdemo.widgets.ClockWidget;
import com.example.dan.castdemo.widgets.MapWidget;
import com.example.dan.castdemo.widgets.PlaceholderWidget;
import com.example.dan.castdemo.widgets.StocksWidget;
import com.example.dan.castdemo.widgets.UIWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;


public class WidgetListAdapter extends RecyclerView.Adapter<WidgetListAdapter.WidgetViewHolder> implements ItemTouchHelperAdapter {

    private final MainActivity mainActivity;
    private List<Widget> widgetList;
    private final OnStartDragListener mDragStartListener;

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Widget widget = widgetList.remove(fromPosition);
        widgetList.add(toPosition, widget);

        notifyItemMoved(fromPosition, toPosition);
        mainActivity.onItemMoved(getWidgetsOrder());
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public JSONObject getWidgetsOrder() {
        JSONObject widgetOrder = new JSONObject();
        int i = 0;
        try {
            for (Widget widget : widgetList) {
                    widget.position = i;
                    widget.save();
                    widgetOrder.put(String.valueOf(widget.id), widget.position);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return widgetOrder;
    }

    public class WidgetViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        protected TextView topHeader;
        protected TextView bottomHeader;
        protected ImageView typeIcon;
        protected View listItemView;
        protected ImageView handleView;


        public WidgetViewHolder(View view) {
            super(view);
            this.topHeader = (TextView) view.findViewById(R.id.widget_name);
            this.bottomHeader = (TextView) view.findViewById(R.id.widget_type);
            this.typeIcon = (ImageView) view.findViewById(R.id.widget_type_icon);
            handleView = (ImageView) itemView.findViewById(R.id.widget_handle);
            this.listItemView = view;
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }
    }


    public WidgetListAdapter(List<Widget> widgetList, MainActivity activity, OnStartDragListener dragStartListener) {
        this.widgetList = widgetList;
        this.mainActivity = activity;
        mDragStartListener = dragStartListener;

    }

    @Override
    public WidgetViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_row, viewGroup, false);

        return new WidgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WidgetViewHolder customViewHolder, int i) {
        final Widget widget = widgetList.get(i);
        Widget.types widgetType = widget.getWidgetType();
        UIWidget uiWidget;
        if (widgetType == Widget.types.CALENDAR) {
            uiWidget = new CalendarWidget(mainActivity, widget);
        } else if (widgetType == Widget.types.STOCKS) {
            uiWidget = new StocksWidget(mainActivity, widget);
        } else if (widgetType == Widget.types.MAP) {
            uiWidget = new MapWidget(mainActivity, widget);
        } else if (widgetType == Widget.types.CLOCK) {
            uiWidget = new ClockWidget(mainActivity, widget);
        } else {
            uiWidget = new PlaceholderWidget(mainActivity, widget);
        }

        customViewHolder.topHeader.setText(widget.getHumanName());
        customViewHolder.bottomHeader.setText(uiWidget.getWidgetPreviewSecondaryHeader());


        customViewHolder.typeIcon.setImageResource(widget.getIconResource());

        customViewHolder.listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity, WidgetSettingsActivity.class);
                intent.putExtra(Widget.ID, widget.id);

                mainActivity.startActivity(intent);
            }
        });

        customViewHolder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(customViewHolder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != widgetList ? widgetList.size() : 0);
    }
}