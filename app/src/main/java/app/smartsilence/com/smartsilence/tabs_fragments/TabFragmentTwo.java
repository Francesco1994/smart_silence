package app.smartsilence.com.smartsilence.tabs_fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import app.smartsilence.com.smartsilence.InfoPlace;
import app.smartsilence.com.smartsilence.R;

public class TabFragmentTwo extends Fragment implements OnMapReadyCallback, android.location.LocationListener {

    protected LocationManager locationManager;
    View rootView;
    private MapView mapView;
    private GoogleMap googleMap;
    String icon = null;
    String name = null;
    String vicinity = null;
    String openNow = null;
    String place_id = null;
    public String info[];
    String photo_reference = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_two, container, false);
        setRetainInstance(true);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        googleMap = map;
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        LatLngBounds ITALIA = new LatLngBounds(new LatLng(36.466103, 6.385101), new LatLng(47.206453, 18.728446));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(ITALIA, 10));
    }

    @Override
    public void onLocationChanged(Location location) {

        String urlPlaces = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                            + location.getLatitude() +","+ location.getLongitude() +
                            "&radius=400" +
                            "&sensor=false" +
                            "&types=bar|restaurant|movie_theater" +
                            "&key=AIzaSyAkgiQlvJckABqyt3BRAuu_QecGpwzQmTk";

        new DownloadWebpageTask().execute(urlPlaces);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

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
        protected void onPostExecute(String result) {

            try {

                JSONObject jsonRootObject = new JSONObject(result);
                JSONArray places = jsonRootObject.getJSONArray("results");

                for (int i=0; i < places.length(); i++) {

                    JSONObject place = places.getJSONObject(i);

                    if(place.has("photos")) {

                        JSONArray photos = place.getJSONArray("photos");

                        for (int j = 0; j < photos.length(); j++) {

                            JSONObject getPhtotos = photos.getJSONObject(j);
                            photo_reference = getPhtotos.getString("photo_reference");
                        }
                    }
                    else {
                        photo_reference = "false";
                    }

                    icon = place.getString("icon");
                    name = place.getString("name");
                    vicinity = place.getString("vicinity");
                    place_id = place.getString("place_id");

                    if(place.has("opening_hours")) {

                        JSONObject hours = place.getJSONObject("opening_hours");
                        openNow = hours.getString("open_now");
                    }

                    JSONObject geo = place.getJSONObject("geometry");
                    JSONObject location = geo.getJSONObject("location");

                    Double lat = location.getDouble("lat");
                    Double lng = location.getDouble("lng");

                    googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat,lng))
                        .title(name)
                        .snippet(vicinity +"&"+ openNow +"&"+ icon +"&"+ place_id +"&"+ photo_reference));
                }
            } catch (JSONException e) { e.printStackTrace(); }

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(final Marker marker) {

                    View v = getActivity().getLayoutInflater().inflate(R.layout.windowlayout, null);
                    ImageView imageView = (ImageView) v.findViewById(R.id.icon);

                    info = marker.getSnippet().split("&");

                    String urlImage = info[2];

                    Glide.with(getContext()).load(urlImage).asBitmap().override(50,50).listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            e.printStackTrace();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if(!isFromMemoryCache) marker.showInfoWindow();
                            return false;
                        }
                    }).into(imageView);

                    TextView namePlace = (TextView) v.findViewById(R.id.name);
                    TextView vicinityPlace = (TextView) v.findViewById(R.id.vicinity);
                    TextView openText = (TextView) v.findViewById(R.id.open);

                    namePlace.setTypeface(null, Typeface.BOLD);
                    namePlace.setText(marker.getTitle());
                    vicinityPlace.setText(info[0]);

                    if(info[1].equals("true")) {
                        openText.setTextColor(Color.parseColor("#4CFF3C"));
                        openText.setText(R.string.open_now);
                    }
                    else {
                        openText.setTextColor(Color.parseColor("#FF2F2F"));
                        openText.setText(R.string.closed_now);
                    }

                    googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        public void onInfoWindowClick(Marker marker) {

                            Intent intent = new Intent(getActivity(), InfoPlace.class);
                            intent.putExtra("place_id", info[3]);
                            intent.putExtra("photo", info[4]);
                            startActivity(intent);
                        }
                    });

                    return v;
                }
            });
        }
    }

    private String downloadUrl(String myurl) throws IOException {

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

    public String readIt(InputStream stream) {

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

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { Log.d("Latitude","status"); }

    @Override
    public void onDestroy()
    {
        Log.i("PROVA SERVICE", "Distruzione Service");
    }
}



