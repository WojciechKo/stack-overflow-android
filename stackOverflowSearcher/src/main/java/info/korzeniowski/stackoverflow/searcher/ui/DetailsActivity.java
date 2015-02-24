package info.korzeniowski.stackoverflow.searcher.ui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.stackoverflow.searcher.R;

public class DetailsActivity extends Activity {
    public static final String EXTRA_URL = "url";

    @InjectView(R.id.webView)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.inject(this);
        String url = getIntent().getExtras().getString(EXTRA_URL);
        webView.loadUrl(url);
    }
}
