package info.korzeniowski.stackoverflow.searcher.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import info.korzeniowski.stackoverflow.searcher.R;

import static info.korzeniowski.stackoverflow.searcher.util.Utils.dipToPixels;

public class TagsView extends TextView {

    public static final String TAG_BACKGROUND_COLOR = "#e4edf4";
    private static final String TAG_TEXT_COLOR = "#3e6d8e";

    public TagsView(Context context) {
        super(context);
    }

    public TagsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        int start = 0;
        for (int i = 0; i < getEditableText().length(); i++) {
            if (getEditableText().charAt(i) == ' ') {
                if (i - 1 >= start) {
                    String tagName = getText().subSequence(start, i).toString();
                    ssb.append(tagName);
                    ImageSpan imageSpan = new ImageSpan(createTagDrawable(tagName));
                    ssb.setSpan(imageSpan, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                start = i + 1;
            }
        }

        super.setText(ssb, type);
    }

    private BitmapDrawable createTagDrawable(String tagName) {
        // Create TextView with Background
        final TextView tv = new TextView(getContext());
        tv.setText(tagName);
        tv.setTextSize(getTextSize());
        Drawable drawable = getResources().getDrawable(R.drawable.oval);
        drawable.setColorFilter(Color.parseColor(TAG_BACKGROUND_COLOR), PorterDuff.Mode.SRC);
        tv.setBackground(drawable);
        tv.setTextColor(Color.parseColor(TAG_TEXT_COLOR));
        tv.setPadding(dipToPixels(getContext(), 15), 0, dipToPixels(getContext(), 15), dipToPixels(getContext(), 1));

        // Convert View to Drawable
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv.measure(spec, spec);
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(tv.getMeasuredWidth(), tv.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-tv.getScrollX(), -tv.getScrollY());
        tv.draw(c);
        tv.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = tv.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        tv.destroyDrawingCache();

        BitmapDrawable bitmapDrawable = new BitmapDrawable(viewBmp);
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());

        return bitmapDrawable;
    }
}
