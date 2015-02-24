package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import info.korzeniowski.stackoverflow.searcher.App;
import info.korzeniowski.stackoverflow.searcher.R;
import info.korzeniowski.stackoverflow.searcher.model.Question;
import info.korzeniowski.stackoverflow.searcher.model.QuestionService;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;
import info.korzeniowski.stackoverflow.searcher.ui.details.DetailsActivity;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ListFragment extends Fragment {
    private static final String LIST_DATA = "LIST_DATA";
    private static final int timeoutMillisec = 8 * 1000;

    @InjectView(R.id.list)
    ListView list;

    @InjectView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @Inject
    StackOverflowApi stackOverflowApi;

    @Inject
    OkHttpClient okHttpClient;

    @Inject
    Bus bus;

    @Inject
    QuestionService questionService;

    private ListState.Builder listStateBuilder;

    public static ListFragment newInstance() {
        ListFragment fragment = new ListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getActivity().getApplication()).inject(this);
        okHttpClient.setReadTimeout(timeoutMillisec, TimeUnit.MILLISECONDS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.inject(this, view);
        bus.register(this);
        listStateBuilder = ListState.builder();
        if (savedInstanceState != null) {
            final ListState listState = savedInstanceState.getParcelable(LIST_DATA);
            listStateBuilder.query(listState.query()).results(listState.results());

            list.setAdapter(new QuestionListAdapter(getActivity(), listState.results()));

            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    stackOverflowApi.query(listState.query().getMappedQuery(), getUpdateListCallback());
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (swipeRefresh.isRefreshing()) {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                }
                            },
                            timeoutMillisec);
                }
            });

        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_DATA, listStateBuilder.build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        questionService.close();
    }

    @OnItemClick(R.id.list)
    public void onListItemClicked(int position) {
        QuestionListAdapter.QuestionAdapterData item = (QuestionListAdapter.QuestionAdapterData) list.getAdapter().getItem(position);
        questionService.insert(item.getQuestionId());

        item.setVisited(true);
        ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();

        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_URL, item.getLink());
        startActivity(intent);
    }

    @Subscribe
    public void onSearchEvent(final SearchEvent event) {
        if (Strings.isNullOrEmpty(event.getStackOverflowQuery().intitle())) {
            return;
        }

        swipeRefresh.setRefreshing(true);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stackOverflowApi.query(event.getStackOverflowQuery().getMappedQuery(), getUpdateListCallback());
                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (swipeRefresh.isRefreshing()) {
                                    swipeRefresh.setRefreshing(false);
                                }
                            }
                        },
                        timeoutMillisec);
            }
        });
        listStateBuilder.query(event.getStackOverflowQuery());

        stackOverflowApi.query(event.getStackOverflowQuery().getMappedQuery(), getUpdateListCallback());
    }

    private Callback<StackOverflowApi.QueryResult> getUpdateListCallback() {
        return new Callback<StackOverflowApi.QueryResult>() {
            @Override
            public void success(StackOverflowApi.QueryResult queryResult, Response response) {
                List<QuestionListAdapter.QuestionAdapterData> resultsData = Lists.newArrayList(Lists.transform(queryResult.getQuestions(), new Function<StackOverflowApi.Question, QuestionListAdapter.QuestionAdapterData>() {
                    @Override
                    public QuestionListAdapter.QuestionAdapterData apply(StackOverflowApi.Question input) {
                        QuestionListAdapter.QuestionAdapterData questionAdapterData = new QuestionListAdapter.QuestionAdapterData();
                        questionAdapterData.setQuestionId(input.getQuestionId());
                        questionAdapterData.setTitle(input.getTitle());
                        questionAdapterData.setTags(input.getTags());
                        questionAdapterData.setLink(input.getLink());
                        questionAdapterData.setOwnerDisplayName(input.getOwner().getDisplayName());
                        questionAdapterData.setOwnerProfileImageUrl(input.getOwner().getProfileImageUrl());

                        Question found = questionService.where().equalTo("questionId", input.getQuestionId()).findFirst();
                        questionAdapterData.setVisited(found != null);
                        return questionAdapterData;
                    }
                }));
                listStateBuilder.results(resultsData);

                list.setAdapter(new QuestionListAdapter(getActivity(), resultsData));
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        };
    }
}
