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
    private static final String LIST_STATE = "LIST_STATE";

    @InjectView(R.id.query)
    EditText query;

    @Inject
    Bus bus;

    private ListState listState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        ((App) getApplication()).inject(this);
        listState = new ListState();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.listFragment, ListFragment.newInstance(listState))
                    .commit();
        } else {
            listState = savedInstanceState.getParcelable(LIST_STATE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_STATE, listState);
        super.onSaveInstanceState(outState);
    }

    @OnClick(R.id.search)
    public void onSearchClicked() {
        if (TextUtils.isEmpty(query.getText())) {
            query.setError(getString(R.string.queryIsRequired));
            return;
        }
        query.setError(null);

        listState.query = query.getText().toString();
        bus.post(new ListStateChanged());
    }
}



