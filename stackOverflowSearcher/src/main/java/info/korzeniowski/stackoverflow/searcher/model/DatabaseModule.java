package info.korzeniowski.stackoverflow.searcher.model;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.stackoverflow.searcher.model.QuestionService;

@Module
public class DatabaseModule {
    @Provides
    QuestionService provideQuestionService(Context context) {
        return new QuestionService(context);
    }
}
