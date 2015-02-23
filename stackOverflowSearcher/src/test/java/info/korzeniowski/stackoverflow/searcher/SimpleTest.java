package info.korzeniowski.stackoverflow.searcher;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;
import retrofit.Callback;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

@Config(emulateSdk = 18, reportSdk = 18)
@RunWith(RobolectricTestRunner.class)
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
        ((TestApp) Robolectric.application.getApplicationContext()).addModules(MockRetrofitModule.class);
        ((TestApp) Robolectric.application.getApplicationContext()).inject(this);

        activity = Robolectric.buildActivity(info.korzeniowski.stackoverflow.searcher.ui.MainActivity.class).create().start().resume().get();
        ButterKnife.inject(this, activity);
    }

    @Test
    public void shouldCallRestApiWithPassedText() {
        // given
        String queryString = "query string";
        query.setText(queryString);

        // when
        search.performClick();

        // then
        Mockito.verify(mockRestApi, times(1)).query(eq(queryString), any(Callback.class));
    }

    @Test
    public void shouldPopulateList() {
        // given
        final String queryString = "query string";
        query.setText(queryString);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Callback<StackOverflowApi.QueryResult> callback = (Callback<StackOverflowApi.QueryResult>) invocation.getArguments()[1];
                StackOverflowApi.QueryResult result = new StackOverflowApi.QueryResult();
                result.topics = Lists.newArrayList(new StackOverflowApi.Topic("Topic 1"), new StackOverflowApi.Topic("Topic 2"));
                callback.success(result, null);
                return null;
            }
        })
                .when(mockRestApi)
                .query(eq(queryString), any(Callback.class));

        // when
        search.performClick();

        // then
        assertThat(list).hasCount(2);
    }
}
