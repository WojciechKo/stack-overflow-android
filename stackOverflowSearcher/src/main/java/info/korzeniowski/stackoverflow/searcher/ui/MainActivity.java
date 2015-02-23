package info.korzeniowski.stackoverflow.searcher.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
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

import com.squareup.picasso.Picasso;

import java.util.List;

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

    @Inject
    StackOverflowApi stackOverflowApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        ((App) getApplication()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @OnClick(R.id.search)
    public void onSearchClicked() {
        if (TextUtils.isEmpty(query.getText())) {
            return;
        }
        stackOverflowApi.query(query.getText().toString(), getMyApiIndexCallback());
    }

    private Callback<StackOverflowApi.QueryResult> getMyApiIndexCallback() {
        return new Callback<StackOverflowApi.QueryResult>() {
            @Override
            public void success(StackOverflowApi.QueryResult queryResult, Response response) {
                list.setAdapter(new QuestionAdapter(MainActivity.this, queryResult.topics));
                ((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
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



