package com.example.dan.castdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.dan.castdemo.widgets.CalendarWidget;
import com.example.dan.castdemo.widgets.MapWidget;
import com.example.dan.castdemo.widgets.PlaceholderWidget;
import com.example.dan.castdemo.widgets.StocksWidget;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListenerAdapter;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WidgetList extends Fragment {

    @Bind(R.id.widgetList)
    RecyclerView widgetList;

    public WidgetList() {
        refreshList(); //@todo is this double since it's called in onResume()?
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.widget_list, container, false);
        ButterKnife.bind(this, view);

        widgetList.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    @OnClick(R.id.fab)
    public void addWidget() {
        final MainActivity activity = (MainActivity) getActivity();

        new MaterialDialog.Builder(getContext())
                .title("Widget Type")
                .items(new String[]{CalendarWidget.HUMAN_NAME, StocksWidget.HUMAN_NAME, MapWidget.HUMAN_NAME, PlaceholderWidget.HUMAN_NAME})
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Widget widget = new Widget();
                        widget.setType(Widget.types.valueOf(text.toString().toUpperCase()));
                        widget.save();

                        widget.initOptions();
                        refreshList();

                        activity.sendMessage(widget.getHumanName() + " widget created.");
                        return true;
                    }
                })
                .positiveText("Add")
                .show();
    }

    @Override
    public void onResume() {
        refreshList();
        super.onResume();
    }

    public void refreshList() {
        //@todo implement helper method
        // async fetch all saved widgets
        TransactionManager.getInstance().addTransaction(
                new SelectListTransaction<>(new Select().from(Widget.class),
                        new TransactionListenerAdapter<List<Widget>>() {
                            @Override
                            public void onResultReceived(List<Widget> someObjectList) {
                                WidgetListAdapter adapter = new WidgetListAdapter(someObjectList, (MainActivity) getActivity());
                                widgetList.setAdapter(adapter);
                            }
                        }));

    }
}