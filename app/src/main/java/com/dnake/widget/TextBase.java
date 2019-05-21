package com.dnake.widget;

import java.util.LinkedList;
import java.util.Queue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
@SuppressWarnings("deprecation")
public class TextBase extends TextView {

	public TextBase(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected Handler mEvent = null;
	protected Queue<QueueObject> mQueue = new LinkedList<QueueObject>();
	protected Object mLock = new Object();

	public int mFoucs = FOCUS_NONE;
	public static final int FOCUS_NONE = 0;
	public static final int FOCUS_ON = 1;
	public static final int FOCUS_EDIT = 2;

	public static final int COLOR_FOCUS_ON = Color.rgb(0x88, 0x69, 0x35);
	public static final int COLOR_FOCUS_EDIT = Color.argb(0x30, 0x88, 0x69, 0x35);

	public class QueueObject {
		public QueueObject(String cmd, String value) {
			mCmd = cmd;
			mValue = value;
		}

		public String mCmd;
		public String mValue;
	}

	public interface OnFinishListener {
		public void onFinished(Boolean ok);
	}

	protected OnFinishListener mListener = null;

	public void setOnFinishListener(OnFinishListener listener) {
		mListener = listener;
	}

	public Boolean isEdit() {
		if (mFoucs == FOCUS_EDIT)
			return true;
		return false;
	}

	public void setFocus(int focus) {
		mFoucs = focus;
		synchronized (mLock) {
			String s = "clear";
			if (focus == 1)
				s = "on";
			else if (focus == 2)
				s = "edit";
			QueueObject obj = new QueueObject("focus", s);
			mQueue.offer(obj);
			if (mEvent != null)
				mEvent.sendMessage(mEvent.obtainMessage());
		}
	}

	private BitmapDrawable bmpFocusOn(int color) {
		Bitmap bmp = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
		int x = this.getWidth();
		int y = this.getHeight();
		for(int i=0; i<x; i++) {
			bmp.setPixel(i, 0, color);
			bmp.setPixel(i, 1, color);
			bmp.setPixel(i, y-2, color);
			bmp.setPixel(i, y-1, color);
		}
		for(int i=0; i<y; i++) {
			bmp.setPixel(0, i, color);
			bmp.setPixel(1, i, color);
			bmp.setPixel(x-2, i, color);
			bmp.setPixel(x-1, i, color);
		}

		bmp.setDensity(160);
		return (new BitmapDrawable(bmp));
	}

	private BitmapDrawable bmpFocusEdit(int color) {
		Bitmap bmp = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
		int x = this.getWidth();
		int y = this.getHeight();
		for(int i=0; i<x; i++) {
			for(int j=0; j<y; j++) {
				bmp.setPixel(i, j, color);
			}
		}

		bmp.setDensity(160);
		return (new BitmapDrawable(bmp));
	}

	private void process() {
		synchronized (mLock) {
			QueueObject obj;
			while ((obj=mQueue.poll()) != null) {
				if (obj.mCmd.equalsIgnoreCase("focus")) {
					if (obj.mValue.equalsIgnoreCase("on"))
						this.setBackground(this.bmpFocusOn(COLOR_FOCUS_ON));
					else if (obj.mValue.equalsIgnoreCase("edit"))
						this.setBackground(this.bmpFocusEdit(COLOR_FOCUS_EDIT));
					else
						this.setBackground(null);
				}
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mEvent == null) {
			mEvent = new Handler() {
				@Override
				public void handleMessage(Message m) {
					super.handleMessage(m);
					process();
				}
			};
			mEvent.sendMessage(mEvent.obtainMessage());
		}
	}

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
		}
	}
}
