package info.korzeniowski.stackoverflow.searcher.ui.list;

public class SearchEvent {
    private SearchFragment.StackOverflowQuery stackOverflowQuery;

    public SearchEvent(SearchFragment.StackOverflowQuery stackOverflowQuery) {
        this.stackOverflowQuery = stackOverflowQuery;
    }

    public SearchFragment.StackOverflowQuery getStackOverflowQuery() {
        return stackOverflowQuery;
    }
}
