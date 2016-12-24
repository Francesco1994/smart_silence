package app.smartsilence.com.smartsilence;

import android.support.v7.app.AppCompatActivity;

public class FavoritePlace extends AppCompatActivity {

    /*final Context context = this;
    private DbManager db = null;
    private CursorAdapter adapter;
    private ListView listview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_place);
        db = new DbManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp, null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
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

    public void salva(View v)
    {
        EditText name = (EditText) findViewById(R.id.namePlace);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        EditText address = (EditText) findViewById(R.id.address);
        EditText number = (EditText) findViewById(R.id.number);
        EditText pro = (EditText) findViewById(R.id.pro);

        db.save(name.getEditableText().toString(), address.getEditableText().toString(), number.getEditableText().toString());
        adapter.changeCursor(db.query());
    }*/
}