package app.smartsilence.com.smartsilence;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    //public static final String INTENT_ACTION = "app.smartsilence.com.smartsilence.receiver.intent.action.SMARTSILENCE_BROADCAST";

    private static final int REQUEST_LOCATION = 1;
    public static FragmentManager fragmentManager;
    private ViewPager viewPager;
    private TableLayout tabLayout;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.settings));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.map));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_LOCATION);
        }

        SharedPreferences pref = getSharedPreferences("SmartSilence", Context.MODE_PRIVATE);
        Boolean notShow = pref.getBoolean("notShow", false);

        if (notShow == false) {

            Intent intent = new Intent();
            intent.setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");

            if (isCallable(intent)) {

                final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(this);
                dontShowAgain.setText("Non mostrare di nuovo");
                dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    }
                });

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.protected_app);

                Button dialogButton = (Button) dialog.findViewById(R.id.attiva);
                Button dialogButtonClose = (Button) dialog.findViewById(R.id.chiudi);

                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        huaweiProtectedApps();
                    }
                });

                dialogButtonClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        }
    }

    public void notShow(View view)
    {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.checkNotShow:
                if (checked) {

                    SharedPreferences pref = getSharedPreferences("SmartSilence", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("notShow", true);
                    editor.commit();

                    break;
                }
        }
    }

    private boolean isCallable(Intent intent)
    {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void huaweiProtectedApps()
    {
        try {
            String cmd = "am start -n com.huawei.systemmanager/.optimize.process.ProtectActivity";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                cmd += " --user " + getUserSerial();
            }
            Runtime.getRuntime().exec(cmd);
        } catch (IOException ignored) {
        }
    }

    private String getUserSerial()
    {
        Object userManager = getSystemService("user");
        if (null == userManager) return "";

        try {
            Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle", (Class<?>[]) null);
            Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);
            Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", myUserHandle.getClass());
            Long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
            if (userSerial != null) {
                return String.valueOf(userSerial);
            } else {
                return "";
            }
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ignored) {
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.shareApp) {

            Resources res = getResources();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = res.getString(R.string.share_app_message);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "SmartSilence");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, res.getString(R.string.share_with)));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startService(View v)
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog);

                TextView dialogText = (TextView) dialog.findViewById(R.id.text);
                dialogText.setText(R.string.start_gps);

                Button gpsOption = (Button) dialog.findViewById(R.id.attivaGPS);
                Button dialogButton = (Button) dialog.findViewById(R.id.annulla);

                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();
                    }
                });

                gpsOption.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }

            startService(new Intent(this, Background.class));

            /*Intent myAlarm = new Intent(getApplicationContext(), Background.class);
            PendingIntent recurringAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, myAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Calendar updateTime = Calendar.getInstance();
            alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, recurringAlarm);*/

        } else {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog);

            TextView dialogText = (TextView) dialog.findViewById(R.id.text);
            dialogText.setText("SmartSilence ha bisogno dei dati internet per funzionare. Per continuare, per favore, attiva i dati e premere su Attiva App");

            Button gpsOption = (Button) dialog.findViewById(R.id.attivaGPS);
            Button dialogButton = (Button) dialog.findViewById(R.id.annulla);

            gpsOption.setVisibility(View.GONE);

            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    public void stopService(View v)
    {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        stopService(new Intent(this, Background.class));

        NotificationManager notifManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }

    public void favoriteActivity(View v)
    {
        Intent intent = new Intent(this, FavoritePlace.class);
        startActivity(intent);
    }

    public void onRadioButtonClicked(View view)
    {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radioSilenzioso:
                if (checked) {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Silenzioso", 1);
                    pref.put("Vibrazione", 0);

                    break;
                }

            case R.id.radioVibrazione:
                if (checked) {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Silenzioso", 0);
                    pref.put("Vibrazione", 1);

                    break;
                }
        }
    }

    public void onCheckboxClicked(View view)
    {
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {
            case R.id.checkChiese:
                if (checked) {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Chiese", 1);

                    break;
                }
                else {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Chiese", 0);

                    break;
                }

            case R.id.checkMusei:
                if (checked) {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Musei", 1);

                    break;
                } else {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Musei", 0);

                    break;
                }

            case R.id.checkScuola:
                if (checked) {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Scuola", 1);

                    break;
                } else {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Scuola", 0);

                    break;
                }

            case R.id.checkUniversità:
                if (checked) {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Università", 1);

                    break;
                } else {

                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Università", 0);

                    break;
                }

            case R.id.checkOspedale:
                if (checked) {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Ospedale", 1);

                    break;
                } else {
                    AppPreferences pref = new AppPreferences(context);
                    pref.put("Ospedale", 0);

                    break;
                }
        }
    }
}

