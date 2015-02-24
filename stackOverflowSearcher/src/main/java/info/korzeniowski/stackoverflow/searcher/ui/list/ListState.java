package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.os.Parcelable;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
abstract class ListState implements Parcelable {
    abstract List<ListFragment.QuestionAdapter.QuestionAdapterData> adapterDatas();

    static ListState create(List<ListFragment.QuestionAdapter.QuestionAdapterData> adapterDatas) {
        return new AutoParcel_ListState(adapterDatas);
    }
}
