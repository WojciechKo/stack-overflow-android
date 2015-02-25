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

import static info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi.OrderType;
import static info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi.SortBy;

public class MainActivity extends FragmentActivity {
    private static final String STATE_SORT_BY = "STATE_SORT_BY";
    private static final String STATE_ORDER = "STATE_ORDER";

    @InjectView(R.id.query)
    EditText query;

    @InjectView(R.id.sortBy)
    Spinner sortBy;

    @InjectView(R.id.order)
    Spinner order;

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

        SortBy[] sortByValues = SortBy.values();
        ArrayAdapter<SortBy> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                sortByValues);
        sortBy.setAdapter(sortAdapter);

        OrderType[] orderTypeValues = OrderType.values();
        ArrayAdapter<OrderType> orderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                orderTypeValues);
        order.setAdapter(orderAdapter);

        if (savedInstanceState == null) {
            sortBy.setSelection(Arrays.asList(SortBy.values()).indexOf(SortBy.CREATION));
            order.setSelection(Arrays.asList(OrderType.values()).indexOf(OrderType.DESC));
        } else {
            sortBy.setSelection(savedInstanceState.getInt(STATE_SORT_BY));
            order.setSelection(savedInstanceState.getInt(STATE_ORDER));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SORT_BY, sortBy.getSelectedItemPosition());
        outState.putInt(STATE_ORDER, order.getSelectedItemPosition());
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
                .sort(SortBy.values()[sortBy.getSelectedItemPosition()])
                .order(OrderType.values()[order.getSelectedItemPosition()])
                .build()));
    }
}



