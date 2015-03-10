package info.korzeniowski.stackoverflow.searcher.ui.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.stackoverflow.searcher.R;

public class QuestionListItemView extends FrameLayout {
    @InjectView(R.id.title)
    TextView title;

    @InjectView(R.id.vote)
    TextView vote;

    @InjectView(R.id.answer)
    TextView answer;

    @InjectView(R.id.views)
    TextView views;

    @InjectView(R.id.date)
    TextView date;

    @InjectView(R.id.tags)
    TextView tags;

    @InjectView(R.id.profileImage)
    ImageView profileImage;

    @InjectView(R.id.authorName)
    TextView authorName;

    private boolean answered;

    public QuestionListItemView(Context context) {
        super(context);
        addView(getQuestionListItemView(getContext()));
    }

    public QuestionListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(getQuestionListItemView(context));
    }

    public QuestionListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addView(getQuestionListItemView(context));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public QuestionListItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        addView(getQuestionListItemView(context));
    }

    private View getQuestionListItemView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.question_item, this, false);
        ButterKnife.inject(this, view);
        return view;
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setDate(Date date) {
        this.date.setText(getDateText(date));
    }

    private String getDateText(Date date) {
        return android.text.format.DateFormat.getTimeFormat(getContext()).format(date)
                + "\n"
                + android.text.format.DateFormat.getDateFormat(getContext()).format(date);
    }

    public void setAuthorName(String authorName) {
        this.authorName.setText(authorName);
    }

    public void setVote(Long vote) {
        this.vote.setText(vote.toString());
    }

    public void setAnswer(Long answer) {
        this.answer.setText(answer.toString());
        setAnswered(this.answered);
    }

    public void setAnswered(Boolean answered) {
        this.answered = answered;

        Editable editeableAnswer = this.answer.getEditableText();
        if (editeableAnswer != null) {
            if (this.answered) {
                String answer = this.answer.getText().toString();
                editeableAnswer.setSpan(new BackgroundColorSpan(Color.parseColor("#75845c")), 0, answer.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                editeableAnswer.setSpan(new ForegroundColorSpan(Color.WHITE), 0, answer.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                editeableAnswer.setSpan(new StyleSpan(Typeface.BOLD), 0, answer.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                editeableAnswer.clearSpans();
            }
            this.answer.setText(editeableAnswer);
        }
    }

    public void setVisited(Boolean visited) {
        this.title.setTypeface(null, visited ? Typeface.NORMAL : Typeface.BOLD);
    }

    public void setViews(Long views) {
        this.views.setText(views.toString());
    }

    public TextView getDate() {
        return date;
    }

    public void setTags(List<String> tags) {
        SpannableStringBuilder tagStringBuilder = new SpannableStringBuilder();
        for (String tag : tags) {
            tagStringBuilder.append(tag);
            tagStringBuilder.append(" ");
        }
        this.tags.setText(tagStringBuilder.toString());
    }

    public void setProfileImage(String profileImage) {
        Picasso.with(getContext()).load(profileImage).placeholder(R.drawable.ic_contact_picture).into(this.profileImage);
    }
}
