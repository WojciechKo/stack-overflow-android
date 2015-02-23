package info.korzeniowski.stackoverflow.searcher;

import com.google.common.collect.Iterables;

import java.util.List;

import dagger.ObjectGraph;

public class TestApp extends info.korzeniowski.stackoverflow.searcher.App {

    private List<Object> modules;

    @Override
    protected List<Object> getModules() {
        if (modules == null) {
            modules = super.getModules();
            modules.add(new MockRetrofitModule());
        }
        return modules;
    }

    public void addModules(Object module) {
        getModules().add(module);
        graph = ObjectGraph.create(getModules().toArray());
    }

    public void removeModule(final Class<?> moduleClass) {
        Iterables.removeIf(modules, new com.google.common.base.Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return input.getClass().equals(moduleClass);
            }
        });
        graph = ObjectGraph.create(getModules().toArray());
    }
}