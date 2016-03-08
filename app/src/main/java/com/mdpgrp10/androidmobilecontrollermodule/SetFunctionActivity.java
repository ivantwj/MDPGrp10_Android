package com.mdpgrp10.androidmobilecontrollermodule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.mdpgrp10.androidmobilecontrollermodule.Utils.*;

public class SetFunctionActivity extends DialogFragment {

    private static final String TAG = "KeySettingActivity";
    SharedPreferences spf;
    EditText editTextF1, editTextF2;
    Button buttonSave;

    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_set_function, container, false);

        editTextF1 = (EditText) view.findViewById(R.id.editTextF1);
        editTextF2 = (EditText) view.findViewById(R.id.editTextF2);
        buttonSave = (Button) view.findViewById(R.id.buttonSave);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle("Set Functions");
        spf = getActivity().getSharedPreferences(PREF_DB, Context.MODE_PRIVATE);

        editTextF1.setText(spf.getString(SET_CMD1, SET_CMD1_DEFAULT));
        editTextF2.setText(spf.getString(SET_CMD2, SET_CMD2_DEFAULT));
        buttonSave.setOnClickListener(saveSettings);
    }

    private View.OnClickListener saveSettings = new View.OnClickListener() {
        public void onClick(View v) {
            savePrefs();
        }
    };

    private void savePrefs() {

        String c1 = editTextF1.getText().toString();
        c1 = c1.length() > 0 ? c1 : SET_CMD1_DEFAULT;
        String c2 = editTextF2.getText().toString();
        c2 = c2.length() > 0 ? c2 : SET_CMD2_DEFAULT;

        SharedPreferences.Editor editor = spf.edit();

        editor.putString(SET_CMD1, c1);
        editor.putString(SET_CMD2, c2);

        editor.apply(); //commit()

        Toast.makeText(getActivity(), "Functions saved.", Toast.LENGTH_SHORT).show();
        getDialog().dismiss();
    }
}
