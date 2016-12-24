package app.smartsilence.com.smartsilence;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class InfoPlace extends AppCompatActivity {

    public static FragmentManager fragmentManager;
    private ViewPager viewPager;
    private TableLayout tabLayout;
    final Context context = this;
    String noWebSite = null;

    String phoneNumber = null;
    String webSite = null;
    String mapsUrl = null;

    int j = 0;
    String rating = null;
    float ratingTotal = 0;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_place);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View mToolbar = findViewById(R.id.toolbar);
        mToolbar.getBackground().setAlpha(0);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp, null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Bundle extras = getIntent().getExtras();
        String place_id = extras.getString("place_id");

        /*MobileAds.initialize(getApplicationContext(), "ca-app-pub-1026366199199479/9519133744");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

        String URL = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place_id + "&key=AIzaSyAkgiQlvJckABqyt3BRAuu_QecGpwzQmTk";

        new DownloadWebpageTask().execute(URL);
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
                String resultPlace = jsonRootObject.getString("result");
                JSONObject places = new JSONObject(resultPlace);

                Bundle extras = getIntent().getExtras();
                String photo_reference = extras.getString("photo");

                if (photo_reference.equals("false")) {

                    ImageView photo = (ImageView) findViewById(R.id.imagePlace);
                    photo.setBackgroundColor(Color.rgb(59, 89, 152));

                } else {

                    String URLPhoto = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photo_reference + "&key=AIzaSyAkgiQlvJckABqyt3BRAuu_QecGpwzQmTk";

                    ImageView photo = (ImageView) findViewById(R.id.imagePlace);
                    Glide.with(context)
                            .load(URLPhoto)
                            .into(photo);
                }

                String name = places.getString("name");
                String vicinity = places.getString("vicinity");

                mapsUrl = places.getString("url");

                if (places.has("reviews")) {

                    JSONArray reviews = places.getJSONArray("reviews");

                    for (j = 0; j < reviews.length(); j++) {

                        JSONObject getPhtotos = reviews.getJSONObject(j);
                        rating = getPhtotos.getString("rating");

                        ratingTotal = (ratingTotal + Float.parseFloat(rating));
                    }

                    ratingTotal = (ratingTotal / j);
                } else {
                    ratingTotal = 0;
                }

                TextView textName = (TextView) findViewById(R.id.namePlace);
                TextView textVicinity = (TextView) findViewById(R.id.vicinity);
                TextView textPhone = (TextView) findViewById(R.id.phoneNumber);
                TextView textWebSite = (TextView) findViewById(R.id.textWebSiteSmall);
                TextView ratingValueText = (TextView) findViewById(R.id.ratingValueText);
                RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);

                if (places.has("formatted_phone_number")) {
                    phoneNumber = places.getString("formatted_phone_number");
                    textPhone.setText(phoneNumber);
                } else {
                    textPhone.setText(R.string.no_number);
                }

                if (places.has("website")) {

                    noWebSite = "false";
                    webSite = places.getString("website");
                    textWebSite.setText(webSite);
                } else {

                    noWebSite = "true";
                    textWebSite.setText(R.string.no_website);
                }

                textName.setTypeface(null, Typeface.BOLD);
                textName.setText(name);
                textVicinity.setText(vicinity);

                if (ratingTotal != 0) {
                    Resources res = getResources();
                    ratingValueText.setText(res.getString(R.string.ranking_message) + String.format(": %.1f", ratingTotal));
                } else {
                    ratingValueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    ratingValueText.setText(R.string.no_rating);
                }

                ratingBar.setRating(Float.parseFloat(String.valueOf(ratingTotal)));

                JSONArray reviews = places.getJSONArray("reviews");

                mAdapter = new ReviewsAdapter(reviews);
                mRecyclerView.setAdapter(mAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void callNumber(View v) {

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    public void openWebSite(View v) {

        if(noWebSite.equals("true")) {

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_error_site);

            Button dialogButton = (Button) dialog.findViewById(R.id.annulla);

            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog.dismiss();
                }
            });

            dialog.show();
        }
        else {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webSite));
            startActivity(browserIntent);
        }
    }

    public void share(View v) {

        Resources res = getResources();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = res.getString(R.string.share_message_place) + " " + mapsUrl;
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Condivisione");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent,res.getString(R.string.share_with)));
    }

    public void openMaps(View v) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
        startActivity(browserIntent);
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

    public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

        private JSONArray array;
        private String URLPhotoProfile = null;

        public ReviewsAdapter(JSONArray reviews) {
            array = reviews;
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {

            protected ImageView photo;
            protected TextView vName;
            protected TextView text;
            protected RatingBar ratingBarReviews;
            protected LinearLayout layout;

            public ViewHolder(View v) {

                super(v);
                photo = (ImageView) v.findViewById(R.id.photoProfile);
                vName =  (TextView) v.findViewById(R.id.txtName);
                vName.setTypeface(null, Typeface.BOLD);
                text =  (TextView) v.findViewById(R.id.txtText);
                ratingBarReviews = (RatingBar) v.findViewById(R.id.ratingBarReviews);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder contactViewHolder, int i) {

            JSONObject getReviews = null;

            try {

                getReviews = array.getJSONObject(i);
                String name = getReviews.getString("author_name");
                String txt = getReviews.getString("text");
                String ratingReviews = getReviews.getString("rating");

                if(getReviews.has("profile_photo_url")) {

                    URLPhotoProfile = "https:"+ getReviews.getString("profile_photo_url");

                    Glide.with(context)
                        .load(URLPhotoProfile)
                        .into(contactViewHolder.photo);
                }

                contactViewHolder.vName.setText(name);
                contactViewHolder.text.setText(txt);
                contactViewHolder.ratingBarReviews.setRating(Float.parseFloat(String.valueOf(ratingReviews)));

            } catch (JSONException e) { e.printStackTrace(); }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            if(array.length() > 0) {

                View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reviews, viewGroup, false);
                return new ViewHolder(itemView);
            }

            return null;
        }

        @Override
        public int getItemCount() { return array.length(); }
    }
}
