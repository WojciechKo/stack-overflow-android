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
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
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
import retrofit.mime.TypedByteArray;

public class ListFragment extends Fragment {
    private static final String LIST_DATA = "LIST_DATA";
    private static final String NEXT_PAGE = "NEXT_PAGE";
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

    private int nextPage;
    private List<QuestionListAdapter.QuestionAdapterData> questionList;
    private boolean isLoading = false;

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
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.inject(this, view);
        bus.register(this);
        listStateBuilder = ListState.builder();
        if (savedInstanceState != null) {
            final ListState listState = savedInstanceState.getParcelable(LIST_DATA);

            questionList = listState.results();
            list.setAdapter(new QuestionListAdapter(getActivity(), questionList));

            nextPage = savedInstanceState.getInt(NEXT_PAGE);
            if (nextPage > 0) {
                list.setOnScrollListener(createOnScrollListener(listState.query()));
            }

            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshList(listState.query());
                }
            });

            listStateBuilder.query(listState.query()).results(listState.results());
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_DATA, listStateBuilder.build());
        outState.putInt(NEXT_PAGE, nextPage);
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
                refreshList(event.getStackOverflowQuery());
            }
        });
        listStateBuilder.query(event.getStackOverflowQuery());
        refreshList(event.getStackOverflowQuery());
    }

    private void refreshList(final SearchEvent.StackOverflowQuery query) {
        nextPage = 1;
        stackOverflowApi.query(query.getMappedQuery(), nextPage++, new Callback<StackOverflowApi.QueryResult>() {
            @Override
            public void success(final StackOverflowApi.QueryResult queryResult, Response response) {
                questionList = getAdapterData(queryResult.getQuestions());
                listStateBuilder.results(questionList);

                list.setAdapter(new QuestionListAdapter(getActivity(), questionList));
                if (queryResult.getHasMore()) {
                    list.setOnScrollListener(createOnScrollListener(query));
                } else {
                    list.setOnScrollListener(null);
                    nextPage = -1;
                }
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void failure(RetrofitError error) {
                String json =  new String(((TypedByteArray)error.getResponse().getBody()).getBytes());
                String message = "";
                try {
                    StackOverflowApi.ErrorResponse response = new Gson().fromJson(json, StackOverflowApi.ErrorResponse.class);
                    message = response.errorMsg;
                } catch (JsonSyntaxException e) {
                    message = error.getMessage();
                }
                Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        });

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

    private void loadDataToList(final SearchEvent.StackOverflowQuery query) {
        if (isLoading) {
            return;
        }
        isLoading = true;
        stackOverflowApi.query(query.getMappedQuery(), nextPage++, new Callback<StackOverflowApi.QueryResult>() {
            @Override
            public void success(StackOverflowApi.QueryResult queryResult, Response response) {
                List<QuestionListAdapter.QuestionAdapterData> receivedQuestions = getAdapterData(queryResult.getQuestions());
                listStateBuilder.results(Lists.newArrayList(Iterables.concat(listStateBuilder.build().results(), receivedQuestions)));
                if (questionList == null) {
                    questionList = receivedQuestions;
                    list.setAdapter(new QuestionListAdapter(getActivity(), questionList));
                    if (queryResult.getHasMore()) {
                        list.setOnScrollListener(createOnScrollListener(query));
                    } else {
                        list.setOnScrollListener(null);
                        nextPage = -1;
                    }
                } else {
                    questionList.addAll(receivedQuestions);
                    ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
                }
                swipeRefresh.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
                isLoading = false;
            }
        });
    }

    private AbsListView.OnScrollListener createOnScrollListener(final SearchEvent.StackOverflowQuery query) {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getLastVisiblePosition() > view.getAdapter().getCount() * 0.7) {
                    loadDataToList(query);
                }
            }
        };
    }

    private ArrayList<QuestionListAdapter.QuestionAdapterData> getAdapterData(List<StackOverflowApi.Question> questions) {
        return Lists.newArrayList(Lists.transform(questions, new Function<StackOverflowApi.Question, QuestionListAdapter.QuestionAdapterData>() {
            @Override
            public QuestionListAdapter.QuestionAdapterData apply(StackOverflowApi.Question input) {
                QuestionListAdapter.QuestionAdapterData questionAdapterData = new QuestionListAdapter.QuestionAdapterData();
                questionAdapterData.setQuestionId(input.getQuestionId());
                questionAdapterData.setTitle(input.getTitle());
                questionAdapterData.setLink(input.getLink());
                questionAdapterData.setVotes(input.getVotes());
                questionAdapterData.setAnswers(input.getAnswers());
                questionAdapterData.setViews(input.getViews());
                questionAdapterData.setAnswered(input.getAnswered());
                questionAdapterData.setCreationDate(input.getCreationDate());
                questionAdapterData.setTags(input.getTags());
                questionAdapterData.setOwnerDisplayName(input.getOwner().getDisplayName());
                questionAdapterData.setOwnerProfileImageUrl(input.getOwner().getProfileImageUrl());

                Question found = questionService.where().equalTo("questionId", input.getQuestionId()).findFirst();
                questionAdapterData.setVisited(found != null);
                return questionAdapterData;
            }
        }));
    }
}
