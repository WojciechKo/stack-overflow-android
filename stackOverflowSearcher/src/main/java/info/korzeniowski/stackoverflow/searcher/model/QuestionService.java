package info.korzeniowski.stackoverflow.searcher.model;

import android.content.Context;

import io.realm.Realm;

public class QuestionService {
    private static Realm realm;

    public QuestionService(Context context) {
        if (realm == null) {
            realm = Realm.getInstance(context);
        }
    }

    public void close() {
        realm.close();
    }

    public void insert(Long questionId) {
        realm.beginTransaction();
        Question question = realm.createObject(Question.class);
        question.setQuestionId(questionId);
        realm.commitTransaction();
    }

    public io.realm.RealmQuery<Question> where() {
        return realm.where(Question.class);
    }
}
