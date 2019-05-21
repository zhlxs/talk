package com.dnake.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

@SuppressLint("HandlerLeak")
public class TextEdit extends TextBase {

	public TextEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
		for(int i=0; i<mIp.length; i++)
			mIp[i] = "";
	}

	public String mIp[] = new String[4];
	public int mIpIdx = 0;

	public int mMode = 0; //0: ÆÕÍ¨ÊäÈë  1: IPÊäÈë
	public void setMode(int mode) {
		mMode = mode;
	}

	public String mText = "";
	public int mMax = 0;

	public void setLength(int max) {
		mMax = max;
	}

	@Override
	public void doKey(String key) {
		char k = key.charAt(0);
		if (k == '*') {
			if (mListener != null)
				mListener.onFinished(false);
			this.setFocus(FOCUS_ON);
			mText = "";
			for(int i=0; i<mIp.length; i++)
				mIp[i] = "";
			mIpIdx = 0;
		} else if (k == '#') {
			if (mMode == 0) {
				if (mListener != null)
					mListener.onFinished(true);
				this.setFocus(FOCUS_ON);
				mText = "";
			} else if (mMode == 1) {
				if (mIpIdx < 3) {
					if (mIp[mIpIdx].length() > 0) {
						mIpIdx++;
						this.setIpText();
					}
				} else {
					if (mIp[mIpIdx].length() > 0) {
						if (mListener != null)
							mListener.onFinished(true);
						this.setFocus(FOCUS_ON);

						for(int i=0; i<mIp.length; i++)
							mIp[i] = "";
						mIpIdx = 0;
					}
				}
			}
		} else if (k >= '0' && k <= '9') {
			if (mMode == 0) {
				if (mText.length() < mMax) {
					mText += key;
					super.setText(mText);
				}
			} else if (mMode == 1) {
				String s = mIp[mIpIdx]+key;
				if (s.length() > 3 || Integer.parseInt(s) > 255) {
					if (mIpIdx < 3) {
						mIpIdx++;
						mIp[mIpIdx] = key;
					}
				} else {
					mIp[mIpIdx] += key;
				}
				this.setIpText();
			}
		}
	}

	private void setIpText() {
		String s = mIp[0];
		for(int i=1; i<=mIpIdx; i++) {
			s += ".";
			s += mIp[i];
		}
		super.setText(s);
	}
}
