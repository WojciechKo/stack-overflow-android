package info.korzeniowski.stackoverflow.searcher;

import android.app.Activity;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

public class MyRobolectricTestRunner extends RobolectricTestRunner {
    public MyRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
        return new AndroidManifest(
                Fs.fileFromPath("src/main/AndroidManifest.xml"),
                Fs.fileFromPath("src/main/res"),
                Fs.fileFromPath("src/main/assets")) {

            @Override
            public int getTargetSdkVersion() {
                return 18;
            }

            @Override
            public String getThemeRef(Class<? extends Activity> activityClass) {
                return "@android:style/Theme.Holo.Light.NoActionBar";
            }
        };
    }
}