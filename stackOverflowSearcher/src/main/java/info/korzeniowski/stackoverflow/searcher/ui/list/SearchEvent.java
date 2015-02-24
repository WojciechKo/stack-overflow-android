package info.korzeniowski.stackoverflow.searcher.ui.list;

class SearchEvent {
    private String query;

    public SearchEvent(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
