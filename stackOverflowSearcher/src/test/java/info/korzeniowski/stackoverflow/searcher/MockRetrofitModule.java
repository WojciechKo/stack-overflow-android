package info.korzeniowski.stackoverflow.searcher;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.stackoverflow.searcher.model.QuestionService;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowApi;

@Module(
        includes = info.korzeniowski.stackoverflow.searcher.MyModule.class,
        injects = {
                SimpleTest.class
        },
        overrides = true,
        complete = false,
        library = true
)
public class MockRetrofitModule {
    @Provides
    @Singleton
    StackOverflowApi provideMockRestApi() {
        return Mockito.mock(StackOverflowApi.class);
    }

    @Provides
    QuestionService provideMockQuestionService() {
        return Mockito.mock(QuestionService.class);
    }
}
