package com.dnake.panel;

import java.util.Date;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint({"DefaultLocale", "SimpleDateFormat", "NewApi"})
@SuppressWarnings("deprecation")
public class FaceLabel extends BaseLabel {
    public static FaceLabel mContext = null;
    public static long mTs = 0;

    public static boolean mFaceHave = false;
    public static int mFaceUid = -1;
    public static int mFaceSim;
    public static int mFaceBlack = 0;
    public static boolean mFaceCms = false;
    public static String mFaceUrl;
    public static Date mFaceTs;
    public static Boolean mStartVo = true;

    private long mTimeTs = 0;
    private long mResultTs = 0;
    private int mLcdHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face);

        onOsdStart();
    }

    public void onResultDisplay() {
        for (int i = 0; i < mOsdFace.length; i++) {
            mOsdFace[i].setImageBitmap(null);
            mOsdText[i].setText("");
        }
        for (int i = 0; i < mFaceData.length; i++) {
            if (mFaceData[i] != null) {
                int n = (mOsdFace.length - 1) - i;
                mOsdFace[n].setImageBitmap(mFaceData[i].bmp);
                mOsdText[n].setText(mFaceData[i].name + " - " + mFaceData[i].sim);
                if (mFaceData[i].black)
                    mOsdText[n].setTextColor(0xFFFF0000);
                else
                    mOsdText[n].setTextColor(0xFFFFFFFF);
            }
        }
    }

    @Override
    public void onTimer() {
        super.onTimer();

        if (Math.abs(System.currentTimeMillis() - mTimeTs) > 1000) {
            mTimeTs = System.currentTimeMillis();
            Date d = new Date();
            String s = String.format("%04d-%02d-%02d %02d:%02d:%02d", d.getYear() + 1900, d.getMonth() + 1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
            mOsdTs.setText(s);
        }

        if (Math.abs(System.currentTimeMillis() - mTs) < 1 * 60 * 1000) {
            WakeTask.acquire();
        }

        if (mLcdHeight != mOsdLayout.getHeight()) {
            mLcdHeight = mOsdLayout.getHeight();
            mStartVo = true;
        }

        if (mStartVo) {
            int w = mOsdLayout.getWidth();
            int h = mOsdLayout.getHeight();
            if (w > 16 && h > 16) {
                dmsg req = new dmsg();
                dxml p = new dxml();
                p.setInt("/params/x", 0);
                p.setInt("/params/y", 0);
                p.setInt("/params/w", w);
                p.setInt("/params/h", h);
                req.to("/face/osd", p.toString());
                mStartVo = false;
            }
        }
        if (mFaceHave) {
            String name = "";
            boolean black = false;

            if (mFaceCms) {
                name = String.valueOf(mFaceUid);
            } else {
                dxml p = new dxml();
                if (mFaceBlack != 0) {
                    p.load("/dnake/data/black/" + mFaceUid + ".xml");
                    black = true;
                } else {
                    p.load("/dnake/data/user/" + mFaceUid + ".xml");
                }
                name = p.getText("/sys/name");
            }

            FaceData d = new FaceData();
            d.name = name;
            d.bmp = BitmapFactory.decodeFile(mFaceUrl);
            d.sim = mFaceSim;
            d.black = black;
            for (int i = 0; i < (mFaceData.length - 1); i++) {
                mFaceData[i] = mFaceData[i + 1];
            }
            mFaceData[mFaceData.length - 1] = d;

            mResultName.setText("姓    名：" + name);
            mResultSim.setText("相似度：" + mFaceSim);
            if (black) {
                mResultStatus.setText("状    态：黑名单");
                mResultName.setTextColor(0xFFFF0000);
                mResultSim.setTextColor(0xFFFF0000);
                mResultStatus.setTextColor(0xFFFF0000);
            } else {
                mResultStatus.setText("状    态：正常");
                mResultName.setTextColor(0xFF000000);
                mResultSim.setTextColor(0xFF000000);
                mResultStatus.setTextColor(0xFF000000);
            }
            mResultBmp.setImageBitmap(d.bmp);
            mResultLayout.setVisibility(View.VISIBLE);
            mResultTs = System.currentTimeMillis();

            onResultDisplay();

            mFaceHave = false;
        }

        if (mResultTs != 0 && Math.abs(System.currentTimeMillis() - mResultTs) > 5 * 1000) {
            mResultTs = 0;
            mResultLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mContext = this;
        mStartVo = true;

        mOsdLayout.setVisibility(View.VISIBLE);
        mResultLayout.setVisibility(View.INVISIBLE);
        onResultDisplay();
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

    @Override
    public void onKey(String key) {
        super.onKey(key);
    }

    public class FaceData {
        public String name;
        //Bitmap位图包括像素以及长、宽、颜色等描述信息。
        // 长宽和像素位数是用来描述图片的，可以通过这些信息计算出图片的像素占用内存的大小。
        public Bitmap bmp;
        public int sim;
        public boolean black;
    }

    public static FaceData mFaceData[] = new FaceData[4];

    public static RelativeLayout mOsdLayout = null;
    public static TextView mOsdTs = null;
    public static ImageView mOsdFace[] = new ImageView[4];
    public static TextView mOsdText[] = new TextView[4];
    public static RelativeLayout mResultLayout = null;
    public static TextView mResultName;
    public static TextView mResultSim;
    public static TextView mResultStatus;
    public static ImageView mResultBmp;

    @SuppressLint("InflateParams")
    public void onOsdStart() {
        if (mOsdLayout != null)
            return;

        LayoutInflater inflater = LayoutInflater.from(this.getApplication());
        mOsdLayout = (RelativeLayout) inflater.inflate(R.layout.face_osd, null);

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

        mOsdTs = (TextView) mOsdLayout.findViewById(R.id.face_osd_ts);
        for (int i = 0; i < 4; i++) {
            mOsdFace[i] = (ImageView) mOsdLayout.findViewById(R.id.face_osd_f0 + i * 2);
            mOsdText[i] = (TextView) mOsdLayout.findViewById(R.id.face_osd_t0 + i * 2);
        }

        mResultLayout = (RelativeLayout) mOsdLayout.findViewById(R.id.face_osd_result);
        mResultBmp = (ImageView) mOsdLayout.findViewById(R.id.face_osd_result_p);
        mResultName = (TextView) mOsdLayout.findViewById(R.id.face_osd_result_name);
        mResultSim = (TextView) mOsdLayout.findViewById(R.id.face_osd_result_sim);
        mResultStatus = (TextView) mOsdLayout.findViewById(R.id.face_osd_result_status);
    }
}
