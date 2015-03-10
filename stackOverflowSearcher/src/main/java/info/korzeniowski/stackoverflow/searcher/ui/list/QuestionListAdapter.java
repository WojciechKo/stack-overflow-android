package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        QuestionAdapterData item = getItem(position);
        QuestionListItemView view = (QuestionListItemView) convertView;
        if (view == null) {
            view = new QuestionListItemView(context);
        }

        view.setTitle(item.getTitle());
        view.setDate(item.getCreationDate());
        view.setAuthorName(item.getOwnerDisplayName());
        view.setVote(item.getVotes());
        view.setAnswer(item.getAnswers());
        view.setAnswered(item.getAnswered());
        view.setViews(item.getViews());
        view.setProfileImage(item.getOwnerProfileImageUrl());
        view.setTag(item.getTags());
        view.setVisited(item.getVisited());
        view.setTags(item.getTags());

        return view;
    }

    public static class QuestionAdapterData implements Parcelable {
        private Boolean visited;
        private Long questionId;
        private String title;
        private List<String> tags;
        private Long votes;
        private Long answers;
        private Long views;
        private Boolean answered;
        private Date creationDate;
        private String link;
        private String ownerProfileImageUrl;
        private String ownerDisplayName;

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
            votes = in.readByte() == 0x00 ? null : in.readLong();
            answers = in.readByte() == 0x00 ? null : in.readLong();
            views = in.readByte() == 0x00 ? null : in.readLong();
            byte answeredVal = in.readByte();
            answered = answeredVal == 0x02 ? null : answeredVal != 0x00;
            long tmpCreationDate = in.readLong();
            creationDate = tmpCreationDate != -1 ? new Date(tmpCreationDate) : null;
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
            if (votes == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeLong(votes);
            }
            if (answers == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeLong(answers);
            }
            if (views == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeLong(views);
            }
            if (answered == null) {
                dest.writeByte((byte) (0x02));
            } else {
                dest.writeByte((byte) (answered ? 0x01 : 0x00));
            }
            dest.writeLong(creationDate != null ? creationDate.getTime() : -1L);
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

        public Long getVotes() {
            return votes;
        }

        public QuestionAdapterData setVotes(Long votes) {
            this.votes = votes;
            return this;
        }

        public Long getAnswers() {
            return answers;
        }

        public QuestionAdapterData setAnswers(Long answers) {
            this.answers = answers;
            return this;
        }

        public Long getViews() {
            return views;
        }

        public QuestionAdapterData setViews(Long views) {
            this.views = views;
            return this;
        }

        public Boolean getAnswered() {
            return answered;
        }

        public QuestionAdapterData setAnswered(Boolean answered) {
            this.answered = answered;
            return this;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public QuestionAdapterData setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

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
            this.title = Html.fromHtml(title).toString();
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
