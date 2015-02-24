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
        private List<Topic> topics;

        public List<Topic> getTopics() {
            return topics;
        }

        public QueryResult setTopics(List<Topic> topics) {
            this.topics = topics;
            return this;
        }
    }

    public static class Topic {
        private String title;
        private List<String> tags;
        private Owner owner;
        private String link;

        public String getTitle() {
            return title;
        }

        public Topic setTitle(String title) {
            this.title = title;
            return this;
        }

        public List<String> getTags() {
            return tags;
        }

        public Topic setTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Owner getOwner() {
            return owner;
        }

        public Topic setOwner(Owner owner) {
            this.owner = owner;
            return this;
        }

        public String getLink() {
            return link;
        }

        public Topic setLink(String link) {
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
