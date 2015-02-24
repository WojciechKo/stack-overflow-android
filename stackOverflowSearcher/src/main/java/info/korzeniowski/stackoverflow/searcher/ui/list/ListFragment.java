package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import auto.parcel.AutoParcel;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import info.korzeniowski.stackoverflow.searcher.App;
import info.korzeniowski.stackoverflow.searcher.R;
import info.korzeniowski.stackoverflow.searcher.model.Question;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;
import info.korzeniowski.stackoverflow.searcher.ui.details.DetailsActivity;
import io.realm.Realm;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static info.korzeniowski.stackoverflow.searcher.util.Utils.dipToPixels;

public class ListFragment extends Fragment {
    private static final String LIST_DATA = "LIST_DATA";
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

    private Realm realm;
    private ListState listState;

    public static ListFragment newInstance() {
        ListFragment fragment = new ListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getActivity().getApplication()).inject(this);
        realm = Realm.getInstance(getActivity());
        okHttpClient.setReadTimeout(timeoutMillisec, TimeUnit.MILLISECONDS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.inject(this, view);
        bus.register(this);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_DATA);
            list.setAdapter(new QuestionAdapter(getActivity(), listState.adapterDatas()));
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LIST_DATA, listState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        realm = null;
    }

    @OnItemClick(R.id.list)
    public void onListItemClicked(int position) {
        QuestionAdapter.QuestionAdapterData item = (QuestionAdapter.QuestionAdapterData) list.getAdapter().getItem(position);

        realm.beginTransaction();
        Question question = realm.createObject(Question.class);
        question.setQuestionId(item.getQuestionId());
        realm.commitTransaction();
        item.setVisited(true);

        ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();

        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_URL, item.getLink());

        startActivity(intent);
    }

    @Subscribe
    public void onSearchEvent(final SearchEvent event) {
        if (Strings.isNullOrEmpty(event.getQuery())) {
            return;
        }
        swipeRefresh.setRefreshing(true);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stackOverflowApi.query(event.getQuery(), getUpdateListCallback());
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
        });

        stackOverflowApi.query(event.getQuery(), getUpdateListCallback());
    }

    private Callback<StackOverflowApi.QueryResult> getUpdateListCallback() {
        return new Callback<StackOverflowApi.QueryResult>() {
            @Override
            public void success(StackOverflowApi.QueryResult queryResult, Response response) {
                List<QuestionAdapter.QuestionAdapterData> adapterDatas = Lists.newArrayList(Lists.transform(queryResult.getQuestions(), new Function<StackOverflowApi.Question, QuestionAdapter.QuestionAdapterData>() {
                    @Override
                    public QuestionAdapter.QuestionAdapterData apply(StackOverflowApi.Question input) {
                        QuestionAdapter.QuestionAdapterData questionAdapterData = new QuestionAdapter.QuestionAdapterData();
                        questionAdapterData.setQuestionId(input.getQuestionId());
                        questionAdapterData.setTitle(input.getTitle());
                        questionAdapterData.setTags(input.getTags());
                        questionAdapterData.setLink(input.getLink());
                        questionAdapterData.setOwnerDisplayName(input.getOwner().getDisplayName());
                        questionAdapterData.setOwnerProfileImageUrl(input.getOwner().getProfileImageUrl());

                        Question found = realm.where(Question.class).equalTo("questionId", input.getQuestionId()).findFirst();
                        questionAdapterData.setVisited(found != null);
                        return questionAdapterData;
                    }
                }));
                listState = ListState.create(adapterDatas);

                list.setAdapter(new QuestionAdapter(getActivity(), adapterDatas));
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        };
    }


    public static class QuestionAdapter extends BaseAdapter {

        private Context context;
        private List<QuestionAdapterData> questions;

        public QuestionAdapter(Context context, List<QuestionAdapterData> questions) {
            this.context = context;
            this.questions = questions;
        }

        @Override
        public int getCount() {
            return questions.size();
        }

        @Override
        public QuestionAdapterData getItem(int position) {
            return questions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.question_item, parent, false);
                holder = new ViewHolder();
                ButterKnife.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            QuestionAdapterData item = getItem(position);
            holder.title.setText(Html.fromHtml(item.getTitle()));
            holder.authorName.setText(item.getOwnerDisplayName());
            Picasso.with(context).load(item.getOwnerProfileImageUrl()).placeholder(R.drawable.ic_contact_picture).into(holder.profileImage);

            SpannableStringBuilder tagStringBuilder = new SpannableStringBuilder();
            for (String tag : item.getTags()) {
                ImageSpan imageSpan = new ImageSpan(getImageSpanForTag(tag));
                tagStringBuilder.append(tag);
                tagStringBuilder.setSpan(imageSpan, tagStringBuilder.length() - tag.length(), tagStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tagStringBuilder.append(" ");
            }
            holder.tags.setText(tagStringBuilder);
            holder.title.setTypeface(null, item.getVisited() ? Typeface.NORMAL : Typeface.BOLD);

            return convertView;
        }

        private BitmapDrawable getImageSpanForTag(String tagName) {
            // creating textview dynamically
            final TextView tv = new TextView(getContext());
            tv.setText(tagName);
            tv.setTextSize(35);
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.oval);
            drawable.setColorFilter(Color.parseColor("#e4edf4"), PorterDuff.Mode.SRC);
            tv.setBackground(drawable);
            tv.setTextColor(Color.parseColor("#3e6d8e"));
            tv.setPadding(dipToPixels(getContext(), 15), 0, dipToPixels(getContext(), 15), dipToPixels(getContext(), 1));

            // convert View to Drawable
            int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            tv.measure(spec, spec);
            tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
            Bitmap b = Bitmap.createBitmap(tv.getMeasuredWidth(), tv.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            c.translate(-tv.getScrollX(), -tv.getScrollY());
            tv.draw(c);
            tv.setDrawingCacheEnabled(true);
            Bitmap cacheBmp = tv.getDrawingCache();
            Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
            tv.destroyDrawingCache();

            BitmapDrawable bitmapDrawable = new BitmapDrawable(viewBmp);
            bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());

            return bitmapDrawable;
        }

        private Context getContext() {
            return context;
        }

        public static class ViewHolder {
            @InjectView(R.id.title)
            TextView title;

            @InjectView(R.id.tags)
            TextView tags;

            @InjectView(R.id.profileImage)
            ImageView profileImage;

            @InjectView(R.id.authorName)
            TextView authorName;
        }


        public static class QuestionAdapterData implements Parcelable {
            Boolean visited;
            Long questionId;
            String title;
            List<String> tags;
            String link;
            String ownerProfileImageUrl;
            String ownerDisplayName;

            public QuestionAdapterData() {

            }

            protected QuestionAdapterData(Parcel in) {
                byte visitedVal = in.readByte();
                visited = visitedVal == 0x02 ? null : visitedVal != 0x00;
                questionId = in.readByte() == 0x00 ? null : in.readLong();
                title = in.readString();
                if (in.readByte() == 0x01) {
                    tags = new ArrayList<String>();
                    in.readList(tags, String.class.getClassLoader());
                } else {
                    tags = null;
                }
                link = in.readString();
                ownerProfileImageUrl = in.readString();
                ownerDisplayName = in.readString();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                if (visited == null) {
                    dest.writeByte((byte) (0x02));
                } else {
                    dest.writeByte((byte) (visited ? 0x01 : 0x00));
                }
                if (questionId == null) {
                    dest.writeByte((byte) (0x00));
                } else {
                    dest.writeByte((byte) (0x01));
                    dest.writeLong(questionId);
                }
                dest.writeString(title);
                if (tags == null) {
                    dest.writeByte((byte) (0x00));
                } else {
                    dest.writeByte((byte) (0x01));
                    dest.writeList(tags);
                }
                dest.writeString(link);
                dest.writeString(ownerProfileImageUrl);
                dest.writeString(ownerDisplayName);
            }

            @SuppressWarnings("unused")
            public static final Parcelable.Creator<QuestionAdapterData> CREATOR = new Parcelable.Creator<QuestionAdapterData>() {
                @Override
                public QuestionAdapterData createFromParcel(Parcel in) {
                    return new QuestionAdapterData(in);
                }

                @Override
                public QuestionAdapterData[] newArray(int size) {
                    return new QuestionAdapterData[size];
                }
            };


            public Boolean getVisited() {
                return visited;
            }

            public void setVisited(Boolean visited) {
                this.visited = visited;
            }

            public Long getQuestionId() {
                return questionId;
            }

            public void setQuestionId(Long questionId) {
                this.questionId = questionId;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public List<String> getTags() {
                return tags;
            }

            public void setTags(List<String> tags) {
                this.tags = tags;
            }

            public String getLink() {
                return link;
            }

            public void setLink(String link) {
                this.link = link;
            }

            public String getOwnerProfileImageUrl() {
                return ownerProfileImageUrl;
            }

            public void setOwnerProfileImageUrl(String ownerProfileImageUrl) {
                this.ownerProfileImageUrl = ownerProfileImageUrl;
            }

            public String getOwnerDisplayName() {
                return ownerDisplayName;
            }

            public void setOwnerDisplayName(String ownerDisplayName) {
                this.ownerDisplayName = ownerDisplayName;
            }

        }
    }
}
