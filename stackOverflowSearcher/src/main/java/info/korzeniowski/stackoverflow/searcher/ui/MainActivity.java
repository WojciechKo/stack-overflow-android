package info.korzeniowski.stackoverflow.searcher.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.korzeniowski.stackoverflow.searcher.App;
import info.korzeniowski.stackoverflow.searcher.R;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        ((App) getApplication()).inject(this);
        okHttpClient.setReadTimeout(timeoutMillisec, TimeUnit.MILLISECONDS);
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
                list.setAdapter(new QuestionAdapter(MainActivity.this, queryResult.topics));
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

    public static class QuestionAdapter extends BaseAdapter {

        private Context context;
        private List<StackOverflowApi.Topic> topics;

        public QuestionAdapter(Context context, List<StackOverflowApi.Topic> topics) {
            this.context = context;
            this.topics = topics;
        }

        @Override
        public int getCount() {
            return topics.size();
        }

        @Override
        public StackOverflowApi.Topic getItem(int position) {
            return topics.get(position);
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
            StackOverflowApi.Topic topic = getItem(position);
            holder.title.setText(Html.fromHtml(topic.title));
            holder.authorName.setText(topic.owner.displayName);
            Picasso.with(context).load(topic.owner.profileImageUrl).placeholder(R.drawable.ic_contact_picture).into(holder.profileImage);
            return convertView;
        }

        public static class ViewHolder {
            @InjectView(R.id.title)
            TextView title;

            @InjectView(R.id.profileImage)
            ImageView profileImage;

            @InjectView(R.id.authorName)
            TextView authorName;
        }
    }
}



