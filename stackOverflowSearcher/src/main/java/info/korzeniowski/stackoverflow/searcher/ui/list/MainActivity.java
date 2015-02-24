package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.app.Activity;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
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

public class MainActivity extends Activity {

    @InjectView(R.id.query)
    EditText query;

    @InjectView(R.id.search)
    Button searchButton;

    @InjectView(R.id.list)
    ListView list;

    @InjectView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    @Inject
    StackOverflowApi stackOverflowApi;

    @Inject
    OkHttpClient okHttpClient;

    private static final int timeoutMillisec = 8 * 1000;

    private Realm realm;
    private QuestionAdapterData questionAdapterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        ((App) getApplication()).inject(this);
        okHttpClient.setReadTimeout(timeoutMillisec, TimeUnit.MILLISECONDS);
        realm = Realm.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @OnItemClick(R.id.list)
    public void onListItemClicked(int position) {
        QuestionAdapterData item = (QuestionAdapterData) list.getAdapter().getItem(position);

        realm.beginTransaction();
        Question question = realm.createObject(Question.class);
        question.setQuestionId(item.getQuestion().getQuestionId());
        realm.commitTransaction();
        item.setVisited(true);

        ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_URL, item.getQuestion().getLink());

        startActivity(intent);
    }

    @OnClick(R.id.search)
    public void onSearchClicked() {
        if (TextUtils.isEmpty(query.getText())) {
            query.setError(getString(R.string.queryIsRequired));
            return;
        }
        query.setError(null);
        swipeRefresh.setRefreshing(true);

        final String lastQuery = query.getText().toString();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stackOverflowApi.query(lastQuery, getUpdateListCallback());
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

        stackOverflowApi.query(lastQuery, getUpdateListCallback());
    }

    private Callback<StackOverflowApi.QueryResult> getUpdateListCallback() {
        return new Callback<StackOverflowApi.QueryResult>() {
            @Override
            public void success(StackOverflowApi.QueryResult queryResult, Response response) {
                List<QuestionAdapterData> adapterData = Lists.transform(queryResult.getQuestions(), new Function<StackOverflowApi.Question, QuestionAdapterData>() {
                    @Override
                    public QuestionAdapterData apply(StackOverflowApi.Question input) {
                        questionAdapterData = new QuestionAdapterData();
                        questionAdapterData.setQuestion(input);
                        Question found = realm.where(Question.class).equalTo("questionId", input.getQuestionId()).findFirst();
                        questionAdapterData.setVisited(found != null);
                        return questionAdapterData;
                    }
                });

                list.setAdapter(new QuestionAdapter(MainActivity.this, adapterData));
                ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        };
    }

    public static class QuestionAdapterData {
        private StackOverflowApi.Question question;
        private Boolean visited;

        public StackOverflowApi.Question getQuestion() {
            return question;
        }

        public void setQuestion(StackOverflowApi.Question question) {
            this.question = question;
        }

        public Boolean getVisited() {
            return visited;
        }

        public void setVisited(Boolean visited) {
            this.visited = visited;
        }
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
            StackOverflowApi.Question question = item.getQuestion();
            holder.title.setText(Html.fromHtml(question.getTitle()));
            holder.authorName.setText(question.getOwner().getDisplayName());
            Picasso.with(context).load(question.getOwner().getProfileImageUrl()).placeholder(R.drawable.ic_contact_picture).into(holder.profileImage);

            SpannableStringBuilder tagStringBuilder = new SpannableStringBuilder();
            for (String tag : question.getTags()) {
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
    }
}



