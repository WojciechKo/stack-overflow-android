package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.common.collect.Maps;
import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import auto.parcel.AutoParcel;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.korzeniowski.stackoverflow.searcher.App;
import info.korzeniowski.stackoverflow.searcher.R;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;

public class SearchFragment extends Fragment {
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


    public static Fragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);

        StackOverflowApi.SortBy[] sortByValues = StackOverflowApi.SortBy.values();
        ArrayAdapter<StackOverflowApi.SortBy> sortAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                sortByValues);
        sortBy.setAdapter(sortAdapter);

        StackOverflowApi.OrderType[] orderTypeValues = StackOverflowApi.OrderType.values();
        ArrayAdapter<StackOverflowApi.OrderType> orderAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                orderTypeValues);
        order.setAdapter(orderAdapter);

        if (savedInstanceState == null) {
            sortBy.setSelection(Arrays.asList(StackOverflowApi.SortBy.values()).indexOf(StackOverflowApi.SortBy.CREATION));
            order.setSelection(Arrays.asList(StackOverflowApi.OrderType.values()).indexOf(StackOverflowApi.OrderType.DESC));
        } else {
            sortBy.setSelection(savedInstanceState.getInt(STATE_SORT_BY));
            order.setSelection(savedInstanceState.getInt(STATE_ORDER));
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SORT_BY, sortBy.getSelectedItemPosition());
        outState.putInt(STATE_ORDER, order.getSelectedItemPosition());
    }


    @OnClick(R.id.search)
    public void onSearchClicked() {
        bus.post(new SearchEvent(StackOverflowQuery
                .builder()
                .intitle(query.getText().toString())
                .sort(StackOverflowApi.SortBy.values()[sortBy.getSelectedItemPosition()])
                .order(StackOverflowApi.OrderType.values()[order.getSelectedItemPosition()])
                .build()));
    }

    @AutoParcel
    public abstract static class StackOverflowQuery implements Parcelable {
        abstract public StackOverflowApi.OrderType order();

        abstract public StackOverflowApi.SortBy sort();

        abstract public String intitle();

        @AutoParcel.Builder
        public interface Builder {
            Builder order(StackOverflowApi.OrderType order);

            Builder sort(StackOverflowApi.SortBy sort);

            Builder intitle(String intitle);

            StackOverflowQuery build();
        }

        public static Builder builder() {
            return new AutoParcel_SearchFragment_StackOverflowQuery.Builder()
                    .order(StackOverflowApi.OrderType.DESC)
                    .sort(StackOverflowApi.SortBy.CREATION)
                    .intitle("");
        }

        public Map<String, String> getMappedQuery() {
            Map<String, String> result = Maps.newHashMap();
            result.put(StackOverflowApi.SEARCH_QUERY_ORDER, order().toString());
            result.put(StackOverflowApi.SEARCH_QUERY_SORT, sort().toString());
            result.put(StackOverflowApi.SEARCH_QUERY_INTITLE, intitle());
            return result;
        }
    }
}
