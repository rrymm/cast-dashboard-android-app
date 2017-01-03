package com.silver.dan.castdemo;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.DataCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.DataCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.DataCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.widgets.IntroductoryOverlay;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.silver.dan.castdemo.SettingEnums.BackgroundType;
import com.silver.dan.castdemo.settingsFragments.CalendarSettings;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnSettingChangedListener, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private boolean mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    public static final int NAV_VIEW_WIDGETS_ITEM = 0;
    public static final int NAV_VIEW_OPTIONS_LAYOUT_ITEM = 1;
    public static final int NAV_VIEW_OPTIONS_THEME_ITEM = 2;


    //drawer
    @BindView(R.id.nvView)
    NavigationView navView;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;

    @BindView(R.id.top_toolbar)
    Toolbar top_toolbar;

    @OnClick(R.id.logout_btn)
    public void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.LOGOUT, true);
        startActivity(intent);
        finish();
    }

    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    private static DataCastManager mCastManager;
    private DataCastConsumer mCastConsumer;
    private MenuItem mediaRouteMenuItem;
    private WidgetList widgetListFrag;

    private FirebaseAnalytics mFirebaseAnalytics;


    public void switchToFragment(Fragment destinationFrag, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment, destinationFrag);

        if (addToBackStack)
            transaction.addToBackStack(null);

        transaction.commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showFtu() {
        IntroductoryOverlay overlay = new IntroductoryOverlay.Builder(this)
                .setMenuItem(mediaRouteMenuItem)
                .setTitleText(R.string.intro_overlay_text)
                .setSingleTime()
                .build();
        overlay.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);


        FlowManager.init(new FlowConfig.Builder(this).build());

        widgetListFrag = new WidgetList();
        switchToFragment(widgetListFrag, false);



        // Set the adapter for the list view
        Menu menu = navView.getMenu();

        menuItems.add(menu.add(0, NAV_VIEW_WIDGETS_ITEM, 0, "Widgets"));
        menuItems.add(menu.add(0, NAV_VIEW_OPTIONS_LAYOUT_ITEM, 1, "Layout"));
        menuItems.add(menu.add(0, NAV_VIEW_OPTIONS_THEME_ITEM, 2, "Theme"));


        // Set a Toolbar to replace the ActionBar.
        setSupportActionBar(top_toolbar);

        // Set the menu icon instead of the launcher icon.
        ActionBar ab = getSupportActionBar();
        assert ab != null;

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, top_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        mDrawer.addDrawerListener(mDrawerToggle);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerToggle.syncState();


        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });


        BaseCastManager.checkGooglePlayServices(this);
        CastConfiguration.Builder options = new CastConfiguration.Builder(getResources().getString(R.string.app_id))
                .enableAutoReconnect()
                .enableWifiReconnection()
                .addNamespace(getResources().getString(R.string.namespace))
                .setLaunchOptions(false, Locale.getDefault());

        if (BuildConfig.DEBUG)
            options.enableDebug();

        DataCastManager.initialize(this, options.build());

        mCastManager = DataCastManager.getInstance();
        mCastManager.setStopOnDisconnect(false);

        mCastManager.reconnectSessionIfPossible();
        CastCommunicator.init(this, mCastManager);

        mCastConsumer = new DataCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String applicationStatus, String sessionId, boolean wasLaunched) {
//                if (wasLaunched) {
//                // always need to send since changes might have been made while not connected
                    sendAllOptions();
                    CastCommunicator.sendAllWidgets();
//                }
            }

            @Override
            public void onCastAvailabilityChanged(boolean castPresent) {
                if (castPresent && mIsHoneyCombOrAbove) {

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mediaRouteMenuItem.isVisible()) {
                                showFtu();
                            }
                        }
                    }, 1000);
                }
            }
        };


        if (!LoginActivity.restoreUser()) {
            logout();
            return;
        }

        setupNavBarUserInfo();


        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void setupNavBarUserInfo() {
        View header = navView.getHeaderView(0);


        TextView displayName = (TextView) header.findViewById(R.id.userDisplayName);
        displayName.setText(LoginActivity.user.getDisplayName());

        TextView email = (TextView) header.findViewById(R.id.userEmail);
        email.setText(LoginActivity.user.getEmail());

        ImageView profilePhoto = (ImageView) header.findViewById(R.id.userPhoto);
        Picasso.with(getApplicationContext()).load(LoginActivity.user.getPhotoUrl()).into(profilePhoto);
    }

    private void selectDrawerItem(MenuItem menuItem) {
        int selected = menuItem.getItemId();
        Fragment destination = null;
        boolean backStack = false;

        if (selected == NAV_VIEW_OPTIONS_LAYOUT_ITEM) {
            destination = new AppSettingsLayout();
            backStack = true;

        } else if (selected == NAV_VIEW_WIDGETS_ITEM) {
            destination = new WidgetList();
            backStack = false;
        } else if (selected == NAV_VIEW_OPTIONS_THEME_ITEM) {
            destination = new AppSettingsTheme();
            backStack = true;
        }


        if (!menuItem.isChecked()) {
            switchToFragment(destination, backStack);
            uncheckAllMenuItems();
        }

        mDrawer.closeDrawer(GravityCompat.START);

    }

    private void uncheckAllMenuItems() {
        for (MenuItem item : menuItems) {
            item.setChecked(false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mediaRouteMenuItem = mCastManager.
                addMediaRouterButton(menu, R.id.media_route_menu_item);

        return true;
    }

    private void sendAllOptions() {
        AppSettingsBindings settings = new AppSettingsBindings();
        settings.loadAllSettings(this);

        JSONObject options = new JSONObject();
        try {
            options.put(AppSettingsBindings.COLUMN_COUNT, settings.getNumberOfColumnsUI());
            options.put(AppSettingsBindings.BACKGROUND_COLOR, settings.getBackgroundColorHexStr());
            options.put(AppSettingsBindings.WIDGET_COLOR, settings.getWidgetColorHexStr());
            options.put(AppSettingsBindings.BACKGROUND_TYPE, settings.getBackgroundTypeUI());
            options.put(AppSettingsBindings.WIDGET_TRANSPARENCY, settings.getWidgetTransparencyUI());
            options.put(AppSettingsBindings.TEXT_COLOR, settings.getTextColorHextStr());
            options.put(AppSettingsBindings.SCREEN_PADDING, settings.getScreenPaddingUI());
            options.put(AppSettingsBindings.LOCALE, getResources().getConfiguration().locale.getLanguage());
            options.put(AppSettingsBindings.SLIDESHOW_INTERVAL, settings.getSlideshowInterval());

            if (settings.getBackgroundType() == BackgroundType.PICTURE) {
                AppSettingsTheme.sendBackgroundImage(new File(settings.backgroundImageLocalPath));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        CastCommunicator.sendJSON("options", options);
    }


    public void setDrawerItemChecked(int menuItem) {
        navView.getMenu().getItem(menuItem).setChecked(true);
    }

    @Override
    public void onSettingChanged(String setting, String value) {
        JSONObject options = new JSONObject();
        try {
            options.put(setting, value);
            CastCommunicator.sendJSON("options", options);

            // @todo cleanup, move to client/angular
            // when the column count changes, force refresh all maps
            if (setting.equals(AppSettingsBindings.COLUMN_COUNT)) {
                Widget.fetchAll(Widget.WidgetType.MAP, new FetchAllWidgetsListener() {
                    @Override
                    public void results(List<Widget> widgets) {
                        for (Widget widget : widgets) {
                            CastCommunicator.sendWidget(widget);
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSettingChanged(String setting, int value) {
        onSettingChanged(setting, String.valueOf(value));
    }

    public void onItemMoved(JSONObject widgetsOrder) {
        CastCommunicator.sendJSON("order", widgetsOrder);
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastManager = DataCastManager.getInstance();
        if (mCastManager != null) {
            mCastManager.addDataCastConsumer(mCastConsumer);
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        mDrawer.closeDrawer(GravityCompat.START);
        uncheckAllMenuItems();
        super.onBackPressed();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(MainActivity.TAG, "Error connecting to google services");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            // if the user accepts the read calendar access, resend all of the calendar widgets
            // if they deny, remove all calendar widgets
            case CalendarSettings.MY_PERMISSIONS_REQUEST_READ_CALENDAR: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Widget.fetchAll(Widget.WidgetType.CALENDAR, new FetchAllWidgetsListener() {
                        @Override
                        public void results(List<Widget> widgets) {
                            for (Widget w : widgets) {
                                CastCommunicator.sendWidget(w);
                            }
                        }
                    });
                    widgetListFrag.processPermissionReceivedCallback(CalendarSettings.MY_PERMISSIONS_REQUEST_READ_CALENDAR, true);
                } else {
                    Widget.fetchAll(Widget.WidgetType.CALENDAR, new FetchAllWidgetsListener() {
                        @Override
                        public void results(List<Widget> widgets) {
                            for (Widget w : widgets) {
                                CastCommunicator.deleteWidget(w);
                                w.delete();
                            }

                            widgetListFrag.refreshList();
                        }
                    });
                    widgetListFrag.processPermissionReceivedCallback(CalendarSettings.MY_PERMISSIONS_REQUEST_READ_CALENDAR, false);
                }
            }
        }
    }

}