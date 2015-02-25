package info.korzeniowski.stackoverflow.searcher.rest;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;

public interface StackOverflowApi {

    @GET("/search?site=stackoverflow&pagesize=4")
    void query(@QueryMap Map<String, String> queryMap, @Query("page") int page, Callback<QueryResult> callback);

    public static class QueryResult {
        @SerializedName("items")
        private List<Question> questions;

        @SerializedName("has_more")
        private Boolean hasMore;

        public List<Question> getQuestions() {
            return questions;
        }

        public QueryResult setQuestions(List<Question> questions) {
            this.questions = questions;
            return this;
        }

        public Boolean getHasMore() {
            return hasMore;
        }

        public QueryResult setHasMore(Boolean hasMore) {
            this.hasMore = hasMore;
            return this;
        }
    }

    public static class Question {
        @SerializedName("question_id")
        private Long questionId;
        private String title;
        private String link;

        @SerializedName("score")
        private Long votes;

        @SerializedName("answer_count")
        private Long answers;

        @SerializedName("view_count")
        private Long views;

        @SerializedName("is_answered")
        private Boolean answered;

        @SerializedName("creation_date")
        private Date creationDate;
        private List<String> tags;
        private Owner owner;

        public Long getQuestionId() {
            return questionId;
        }

        public Question setQuestionId(Long questionId) {
            this.questionId = questionId;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Question setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getLink() {
            return link;
        }

        public Question setLink(String link) {
            this.link = link;
            return this;
        }

        public Long getVotes() {
            return votes;
        }

        public Question setVotes(Long votes) {
            this.votes = votes;
            return this;
        }

        public Long getAnswers() {
            return answers;
        }

        public Question setAnswers(Long answers) {
            this.answers = answers;
            return this;
        }

        public Long getViews() {
            return views;
        }

        public Question setViews(Long views) {
            this.views = views;
            return this;
        }

        public Boolean getAnswered() {
            return answered;
        }

        public Question setAnswered(Boolean answered) {
            this.answered = answered;
            return this;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public Question setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public List<String> getTags() {
            return tags;
        }

        public Question setTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Owner getOwner() {
            return owner;
        }

        public Question setOwner(Owner owner) {
            this.owner = owner;
            return this;
        }
    }

    public static class Owner {
        @SerializedName("profile_image")
        private String profileImageUrl;

        @SerializedName("display_name")
        private String displayName;

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public Owner setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Owner setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
    }

    enum SortBy {
        ACTIVITY,
        CREATION,
        VOTES,
        RELEVANCE
    }

    enum OrderType {
        ASC,
        DESC
    }
}
