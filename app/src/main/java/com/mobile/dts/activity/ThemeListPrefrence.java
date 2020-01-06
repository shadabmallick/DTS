package com.mobile.dts.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;



public class ThemeListPrefrence extends ListPreference implements DialogInterface.OnClickListener {

    private int mClickedDialogEntryIndex;
    private Context context;

    public ThemeListPrefrence(Context context, AttributeSet attrs) {
        super(context, attrs);




    }


    private int getValueIndex() {
        return findIndexOfValue(this.getValue() +"");
    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        mClickedDialogEntryIndex = getValueIndex();
        builder.setSingleChoiceItems(this.getEntries(), mClickedDialogEntryIndex, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mClickedDialogEntryIndex = which;

            }
        });

        System.out.println(getEntry() + " " + this.getEntries()[0]);
        builder.setPositiveButton("UPDATE", this);
    }

    public  void onClick (DialogInterface dialog, int which)
    {
        this.setValue(this.getEntryValues()[mClickedDialogEntryIndex]+"");
    }

}