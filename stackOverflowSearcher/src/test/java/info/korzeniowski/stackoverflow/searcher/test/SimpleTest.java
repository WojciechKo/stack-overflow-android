package info.korzeniowski.stackoverflow.searcher.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import info.korzeniowski.stackoverflow.searcher.MyRobolectricTestRunner;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;
import info.korzeniowski.stackoverflow.searcher.ui.list.SearchFragment;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MyRobolectricTestRunner.class)
public class SimpleTest {

    @Before
    public void setUp() {

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
