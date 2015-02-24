package info.korzeniowski.stackoverflow.searcher.ui.details;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import info.korzeniowski.stackoverflow.searcher.R;

public class DetailsActivity extends FragmentActivity {
    public static final String EXTRA_URL = "EXTRA_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        String url = getIntent().getExtras().getString(EXTRA_URL);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, DetailsFragment.newInstance(url))
                    .commit();
        }
    }
}
