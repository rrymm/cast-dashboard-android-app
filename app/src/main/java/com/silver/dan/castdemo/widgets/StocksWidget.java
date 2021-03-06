package com.silver.dan.castdemo.widgets;

import android.content.Context;

import com.silver.dan.castdemo.Widget;
import com.silver.dan.castdemo.WidgetOption;
import com.silver.dan.castdemo.settingsFragments.StocksSettings;
import com.silver.dan.castdemo.settingsFragments.WidgetSettingsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class StocksWidget extends UIWidget {

    public StocksWidget(Context context, Widget widget) {
        super(context, widget);
    }

    @Override
    public void init() {
        widget.initOption(StocksSettings.STOCK_IN_LIST, "");
    }


    @Override
    public JSONObject getContent() throws JSONException {
        WidgetOption savedStocks = widget.loadOrInitOption(StocksSettings.STOCK_IN_LIST, context);
        List<String> stockTickers = savedStocks.getList();

        JSONObject json = new JSONObject();
        JSONArray tickers = new JSONArray();
        for (String ticker: stockTickers) {
            tickers.put(ticker);
        }

        json.put("tickers", tickers);

        return json;
    }

    @Override
    public WidgetSettingsFragment createSettingsFragment() {
        return new StocksSettings();
    }

    @Override
    public String getWidgetPreviewSecondaryHeader() {
        WidgetOption savedStocksOption = widget.loadOrInitOption(StocksSettings.STOCK_IN_LIST, context);
        List<String> tickers = savedStocksOption.getList();
        if (tickers.size() == 0) {
            return "No stocks selected";
        }


        // transform the stock ids into tickers, build a comma separated string

        ArrayList<String> stocksString = new ArrayList<>();

        for (String ticker: tickers) {
            //otherwise use the ticker
            stocksString.add(ticker);
            if (stocksString.size() > 4) {
                break;
            }
        }


        return android.text.TextUtils.join(", ", stocksString);
    }
}