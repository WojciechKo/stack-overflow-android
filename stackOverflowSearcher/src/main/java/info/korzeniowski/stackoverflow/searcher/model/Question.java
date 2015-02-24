package info.korzeniowski.stackoverflow.searcher.model;

import io.realm.RealmObject;

public class Question extends RealmObject {
    private long questionId;

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }
}
