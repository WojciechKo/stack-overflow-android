package info.korzeniowski.stackoverflow.searcher;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.collect.Lists;

import org.fest.assertions.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;
import info.korzeniowski.stackoverflow.searcher.ui.details.DetailsActivity;
import info.korzeniowski.stackoverflow.searcher.ui.list.MainActivity;
import info.korzeniowski.stackoverflow.searcher.ui.list.QuestionListAdapter;
import info.korzeniowski.stackoverflow.searcher.ui.list.SearchFragment;
import retrofit.Callback;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

@RunWith(MyRobolectricTestRunner.class)
public class SimpleTest {

    private Activity activity;

    @InjectView(R.id.query)
    EditText query;

    @InjectView(R.id.search)
    Button search;

    @InjectView(R.id.list)
    ListView list;

    @Inject
    StackOverflowApi mockRestApi;

    @Before
    public void setUp() {
        ((TestApp) RuntimeEnvironment.application).component().inject(this);

        activity = Robolectric.buildActivity(MainActivity.class).create().start().resume().get();
        ButterKnife.inject(this, activity);
    }

    @Test
    public void shouldPopulateListWithWithRestApiData() {
        // given
        String queryText = "simple query";
        query.setText(queryText);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Callback<StackOverflowApi.SearchResult> callback = (Callback<StackOverflowApi.SearchResult>) invocation.getArguments()[2];
                StackOverflowApi.SearchResult result = new StackOverflowApi.SearchResult();
                result.setQuestions(
                        Lists.newArrayList(
                                new StackOverflowApi.Question().setTitle("Topic 1"),
                                new StackOverflowApi.Question().setTitle("Topic 2")
                        )
                );
                callback.success(result, null);
                return null;
            }
        })
                .when(mockRestApi)
                .search(anyMapOf(String.class, String.class), any(Integer.class), Matchers.<Callback<StackOverflowApi.SearchResult>>any());

        // when
        search.performClick();

        // then
        ArgumentCaptor<Map> arg = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(mockRestApi, times(1)).search(arg.capture(), any(Integer.class), any(Callback.class));
        assertThat(arg.getValue()).contains(MapEntry.entry(StackOverflowApi.SEARCH_QUERY_INTITLE, queryText));

        assertThat(list).hasCount(2);
    }

    @Test
    public void shouldStartDetailsActivity() {
        // given
        List<QuestionListAdapter.QuestionAdapterData> questions =
                Lists.newArrayList(
                        new QuestionListAdapter.QuestionAdapterData().setTitle("Topic 1").setLink("http://top1"),
                        new QuestionListAdapter.QuestionAdapterData().setTitle("Topic 2").setLink("http://top2")
                );

        list.setAdapter(new QuestionListAdapter(activity, questions));

        // when
        int index = 1;
        Shadows.shadowOf(list).performItemClick(index);

        // then
        Intent expectedIntent = new Intent(activity, DetailsActivity.class);
        expectedIntent.putExtra(DetailsActivity.EXTRA_URL, questions.get(index).getLink());
        assertThat(Shadows.shadowOf(activity).getNextStartedActivity()).isEqualTo(expectedIntent);
    }

    @Test
    public void shouldBuildMappedQuery() {
        // given
        String intitle = "intitle query";
        StackOverflowApi.SortBy sort = StackOverflowApi.SortBy.CREATION;
        StackOverflowApi.OrderType order = StackOverflowApi.OrderType.ASC;

        SearchFragment.StackOverflowQuery.Builder builder = SearchFragment.StackOverflowQuery.builder()
                .order(order)
                .sort(sort)
                .intitle(intitle);

        // when
        SearchFragment.StackOverflowQuery query = builder.build();

        // then
        Map<String, String> mappedQuery = query.getMappedQuery();
        assertThat(mappedQuery.get(StackOverflowApi.SEARCH_QUERY_ORDER)).isEqualToIgnoringCase(order.toString());
        assertThat(mappedQuery.get(StackOverflowApi.SEARCH_QUERY_SORT)).isEqualToIgnoringCase(sort.toString());
        assertThat(mappedQuery.get(StackOverflowApi.SEARCH_QUERY_INTITLE)).isEqualToIgnoringCase(intitle);
    }
}
