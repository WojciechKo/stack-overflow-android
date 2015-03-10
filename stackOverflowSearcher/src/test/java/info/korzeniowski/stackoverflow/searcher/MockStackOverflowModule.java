package info.korzeniowski.stackoverflow.searcher;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.stackoverflow.searcher.model.QuestionService;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;

@Module
public class MockStackOverflowModule {
    @Provides
    @Singleton
    StackOverflowApi provideMockRestApi() {
        return Mockito.mock(StackOverflowApi.class);
    }
}
