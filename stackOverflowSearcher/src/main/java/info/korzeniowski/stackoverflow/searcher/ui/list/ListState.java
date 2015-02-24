package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.os.Parcelable;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
abstract class ListState implements Parcelable {
    abstract SearchEvent.StackOverflowQuery query();
    abstract List<QuestionListAdapter.QuestionAdapterData> results();

    public static Builder builder() {
        return new AutoParcel_ListState.Builder();
    }

    @AutoParcel.Builder
    public interface Builder {
        public Builder query(SearchEvent.StackOverflowQuery query);
        public Builder results(List<QuestionListAdapter.QuestionAdapterData> results);
        public ListState build();
    }
}
