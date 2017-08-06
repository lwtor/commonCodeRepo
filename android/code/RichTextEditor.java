package com.aidrive.dingdong.util;

import android.content.Context;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import com.aidrive.dingdong.R;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lwtor on 2016/1/19.
 */
public class RichTextEditor {
    private static final String TAG = "RichTextEditor";

    private static final String MATCHER_REPLY = "@\\{\\\"uin\\\"\\:\\d{1,}\\," +
            "\\\"nick\\\"\\:\\\"(.*?)\\\"\\}";
    //    private static final String MATCHER_TAG = "\\[##\\](.*?)\\[\\/##\\]";
    private static final String MATCHER_TAG = "#[^#].*?#";
    private static final String MATCHER_FACE = "\\[em\\](e\\d{3})\\[\\/em\\]";

    private Context mContext;

    private TextView mTextView;
    private Pattern mReplyPattern;
    private Pattern mTagPattern;
    private Pattern mFacePattern;

    private int mHighLightColor;

    private StringBuilder mParseText;

    public RichTextEditor(Context context) {
        this.mContext = context;
        mHighLightColor = context.getResources().getColor(R.color.social_highLight_blue);
    }

    public RichTextEditor(Context context, TextView textView) {
        this.mContext = context;
        this.mTextView = textView;
    }

    public void setView(TextView textView) {
        this.mTextView = textView;
    }

    public void parse(TextView view, String text) {
        this.mTextView = view;
        parse(text);
    }

    public void parse(String text) {

        if (mTextView == null) {
            return;
        }
        if (mReplyPattern == null) {
            mReplyPattern = Pattern.compile(MATCHER_REPLY);
        }
        if (mTagPattern == null) {
            mTagPattern = Pattern.compile(MATCHER_TAG);
        }
        if (mFacePattern == null) {
            mFacePattern = Pattern.compile(MATCHER_FACE);
        }
        if (mParseText == null) {
            mParseText = new StringBuilder();
        }
        mParseText.delete(0, mParseText.length());
        mParseText.append(text);

        parseText();
    }

    private void parseText() {
        RichText reply = parseRichText(mReplyPattern);
        RichText tag = parseRichText(mTagPattern);
        RichText face = parseRichText(mFacePattern);

        //解析最先遇到的情况
        if (isMin(reply, tag, face)) {
            //解析回复
            setReply(reply);
        } else if (isMin(tag, face)) {
            //解析标签
            setTag(tag);
        } else if (face != null) {
            //解析表情
            setFace(face);
        } else {
            mTextView.append(mParseText.toString());
            String s = mTextView.getEditableText().toString();
            Log.i(TAG, "text:" + s);
            mParseText.delete(0, mParseText.length());
        }
        if (mParseText.length() > 0) {
            parseText();
        }
    }

    private RichText parseRichText(Pattern pattern) {
        RichText result = new RichText();
        Matcher matcher = pattern.matcher(mParseText.toString());
        if (matcher.find()) {
            Log.i(TAG, "1.group count:" + matcher.groupCount());
            result.text = matcher.group(0);
            Log.i(TAG, "2.group count:" + matcher.groupCount());
            if (matcher.groupCount() > 0) {
                result.key = matcher.group(1);
            }
            result.position = mParseText.indexOf(result.text);
            return result;
        }
        return null;
    }

    private void setReply(RichText richText) {
        String prefixText = mParseText.substring(0, richText.position);
        mTextView.append(prefixText);
        mTextView.append(mContext.getString(R.string.reply));
        mTextView.append(new Spanny/*("回复")
                .append*/("@", new ForegroundColorSpan(mHighLightColor)));
        parseNestFace(richText.key);
        mTextView.append("：");
        mParseText.replace(0, richText.position + richText.text.length(), "");
    }

    private void setTag(RichText richText) {
        String prefixText = mParseText.substring(0, richText.position);
        mTextView.append(prefixText);
        parseNestFace(richText.text);
        mParseText.replace(0, richText.position + richText.text.length(), "");
    }

    private void setFace(RichText richText) {
        String prefixText = mParseText.substring(0, richText.position);
        mTextView.append(prefixText);
        mTextView.append(new Spanny("image", new ImageSpan(mContext, R.drawable.e100)));
        mParseText.replace(0, richText.position + richText.text.length(), "");
    }

    public void parseNestFace(String text) {
        Matcher matcher = mFacePattern.matcher(text);
        int position = 0;
        int facePosition = 0;
        while (matcher.find()) {
            String mText = matcher.group(0);
            //todo 使用key获取指定的表情
            String key = matcher.group(1);
            facePosition = text.indexOf(mText, facePosition);
            if (position < facePosition) {
                mTextView.append(new Spanny(text.substring(position, facePosition),
                        new ForegroundColorSpan(mHighLightColor)));
            }
            mTextView.append(new Spanny("image", new ImageSpan(mContext, R.drawable.e100)));
            facePosition = position = (facePosition + mText.length());
        }
        if (position < text.length()) {
            Log.i(TAG, "add spanny");
            mTextView.append(new Spanny(text.substring(facePosition, text.length()),
                    new ForegroundColorSpan(mHighLightColor)));
        }
    }

    private boolean isMin(RichText compare, RichText... others) {
        if (compare == null) {
            return false;
        }
        for (RichText other : others) {
            if (other != null && other.position < compare.position) {
                return false;
            }
        }
        return true;
    }

    private class RichText {
        int position;
        String text;
        String key;
        String[] extra;

        @Override
        public String toString() {
            return "RichText{" +
                    "position=" + position +
                    ", text='" + text + '\'' +
                    ", key='" + key + '\'' +
                    ", extra=" + Arrays.toString(extra) +
                    '}';
        }
    }

}
