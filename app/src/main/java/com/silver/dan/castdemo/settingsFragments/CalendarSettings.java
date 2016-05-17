package com.silver.dan.castdemo.settingsFragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;
import com.silver.dan.castdemo.CalendarInfo;
import com.silver.dan.castdemo.R;
import com.silver.dan.castdemo.Widget;
import com.silver.dan.castdemo.WidgetOption;
import com.silver.dan.castdemo.widgets.CalendarWidget;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CalendarSettings extends WidgetSettingsFragment {

    public static String ALL_CALENDARS = "ALL_CALENDARS";
    public static String CALENDAR_ENABLED = "CALENDAR_ENABLED";
    public static String SHOW_EVENT_LOCATIONS = "SHOW_EVENT_LOCATIONS";
    public static String SHOW_EVENTS_UNTIL = "SHOW_EVENTS_UNTIL";

    Integer numDaysDisplayValues[] = new Integer[]{3, 7, 14, 30, 90};

    WidgetOption optionAllCalendars;
    WidgetOption optionShowEventLocations;
    WidgetOption optionShowEventsUntil;

    @Bind(R.id.display_all_calendars)
    Switch allCalendars;

    @Bind(R.id.calendar_list)
    RecyclerView calendarList;

    @Bind(R.id.display_event_locations)
    Switch eventLocations;

    @Bind(R.id.show_events_until)
    TwoLineSettingItem showEventsUntil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.calendar_settings, container, false);
        ButterKnife.bind(this, view);

        // restore saved options into GUI

        optionAllCalendars = loadOrInitOption(CalendarSettings.ALL_CALENDARS);
        optionShowEventLocations = loadOrInitOption(CalendarSettings.SHOW_EVENT_LOCATIONS);
        optionShowEventsUntil = loadOrInitOption(CalendarSettings.SHOW_EVENTS_UNTIL);


        supportWidgetHeightOption();

        allCalendars.setChecked(optionAllCalendars.getBooleanValue());

        allCalendars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                optionAllCalendars.update(isChecked);
                displayCalendarList();
                refreshWidget();
            }
        });

        eventLocations.setChecked(optionShowEventLocations.getBooleanValue());
        eventLocations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                optionShowEventLocations.update(isChecked);
                updateWidgetProperty(SHOW_EVENT_LOCATIONS, optionShowEventLocations.getBooleanValue());
            }
        });

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        calendarList.setLayoutManager(mLayoutManager);

        displayCalendarList();

        updateCalendarUntilTextView();

        return view;
    }


    @OnClick(R.id.show_events_until)
    public void showEventsUntilCallback() {
        new MaterialDialog.Builder(getContext())
                .title("Calendar Duration")
                .items(R.array.calendar_duration_list)
                .itemsCallbackSingleChoice(getSelectedCalendarOptionIndex(), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        optionShowEventsUntil.update(numDaysDisplayValues[which]);
                        updateCalendarUntilTextView();
                        refreshWidget();
                        return true;
                    }
                })
                .show();
    }

    private void updateCalendarUntilTextView() {
        showEventsUntil.setSubHeaderText(getResources().getStringArray(R.array.calendar_duration_list)[getSelectedCalendarOptionIndex()]);
    }

    public void displayCalendarList() {
        if (optionAllCalendars.getBooleanValue()) {
            calendarList.setVisibility(View.GONE);
        } else {
            calendarList.setVisibility(View.VISIBLE);

            // query for the list of calendars
            List<CalendarInfo> calendars = CalendarWidget.getCalendars(getContext(), widget);
            CalendarListAdapter mAdapter = new CalendarListAdapter(calendars, widget);
            calendarList.setAdapter(mAdapter);
        }
    }

    private int getSelectedCalendarOptionIndex() {
        return Arrays.asList(numDaysDisplayValues).indexOf(optionShowEventsUntil.getIntValue());
    }

    public static List<WidgetOption> getEnabledCalendars(Widget widget) {
        return widget.getOptions(CalendarSettings.CALENDAR_ENABLED);
    }
}