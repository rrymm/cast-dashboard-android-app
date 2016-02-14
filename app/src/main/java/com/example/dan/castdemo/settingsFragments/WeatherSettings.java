package com.example.dan.castdemo.settingsFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dan.castdemo.MainActivity;
import com.example.dan.castdemo.R;
import com.example.dan.castdemo.Widget;
import com.example.dan.castdemo.WidgetOption;
import com.example.dan.castdemo.Widget_Table;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.raizlabs.android.dbflow.sql.language.Select;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WeatherSettings extends WidgetSettingsFragment {

    public static String WEATHER_LAT = "WEATHER_LAT";
    public static String WEATHER_LNG = "WEATHER_LNG";
    public static String WEATHER_CITY = "WEATHER_CITY";

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    WidgetOption weatherLat;
    WidgetOption weatherLng;

    WidgetOption weatherCity;

    @Bind(R.id.weather_city)
    EditText tvWeatherCity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_settings, container, false);
        ButterKnife.bind(this, view);

        weatherLng = widget.getOption(WEATHER_LNG);
        weatherLat = widget.getOption(WEATHER_LAT);
        weatherCity = widget.getOption(WEATHER_CITY);


        tvWeatherCity.setText(weatherCity.value);

        return view;
    }

    @OnClick(R.id.weather_city)
    public void selectCity() {

        //restrict to cities
        AutocompleteFilter cityFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(cityFilter)
                            .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public static void init(Widget widget) {
        widget.initOption(WEATHER_LAT, "47.6025269");
        widget.initOption(WEATHER_LNG, "-122.3411561");
        widget.initOption(WEATHER_CITY, "Seattle, WA");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getContext(), data);
                Log.i(MainActivity.TAG, "Place: " + place.getName());


                weatherLat.value = String.valueOf(place.getLatLng().latitude);
                weatherLng.value = String.valueOf(place.getLatLng().longitude);
                weatherCity.value = place.getName().toString();


                weatherLat.save();
                weatherLng.save();
                weatherCity.save();


                tvWeatherCity.setText(weatherCity.value);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getContext(), data);
                // TODO: Handle the error.
                Log.i(MainActivity.TAG, status.getStatusMessage());

            }
        }
    }
}