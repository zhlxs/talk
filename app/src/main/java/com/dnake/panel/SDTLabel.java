package com.dnake.panel;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.dnake.misc.SDTLogger;
import com.dnake.misc.Sound;
import com.dnake.misc.SysSpecial;
import com.dnake.misc.SysTalk;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.utils;
import com.dnake.widget.Button2;
import com.ivsign.android.IDCReader.IDCReaderSDK;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint({ "DefaultLocale", "CutPasteId", "SdCardPath", "NewApi" })
@SuppressWarnings("deprecation")
public class SDTLabel extends BaseLabel {
	public static SDTLabel mContext = null;
	public static Boolean mStartVo = true;
	public static long mTs = 0;

	private long mResultTs = 0;

	public static class Data {
		public String mName; // 姓名
		public String mSex; // 性别
		public String mNation; // 民族
		public String mBirthday; // 生日
		public String mAddress; // 户口所在地
		public String mDepart; // 发证机关
		public String mSTs; // 有效时间 开始
		public String mSTp; // 有效时间 结束
		public String mID; // 身份证号
		public String mUrl; // 照片
		public Bitmap mBmp;// 图片
	}

	public List<Data> mList = new LinkedList<Data>();

	public static class Result {
		public String mID;
		public int mData;
		public String mUrl;
	}

	private Handler mEvent = new Handler() {
		@Override
		public void handleMessage(Message m) {
			super.handleMessage(m);
			if (m.what == 1) {
				Data d = (Data) m.obj;
				loadSdtData(d);
			} else if (m.what == 2) {
				Result d = (Result) m.obj;
				loadResultData(d);
			}
		}
	};

	public void setData(Data d) {
		mEvent.sendMessage(mEvent.obtainMessage(1, d));
	}

	public void setResult(String id, int data, String url) {
		Result d = new Result();
		d.mID = id;
		d.mData = data;
		d.mUrl = url;
		mEvent.sendMessage(mEvent.obtainMessage(2, d));
	}

	private void loadSdtData(Data d) {
		if (d.mName == null || d.mID == null || d.mSex == null)
			return;

		mList.clear();

		mOsdName.setText(d.mName);
		mOsdId.setText(d.mID);

		d.mBmp = BitmapFactory.decodeFile(d.mUrl);
		if (d.mBmp != null)
			mOsdP1.setImageBitmap(d.mBmp);

		mOsdP2.setImageBitmap(null);
		mOsdResult.setVisibility(View.INVISIBLE);

		mOsdPrompt.setText("证件信息读取成功，请正视屏幕");

		mResultTs = 0;
		mTs = System.currentTimeMillis();
		mList.add(d);
		utils.buzzer(100);
	}

	private void loadResultData(Result r) {
		Date dt = new Date();
		int ts = (dt.getYear()+1900)*10000+(dt.getMonth()+1)*100+dt.getDate();
		boolean ok = false;
		for (int i = 0; i < mList.size(); i++) {
			Data d = mList.get(i);
			if (r.mID.equalsIgnoreCase(d.mID)) {
				if (ts > Integer.parseInt(d.mSTp)) {
					mOsdTitle.setText("证件已过期");
					mOsdPrompt.setText("证件已过期");
					Sound.play(Sound.sdt_invalid, false);
				} else {
					dmsg req = new dmsg();
					req.to("/face/unlock", null);

					Bitmap b = BitmapFactory.decodeFile(r.mUrl);
					if (b != null)
						mOsdP2.setImageBitmap(b);

					mOsdTitle.setText("证件对比通过");
					mOsdPrompt.setText("证件对比通过");
					Sound.play(Sound.sdt_success, false);

					dxml p = new dxml();
					p.setText("/sys/name", d.mName);
					p.setText("/sys/sex", d.mSex);
					p.setText("/sys/nation", d.mNation);
					p.setText("/sys/birthday", d.mBirthday);
					p.setText("/sys/address", d.mAddress);
					p.setText("/sys/depart", d.mDepart);
					p.setText("/sys/ts", d.mSTs);
					p.setText("/sys/tp", d.mSTp);
					p.setText("/sys/id", d.mID);
					SDTLogger.insert(p, d.mBmp, b);
				}
				mOsdResult.setVisibility(View.VISIBLE);
				mResultTs = System.currentTimeMillis();
				ok = true;
				break;
			}
		}
		if (ok)
			mList.clear();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sdt);

