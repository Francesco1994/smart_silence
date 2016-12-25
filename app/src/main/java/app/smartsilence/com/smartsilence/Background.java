package app.smartsilence.com.smartsilence;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.grandcentrix.tray.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Background extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    protected LocationManager locationManager;
    LocationRequest mLocationRequest;
    final Context context = this;
    private static final String TAG = "LocationService";
    private static final int LOCATION_INTERVAL = 1000;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private static PowerManager.WakeLock wl;

    String chieseUrl = null;
    String muesoUrl = null;
    String scuolaUrl = null;
    String universitaUrl = null;
    String ospedaleUrl = null;

    @Override
    public void onCreate()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();

        googleApiClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        wl = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartLock");
        wl.acquire();

        getNotification();

        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { }

        mLocationRequest = new LocationRequest();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(0);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        AppPreferences pref = new AppPreferences(context);

        int chiese = pref.getInt("Chiese", 0);
        int musei = pref.getInt("Musei", 0);
        int scuola = pref.getInt("Scuola", 0);
        int università = pref.getInt("Scuola", 0);
        int ospedale = pref.getInt("Scuola", 0);

        if (String.valueOf(chiese).equals("1")) {
            chieseUrl = "church|";
        } else {
            chieseUrl = "null|";
        }

        if (String.valueOf(musei).equals("1")) {
            muesoUrl = "museum|";
        } else {
            muesoUrl = "null|";
        }

        if (String.valueOf(scuola).equals("1")) {
            scuolaUrl = "school|";
        } else {
            scuolaUrl = "null|";
        }

        if (String.valueOf(università).equals("1")) {
            universitaUrl = "university|";
        } else {
            universitaUrl = "null|";
        }

        if (String.valueOf(ospedale).equals("1")) {
            ospedaleUrl = "hospital";
        } else {
            ospedaleUrl = "null";
        }

        String stringUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                            + location.getLatitude() + "," + location.getLongitude() +
                            "&radius=30" +
                            "&sensor=false" +
                            "&types=" + chieseUrl + muesoUrl + scuolaUrl + universitaUrl + ospedaleUrl +
                            "&key=AIzaSyAkgiQlvJckABqyt3BRAuu_QecGpwzQmTk";

        new DownloadWebpageTask().execute(stringUrl);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String>
    {
        private Context mainContxt;

        @Override
        protected String doInBackground(String... urls) {

            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (result != null) {
                try {
                    JSONObject jsonRootObject = new JSONObject(result);
                    String jsonArray = jsonRootObject.getString("status");

                    if (jsonArray.equals("OK")) {

                        AppPreferences pref = new AppPreferences(context);

                        int silenzioso = pref.getInt("Silenzioso", 0);
                        int vibrazione = pref.getInt("Vibrazione", 0);

                        if (String.valueOf(silenzioso).equals("1")) {

                            AudioManager audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            audiomanage.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        }

                        if (String.valueOf(vibrazione).equals("1")) {

                            AudioManager audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            audiomanage.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        }
                    } else {

                        AudioManager audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audiomanage.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String downloadUrl(String myurl) throws IOException
    {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            String contentAsString = readIt(is);
            return contentAsString;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream)
    {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(stream));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    private void getNotification()
    {
        Intent notificationIntent = new Intent(this, Background.class);
        notificationIntent.setAction("app.smartsilence.com.smartsilence.foregroundservice.action.main");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Notification notification = new NotificationCompat.Builder(this)
            .setContentTitle("SmartSilence")
            .setTicker("SmartSilence è attivo")
            .setContentText("SmartSilence è attivo")
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();

        startForeground(1, notification);
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() { Toast.makeText(this, "SmartSilence è stato stoppato", Toast.LENGTH_SHORT).show(); }
}

