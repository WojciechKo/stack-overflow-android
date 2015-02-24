package info.korzeniowski.stackoverflow.searcher.rest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface StackOverflowApi {

    @GET("/search?site=stackoverflow")
    void query(@Query("intitle") String intitle, Callback<QueryResult> callback);

    public static class QueryResult {
        @SerializedName("items")
        private List<Question> questions;

        public List<Question> getQuestions() {
            return questions;
        }

        public QueryResult setQuestions(List<Question> questions) {
            this.questions = questions;
            return this;
        }
    }

    public static class Question {
        @SerializedName("question_id")
        private Long questionId;
        private String title;
        private List<String> tags;
        private Owner owner;
        private String link;

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

        public String getLink() {
            return link;
        }

        public Question setLink(String link) {
            this.link = link;
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
}
