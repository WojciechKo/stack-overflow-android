package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.stackoverflow.searcher.R;

import static info.korzeniowski.stackoverflow.searcher.util.Utils.dipToPixels;

public class QuestionListAdapter extends BaseAdapter {

    private Context context;
    private List<QuestionAdapterData> questions;

    public QuestionListAdapter(Context context, List<QuestionAdapterData> questions) {
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
        public static final Creator<QuestionAdapterData> CREATOR = new Creator<QuestionAdapterData>() {
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

        public QuestionAdapterData setVisited(Boolean visited) {
            this.visited = visited;
            return this;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public QuestionAdapterData setQuestionId(Long questionId) {
            this.questionId = questionId;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public QuestionAdapterData setTitle(String title) {
            this.title = title;
            return this;
        }

        public List<String> getTags() {
            return tags;
        }

        public QuestionAdapterData setTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public String getLink() {
            return link;
        }

        public QuestionAdapterData setLink(String link) {
            this.link = link;
            return this;
        }

        public String getOwnerProfileImageUrl() {
            return ownerProfileImageUrl;
        }

        public QuestionAdapterData setOwnerProfileImageUrl(String ownerProfileImageUrl) {
            this.ownerProfileImageUrl = ownerProfileImageUrl;
            return this;
        }

        public String getOwnerDisplayName() {
            return ownerDisplayName;
        }

        public QuestionAdapterData setOwnerDisplayName(String ownerDisplayName) {
            this.ownerDisplayName = ownerDisplayName;
            return this;
        }
    }
}
