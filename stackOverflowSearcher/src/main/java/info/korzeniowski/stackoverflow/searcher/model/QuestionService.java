package info.korzeniowski.stackoverflow.searcher.model;

import android.content.Context;

import io.realm.Realm;

public class QuestionService {
    private static Realm realm;

    public static QuestionService get(Context context) {
        realm = Realm.getInstance(context);
        return new QuestionService(realm);
    }

    private QuestionService(Realm realm) {
        this.realm = realm;
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
