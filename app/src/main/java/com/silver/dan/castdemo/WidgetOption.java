package com.silver.dan.castdemo;

import android.text.TextUtils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.silver.dan.castdemo.Widget.getFirebaseDashboardWidgetsRef;

@ModelContainer
@Table(database = WidgetDatabase.class)
@IgnoreExtraProperties
public class WidgetOption extends BaseModel {

    @Exclude
    Widget widgetRef;

    @PrimaryKey(autoincrement = true)
    @Exclude
    public long id;

    @Column
    @Exclude
    public String key;

    @Column
    public String value;

    public WidgetOption() {
    }

    @Exclude
    void saveFirebase() {
        getOptionsRef()
                .child(this.key)
                .setValue(this.toMap());
    }

    @Exclude
    private DatabaseReference getOptionsRef() {
        return getFirebaseDashboardWidgetsRef()
                .child(widgetRef.guid)
                .child("optionsMap");
    }

    @Exclude
    public Map<String, String> toMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("value", value);

        return result;
    }


    @Column
    @ForeignKey(saveForeignKeyModel = false)
    @Exclude
    @Deprecated
    ForeignKeyContainer<Widget> widgetForeignKeyContainer;

    @Exclude
    public void associateWidget(Widget widget) {
        widgetForeignKeyContainer = FlowManager.getContainerAdapter(Widget.class).toForeignKeyContainer(widget);

        // keep this line, remove container eventually
        this.widgetRef = widget;
    }

    @Exclude
    public boolean getBooleanValue() {
        return value.equals("1");
    }

    @Exclude
    public void setValue(boolean booleanValue) {
        this.value = booleanValue ? "1" : "0";
    }

    @Exclude
    public void setValue(int value) {
        this.value = Integer.toString(value);
    }

    @Exclude
    public int getIntValue() {
        return Integer.valueOf(value);
    }

    @Exclude
    public void setValue(Date datetime) {
        this.value = Long.toString(datetime.getTime());
    }

    @Exclude
    public Date getDate() {
        return new Date(Long.parseLong(this.value));
    }

    @Exclude
    @Override
    public void save() {
        saveFirebase();
    }

    @Exclude
    public void update(int value) {
        setValue(value);
        save();
    }

    @Exclude
    public void update(boolean value) {
        setValue(value);
        save();
    }

    @Exclude
    public void update(String str) {
        setValue(str);
        save();
    }

    @Exclude
    public void update(double n) {
        setValue(n);
        save();
    }

    @Exclude
    private void setValue(double n) {
        this.value = Double.toString(n);
    }

    @Exclude
    public void setValue(long l) {
        this.value = Long.toString(l);
    }

    @Exclude
    public void setValue(String str) {
        this.value = str;
    }


    @Exclude
    public List<String> getList() {
        String[] stringArray = this.value.split(",");
        List<String> items = new ArrayList<>();
        Collections.addAll(items, stringArray);


        if (items.size() == 1 && items.get(0).length() == 0)
            return new ArrayList<>();

        return items;
    }

    @Exclude
    public void update(List<String> enabledIds) {
        setValue(enabledIds);
        save();
    }

    @Exclude
    protected void setValue(List<String> enabledIds) {
        setValue(TextUtils.join(",", enabledIds));
    }
}
