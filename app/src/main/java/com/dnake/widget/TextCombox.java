package com.dnake.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

@SuppressLint("HandlerLeak")
public class TextCombox extends TextBase {

	public TextCombox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void doKey(String key) {
		char k = key.charAt(0);
		if (k == '*') {
			if (mListener != null)
				mListener.onFinished(false);
			this.setFocus(FOCUS_ON);
		} else if (k == '#') {
			if (mListener != null)
				mListener.onFinished(true);
			this.setFocus(FOCUS_ON);
		} else if (k == '2' || k == '4' || k == 'B') {
			if (mSelect > 0) {
				mSelect--;
				super.setText(mText[mSelect]);
			}
		} else if (k == '8' || k == '6' || k == 'C') {
			if (mText != null) {
				if (mSelect+1 < mText.length) {
					mSelect++;
					super.setText(mText[mSelect]);
				}
			}
		}
	}

	public String mText[] = null;
	public void setTextArray(String s[]) {
		mText = s;
	}

	public int mSelect = 0;
	public void setSelect(int s) {
		if (mText == null)
			return;
		if (s >= mText.length)
			return;
		mSelect = s;
		super.setText(mText[s]);
	}
}
