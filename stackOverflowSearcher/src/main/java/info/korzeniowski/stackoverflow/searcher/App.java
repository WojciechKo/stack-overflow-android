package info.korzeniowski.stackoverflow.searcher;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import info.korzeniowski.stackoverflow.searcher.model.DatabaseModule;
import info.korzeniowski.stackoverflow.searcher.rest.StackOverflowModule;
import info.korzeniowski.stackoverflow.searcher.ui.list.ListFragment;
import info.korzeniowski.stackoverflow.searcher.ui.list.MainActivity;
import info.korzeniowski.stackoverflow.searcher.ui.list.SearchFragment;

public class App extends Application {

    ApplicationComponent component;

    @Singleton
    @Component(
            modules = {
                    MainModule.class,
                    DatabaseModule.class,
                    HttpClientModule.class,
                    StackOverflowModule.class
            })
    public interface ApplicationComponentImpl extends ApplicationComponent {

    }

    public interface ApplicationComponent {
        void inject(MainActivity object);

        void inject(ListFragment object);

        void inject(SearchFragment object);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        component = Dagger_App_ApplicationComponentImpl.builder()
                .mainModule(new MainModule(this))
                .build();
    }

    public ApplicationComponent component() {
        return component;
    }
}