		onOsdStart();
	}

	private long mTimeTs = 0;
	private int mLcdHeight = 0;

	@Override
	public void onTimer() {
		super.onTimer();

		if (Math.abs(System.currentTimeMillis() - mTimeTs) >= 1000) {
			mTimeTs = System.currentTimeMillis();
			Date d = new Date();
			String s = String.format("%04d-%02d-%02d %02d:%02d:%02d", d.getYear() + 1900, d.getMonth() + 1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
			mOsdTs.setText(s);
		}

		if (this.getWindow().getDecorView().getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
			this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}

		if (mLcdHeight != mOsdLayout.getHeight()) {
			mLcdHeight = mOsdLayout.getHeight();
			mStartVo = true;
		}
		if (mStartVo) {
			int w = mOsdLayout.getWidth();
			int h = mOsdLayout.getHeight();
			dmsg req = new dmsg();
			dxml p = new dxml();
			p.setInt("/params/x", 0);
			p.setInt("/params/y", 0);
			p.setInt("/params/w", w);
			p.setInt("/params/h", h);
			req.to("/face/osd", p.toString());
			mStartVo = false;
		}

		if (Math.abs(System.currentTimeMillis() - mTs) < 2 * 60 * 1000) {
			WakeTask.acquire();
		}

		if (Math.abs(System.currentTimeMillis() - mTs) > 3 * 1000) {
			if (mList.size() > 0) {
				mList.clear();

				mOsdTitle.setText("证件对比失败");
				mOsdPrompt.setText("证件对比失败");
				mOsdResult.setVisibility(View.VISIBLE);
				mResultTs = System.currentTimeMillis();
				Sound.play(Sound.sdt_failed, false);

				dmsg req = new dmsg();
				req.to("/face/sdt/reset", null);
			}
		}
		if (mResultTs != 0 && Math.abs(System.currentTimeMillis() - mResultTs) > 3 * 1000) {
			this.reset();
			mOsdPrompt.setText("请将证件放到读卡器上");
		}
	}

	private void reset() {
		mResultTs = 0;
		mList.clear();

		mOsdP1.setImageBitmap(null);
		mOsdP2.setImageBitmap(null);
		mOsdName.setText("");
		mOsdId.setText("");
		mOsdResult.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onStart() {
		super.onStart();
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		mContext = this;
		mStartVo = true;
		mOsdResult.setVisibility(View.INVISIBLE);
		mOsdLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		mContext = this;
		mStartVo = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		mContext = null;
		mOsdLayout.setVisibility(View.INVISIBLE);

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/x", 0);
		p.setInt("/params/y", 0);
		p.setInt("/params/w", 0);
		p.setInt("/params/h", 0);
		req.to("/face/osd", p.toString());
	}

	public static SdtThread mSdtThread = null;

	public static class SdtThread implements Runnable {
		public String body;
		@Override
		public void run() {
			dxml p = new dxml();
			p.parse(body);
			String url = p.getText("/params/url");
			if (IDCReaderSDK.doBmp(url)) {
				Runtime rt = Runtime.getRuntime();
				try {
					rt.exec("chmod 0777 /var/wltlib/zp.bmp").waitFor();
				} catch (Exception e) {
				}

				if (SysSpecial.sedireco != 0) {
					p.setText("/params/bmp", "/var/wltlib/zp.bmp");
					dmsg req = new dmsg();
					req.to("/exApp/cid/result", p.toString());
					return;
				}

				if (SDTLabel.mContext == null) {
					Intent it = new Intent(SysTalk.mContext, SDTLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SysTalk.mContext.startActivity(it);
				}

				SDTLabel.Data d = new SDTLabel.Data();
				d.mName = p.getText("/params/name");
				d.mSex = p.getText("/params/sex");
				d.mNation = p.getText("/params/nation");
				d.mBirthday = p.getText("/params/birthday");
				d.mAddress = p.getText("/params/address");
				d.mDepart = p.getText("/params/depart");
				d.mSTs = p.getText("/params/ts");
				d.mSTp = p.getText("/params/tp");
				d.mID = p.getText("/params/id");
				d.mUrl = "/var/wltlib/zp.bmp";

				boolean ok = false;
				for(int i=0; i<10; i++) {
					if (SDTLabel.mContext != null) {
						SDTLabel.mContext.setData(d);
						ok = true;
						break;
					}
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
				if (!ok)
					return;

				dxml p2 = new dxml();
				dmsg req = new dmsg();
				p2.setText("/params/url", "/var/wltlib/zp.bmp");
				p2.setText("/params/id", d.mID);
				req.to("/face/sdt/start", p2.toString());
			}
			mSdtThread = null;
		}
	}

	public static RelativeLayout mOsdLayout = null;
	public static RelativeLayout mOsdResult = null;
	public static TextView mOsdTs = null;
	public static TextView mOsdPrompt = null;
	public static TextView mOsdTitle = null;
	public static TextView mOsdId = null;
	public static TextView mOsdName = null;
	public static ImageView mOsdP1 = null;
	public static ImageView mOsdP2 = null;

	@SuppressLint("InflateParams")
	public void onOsdStart() {
		if (mOsdLayout != null)
			return;

		LayoutInflater inflater = LayoutInflater.from(this.getApplication());
		mOsdLayout = (RelativeLayout) inflater.inflate(R.layout.sdt_osd, null);

		WindowManager.LayoutParams p = new WindowManager.LayoutParams();
		p.type = WindowManager.LayoutParams.TYPE_PHONE;
		p.format = PixelFormat.RGBA_8888;
		p.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		p.x = 0;
		p.y = 0;
		p.width = WindowManager.LayoutParams.MATCH_PARENT;
		p.height = WindowManager.LayoutParams.MATCH_PARENT;
		WindowManager wm = (WindowManager) this.getApplication().getSystemService(Application.WINDOW_SERVICE);
		wm.addView(mOsdLayout, p);
		mOsdLayout.setVisibility(View.INVISIBLE);

		mOsdResult = (RelativeLayout)mOsdLayout.findViewById(R.id.sdt_osd_result);
		mOsdResult.setVisibility(View.INVISIBLE);

		mOsdTs = (TextView)mOsdLayout.findViewById(R.id.sdt_osd_ts);
		mOsdPrompt = (TextView)mOsdLayout.findViewById(R.id.sdt_osd_prompt);
		mOsdTitle = (TextView)mOsdLayout.findViewById(R.id.sdt_osd_result_title);

		mOsdName = (TextView)mOsdLayout.findViewById(R.id.sdt_osd_result_name);
		mOsdId = (TextView)mOsdLayout.findViewById(R.id.sdt_osd_result_id);
		mOsdP1 = (ImageView)mOsdLayout.findViewById(R.id.sdt_osd_result_p1);
		mOsdP2 = (ImageView)mOsdLayout.findViewById(R.id.sdt_osd_result_p2);

		Button2 b = (Button2) mOsdLayout.findViewById(R.id.sdt_osd_back);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (SDTLabel.mContext != null && !SDTLabel.mContext.isFinishing())
					SDTLabel.mContext.finish();
			}
		});
	}
}
