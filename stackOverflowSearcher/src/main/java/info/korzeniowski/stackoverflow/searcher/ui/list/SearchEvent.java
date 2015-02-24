package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.os.Parcelable;

import com.google.common.collect.Maps;

import java.util.Map;

import auto.parcel.AutoParcel;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;

public class SearchEvent {
    private StackOverflowQuery stackOverflowQuery;

    public SearchEvent(StackOverflowQuery stackOverflowQuery) {
        this.stackOverflowQuery = stackOverflowQuery;
    }

    public StackOverflowQuery getStackOverflowQuery() {
        return stackOverflowQuery;
    }

    @AutoParcel
    public static abstract class StackOverflowQuery implements Parcelable {
        abstract StackOverflowApi.OrderType order();
        abstract StackOverflowApi.SortBy sort();
        abstract String intitle();

        @AutoParcel.Builder
        public interface Builder {
            public Builder order(StackOverflowApi.OrderType order);
            public Builder sort(StackOverflowApi.SortBy sort);
            public Builder intitle(String intitle);
            public StackOverflowQuery build();
        }

        public static Builder builder() {
            return new AutoParcel_SearchEvent_StackOverflowQuery.Builder()
                    .order(StackOverflowApi.OrderType.DESC)
                    .sort(StackOverflowApi.SortBy.CREATION);
        }

        public Map<String, String> getMappedQuery() {
            Map<String, String> result = Maps.newHashMap();
            result.put("order", order().toString());
            result.put("sort", sort().toString());
            result.put("intitle", intitle());
            return result;
        }
    }
}
