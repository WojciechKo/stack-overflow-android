package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.os.Parcel;
import android.os.Parcelable;

public class ListState implements Parcelable {
    public String query;

    ListState() {

    }

    ListState(Parcel in) {
        query = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(query);
    }

    @SuppressWarnings("unused")
    public static final Creator<ListState> CREATOR = new Creator<ListState>() {
        @Override
        public ListState createFromParcel(Parcel in) {
            return new ListState(in);
        }

        @Override
        public ListState[] newArray(int size) {
            return new ListState[size];
        }
    };
}
