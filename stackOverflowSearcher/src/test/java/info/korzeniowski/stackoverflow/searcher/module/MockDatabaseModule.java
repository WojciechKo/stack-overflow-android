package info.korzeniowski.stackoverflow.searcher.module;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.stackoverflow.searcher.model.QuestionService;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;

@Module
public class MockDatabaseModule {
    @Provides
    QuestionService provideMockQuestionService() {
        return Mockito.mock(QuestionService.class);
    }
}
