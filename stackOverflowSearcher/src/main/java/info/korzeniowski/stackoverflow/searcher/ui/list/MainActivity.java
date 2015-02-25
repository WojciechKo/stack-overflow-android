package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.squareup.otto.Bus;

import java.util.Arrays;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.korzeniowski.stackoverflow.searcher.App;
import info.korzeniowski.stackoverflow.searcher.R;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;

public class MainActivity extends FragmentActivity {
    private final String STATE_SORT_BY = "STATE_SORT_BY";

    @InjectView(R.id.query)
    EditText query;

    @InjectView(R.id.sortBy)
    Spinner sortBy;
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

        StackOverflowApi.SortBy[] sortByValues = StackOverflowApi.SortBy.values();
        ArrayAdapter<StackOverflowApi.SortBy> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                sortByValues);
        sortBy.setAdapter(adapter);

        if (savedInstanceState == null) {
            sortBy.setSelection(Arrays.asList(StackOverflowApi.SortBy.values()).indexOf(StackOverflowApi.SortBy.CREATION));
        } else {
            sortBy.setSelection(savedInstanceState.getInt(STATE_SORT_BY));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SORT_BY, sortBy.getSelectedItemPosition());
    }

    @OnClick(R.id.search)
    public void onSearchClicked() {
        if (TextUtils.isEmpty(query.getText())) {
            query.setError(getString(R.string.queryIsRequired));
            return;
        }
        query.setError(null);

        bus.post(new SearchEvent(SearchEvent.StackOverflowQuery
                .builder()
                .intitle(query.getText().toString())
                .sort(StackOverflowApi.SortBy.values()[sortBy.getSelectedItemPosition()])
                .build()));
    }
}



