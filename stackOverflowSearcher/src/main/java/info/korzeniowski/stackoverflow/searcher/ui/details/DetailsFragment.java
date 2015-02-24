package info.korzeniowski.stackoverflow.searcher.ui.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.stackoverflow.searcher.R;

public class DetailsFragment extends Fragment {
    private static final String ARGUMENT_URL = "ARGUMENT_URL";

    @InjectView(R.id.webView)
    WebView webView;

    public static DetailsFragment newInstance(String url) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle argument = new Bundle();
        argument.putString(ARGUMENT_URL, url);
        fragment.setArguments(argument);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.inject(this, view);
        webView.loadUrl(getArguments().getString(ARGUMENT_URL));
        return view;
    }
}
