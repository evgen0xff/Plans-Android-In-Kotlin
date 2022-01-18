/*
 * Copyright (C) 2016 Borja Bravo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.planscollective.plansapp.customUI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.planscollective.plansapp.R;

public class ReadMoreTextView extends AppCompatTextView {

    public interface OnReadMoreListener {
        void onExpanded (View textView);
        void onCollapsed(View textView);
    }

    private static final int TRIM_MODE_LINES = 0;
    private static final int TRIM_MODE_LENGTH = 1;
    private static final int DEFAULT_TRIM_LENGTH = 240;
    private static final int DEFAULT_TRIM_LINES = 2;
    private static final int INVALID_END_INDEX = -1;
    private static final boolean DEFAULT_SHOW_TRIM_EXPANDED_TEXT = true;
    private static final boolean DEFAULT_ENABLE_EXPAND = true;
    private static final String ELLIPSIZE = "... ";

    private CharSequence text;
    private BufferType bufferType;
    private boolean readMore = true;
    private int trimLength;
    private CharSequence trimCollapsedText;
    private CharSequence trimExpandedText;
    private ReadMoreClickableSpan viewMoreSpan;
    private int colorClickableText;
    private boolean showTrimExpandedText;

    private int trimMode;
    private int lineEndIndex;
    private int trimLines;
    private OnReadMoreListener listenerReadMore = null;

    public boolean enableExpand = true;
    public boolean isShownReadMore = false;
    public boolean isShownReadLess = false;

    public void  setIsCollapsed(boolean newValue) {
        readMore = newValue;
    }

    public ReadMoreTextView(Context context) {
        this(context, null);
    }

    public ReadMoreTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReadMoreTextView);
        this.trimLength = typedArray.getInt(R.styleable.ReadMoreTextView_trimLength, DEFAULT_TRIM_LENGTH);
        int resourceIdTrimCollapsedText =
                typedArray.getResourceId(R.styleable.ReadMoreTextView_trimCollapsedText, R.string.see_more);
        int resourceIdTrimExpandedText =
                typedArray.getResourceId(R.styleable.ReadMoreTextView_trimExpandedText, R.string.see_less);
        this.trimCollapsedText = getResources().getString(resourceIdTrimCollapsedText);
        this.trimExpandedText = getResources().getString(resourceIdTrimExpandedText);
        this.trimLines = typedArray.getInt(R.styleable.ReadMoreTextView_trimLines, DEFAULT_TRIM_LINES);
        this.colorClickableText = typedArray.getColor(R.styleable.ReadMoreTextView_colorClickableText,
                ContextCompat.getColor(context, R.color.colorAccent));
        this.showTrimExpandedText =
                typedArray.getBoolean(R.styleable.ReadMoreTextView_showTrimExpandedText, DEFAULT_SHOW_TRIM_EXPANDED_TEXT);
        this.enableExpand =
                typedArray.getBoolean(R.styleable.ReadMoreTextView_enableExpand, DEFAULT_ENABLE_EXPAND);
        this.trimMode = typedArray.getInt(R.styleable.ReadMoreTextView_trimMode, TRIM_MODE_LINES);
        typedArray.recycle();

        viewMoreSpan = new ReadMoreClickableSpan();
        onGlobalLayoutLineEndIndex();
        setText();
    }

    public void setOnReadMoreListener(OnReadMoreListener l) {
        listenerReadMore = l;
    }

    private void setText() {
        super.setText(getDisplayableText(), bufferType);
        setMovementMethod(LinkMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);
    }

    private CharSequence getDisplayableText() {
        return getTrimmedText(text);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        updateMaxLines();
        super.setText(text, type);
        this.text = text;
        bufferType = type;
        adjustText();
    }

    private void updateMaxLines() {
        int value = Integer.MAX_VALUE;
        if (trimMode == TRIM_MODE_LINES && readMore && trimLines > 0) {
            value = trimLines;
        }
        setMaxLines(value);
    }

    private void adjustText() {
        refreshLineEndIndex();
        setText();
    }

    private CharSequence getTrimmedText(CharSequence text) {
        isShownReadMore = false;
        isShownReadLess = false;
        if (trimMode == TRIM_MODE_LENGTH) {
            if (text != null && text.length() > trimLength) {
                if (readMore) {
                    isShownReadMore = true;
                    return updateCollapsedText();
                } else {
                    isShownReadLess = true;
                    return updateExpandedText();
                }
            }
        }else if (trimMode == TRIM_MODE_LINES) {
            if (text != null && lineEndIndex > 0) {
                if (readMore) {
                    if (getLayout().getLineCount() > trimLines) {
                        isShownReadMore = true;
                        return updateCollapsedText();
                    }
                } else {
                    isShownReadLess = true;
                    return updateExpandedText();
                }
            }
        }
        return text;
    }

    private CharSequence updateCollapsedText() {
        Log.d("ReadMoreTextView", "updateCollapsedText()");

        int trimEndIndex = text.length();
        switch (trimMode) {
            case TRIM_MODE_LINES:
                trimEndIndex = lineEndIndex - (ELLIPSIZE.length() + trimCollapsedText.length() + 1);
                if (trimEndIndex < 0) {
                    trimEndIndex = trimLength + 1;
                }
                break;
            case TRIM_MODE_LENGTH:
                trimEndIndex = trimLength + 1;
                break;
        }
        SpannableStringBuilder s = new SpannableStringBuilder(text, 0, trimEndIndex)
                .append(ELLIPSIZE)
                .append(trimCollapsedText);
        return addClickableSpan(s, trimCollapsedText);
    }

    private CharSequence updateExpandedText() {
        Log.d("ReadMoreTextView", "updateExpandedText()");

        if (showTrimExpandedText) {
            boolean isReadLess = false;
            switch (trimMode) {
                case TRIM_MODE_LINES:
                    if (getLayout().getLineCount() >= trimLines) {
                        isReadLess = true;
                    }
                    break;
                case TRIM_MODE_LENGTH:
                    if (text.length() >= trimLength) {
                        isReadLess = true;
                    }
                    break;
            }
            if (isReadLess) {
                SpannableStringBuilder s = new SpannableStringBuilder(text, 0, text.length()).append(trimExpandedText);
                return addClickableSpan(s, trimExpandedText);
            }
        }
        return text;
    }

    private CharSequence addClickableSpan(SpannableStringBuilder s, CharSequence trimText) {
        s.setSpan(viewMoreSpan, s.length() - trimText.length(), s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public void setTrimLength(int trimLength) {
        this.trimLength = trimLength;
        setText();
    }

    public void setColorClickableText(int colorClickableText) {
        this.colorClickableText = colorClickableText;
    }

    public void setTrimCollapsedText(CharSequence trimCollapsedText) {
        this.trimCollapsedText = trimCollapsedText;
    }

    public void setTrimExpandedText(CharSequence trimExpandedText) {
        this.trimExpandedText = trimExpandedText;
    }

    public void setTrimMode(int trimMode) {
        this.trimMode = trimMode;
    }

    public void setTrimLines(int trimLines) {
        this.trimLines = trimLines;
    }

    private class ReadMoreClickableSpan extends ClickableSpan {
        @Override
        public void onClick(View View) {
            if (enableExpand) {
                readMore = !readMore;
                setText();
                if (listenerReadMore != null) {
                    if (readMore) {
                        listenerReadMore.onCollapsed(View);
                    }else {
                        listenerReadMore.onExpanded(View);
                    }
                }
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(colorClickableText);
        }
    }

    private void onGlobalLayoutLineEndIndex() {
        if (trimMode == TRIM_MODE_LINES) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (getMeasuredHeight() > 0 && getMeasuredWidth() > 0) {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        adjustText();
                    }
                }
            });
        }
    }

    private void refreshLineEndIndex() {
        Log.d("ReadMoreTextView", "refreshLineEndIndex()");
        try {
            if (trimLines == 0) {
                lineEndIndex = getLayout().getLineEnd(0);
            } else if (trimLines > 0 && getLineCount() >= trimLines) {
                lineEndIndex = getLayout().getLineEnd(trimLines - 1);
            } else {
                lineEndIndex = INVALID_END_INDEX;
            }
        } catch (Exception ignored) {
        }
    }
}
