package app.smartsilence.com.smartsilence.tabs_fragments;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;

import net.grandcentrix.tray.AppPreferences;

import app.smartsilence.com.smartsilence.R;

public class TabFragmentOne extends Fragment {

    View rootView;
    protected LocationManager locationManager;
    protected LocationListener locationListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_one, container, false);
        setRetainInstance(true);

        AppPreferences pref = new AppPreferences(getContext());

        int silenzioso = pref.getInt("Silenzioso", 0);
        int vibrazione = pref.getInt("Vibrazione", 0);

        final RadioButton radioSilenzioso = (RadioButton) rootView.findViewById(R.id.radioSilenzioso);
        final RadioButton radioVibrazione = (RadioButton) rootView.findViewById(R.id.radioVibrazione);

        if(String.valueOf(silenzioso).equals("1")) {

            radioSilenzioso.setChecked(true);
        }
        else {
            if(String.valueOf(vibrazione).equals("1")) {

                radioVibrazione.setChecked(true);
            }
        }

        int chiese = pref.getInt("Chiese", 0);
        int musei = pref.getInt("Musei", 0);
        int scuola = pref.getInt("Scuola", 0);
        int università = pref.getInt("Università", 0);
        int ospedale = pref.getInt("Ospedale", 0);

        final CheckBox chieseCheck = (CheckBox) rootView.findViewById(R.id.checkChiese);
        final CheckBox museiCheck = (CheckBox) rootView.findViewById(R.id.checkMusei);
        final CheckBox scuolaCheck = (CheckBox) rootView.findViewById(R.id.checkScuola);
        final CheckBox universitàCheck = (CheckBox) rootView.findViewById(R.id.checkUniversità);
        final CheckBox ospedaleCheck = (CheckBox) rootView.findViewById(R.id.checkOspedale);

        if(String.valueOf(chiese).equals("1")) {

            chieseCheck.setChecked(true);
        }

        if(String.valueOf(musei).equals("1")) {

            museiCheck.setChecked(true);
        }

        if(String.valueOf(scuola).equals("1")) {

            scuolaCheck.setChecked(true);
        }

        if(String.valueOf(università).equals("1")) {

            universitàCheck.setChecked(true);
        }

        if(String.valueOf(ospedale).equals("1")) {

            ospedaleCheck.setChecked(true);
        }

        return rootView;
    }
}
