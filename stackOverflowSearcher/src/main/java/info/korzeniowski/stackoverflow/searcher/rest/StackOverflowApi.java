package info.korzeniowski.stackoverflow.searcher.rest;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;

import static com.google.common.base.Preconditions.checkNotNull;

public interface StackOverflowApi {
    String SEARCH_QUERY_ORDER = "order";
    String SEARCH_QUERY_SORT = "sort";
    String SEARCH_QUERY_INTITLE = "intitle";
    String SEARCH_QUERY_PAGE = "page";

    @GET("/search?site=stackoverflow&pagesize=20")
    void search(@QueryMap Map<String, String> queryMap, @Query(SEARCH_QUERY_PAGE) int page, Callback<SearchResult> callback);

    class SearchResult {
        public static final String ITEMS = "items";
        public static final String HAS_MORE = "has_more";

        @SerializedName(ITEMS)
        private List<Question> questions = Lists.newArrayList();

        @SerializedName(HAS_MORE)
        private Boolean hasMore = false;

        public List<Question> getQuestions() {
            return questions;
        }

        public SearchResult setQuestions(List<Question> questions) {
            checkNotNull(questions);

            this.questions = questions;
            return this;
        }

        public Boolean getHasMore() {
            return hasMore;
        }

        public SearchResult setHasMore(Boolean hasMore) {
            checkNotNull(hasMore);

            this.hasMore = hasMore;
            return this;
        }
    }

    class Question {
        public static final String QUESTION_ID = "question_id";
        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String SCORE = "score";
        public static final String ANSWER_COUNT = "answer_count";
        public static final String VIEW_COUNT = "view_count";
        public static final String IS_ANSWERED = "is_answered";
        public static final String CREATION_DATE = "creation_date";
        public static final String TAGS = "tags";
        public static final String OWNER = "owner";

        @SerializedName(QUESTION_ID)
        private Long questionId;

        @SerializedName(TITLE)
        private String title;

        @SerializedName(LINK)
        private String link;

        @SerializedName(SCORE)
        private Long votes;

        @SerializedName(ANSWER_COUNT)
        private Long answers;

        @SerializedName(VIEW_COUNT)
        private Long views;

        @SerializedName(IS_ANSWERED)
        private Boolean answered;

        @SerializedName(CREATION_DATE)
        private Date creationDate;

        @SerializedName(TAGS)
        private List<String> tags;

        @SerializedName(OWNER)
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

    class Owner {
        public static final String PROFILE_IMAGE = "profile_image";
        public static final String DISPLAY_NAME = "display_name";

        @SerializedName(PROFILE_IMAGE)
        private String profileImageUrl;

        @SerializedName(DISPLAY_NAME)
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

    class ErrorResponse {
        public static final String ERROR_ID = "error_id";
        public static final String ERROR_MESSAGE = "error_message";
        public static final String ERROR_NAME = "error_name";

        @SerializedName(ERROR_ID)
        public String errorId;

        @SerializedName(ERROR_MESSAGE)
        public String errorMsg;

        @SerializedName(ERROR_NAME)
        public String errorName;
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
