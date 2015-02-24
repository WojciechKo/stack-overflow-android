package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.EditText;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.korzeniowski.stackoverflow.searcher.App;
import info.korzeniowski.stackoverflow.searcher.R;

public class MainActivity extends FragmentActivity {
    @InjectView(R.id.query)
    EditText query;

    @Inject
    Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        ((App) getApplication()).inject(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.listFragment, ListFragment.newInstance())
                    .commit();
        }
    }

    @OnClick(R.id.search)
    public void onSearchClicked() {
        if (TextUtils.isEmpty(query.getText())) {
            query.setError(getString(R.string.queryIsRequired));
            return;
        }
        query.setError(null);

        bus.post(new SearchEvent(SearchEvent.StackOverflowQuery.builder().intitle(query.getText().toString()).build()));
    }
}



