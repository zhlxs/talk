package com.dnake.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

@SuppressLint({"DefaultLocale", "SdCardPath"})
@SuppressWarnings("deprecation")
public class SDTLogger {
    public static int MAX = 10000;
    public static long mData[] = new long[MAX];
    public static int mIdx = 0;

    public static class Data {
        public String id;
        public String name;
        public String sex;
        public String nation;
        public String birthday;
        public String address;
        public String depart;
        public String ts;
        public String tp;
        public String photo;
        public String camera;
        public String dt;
    }

    public static Data load(long idx) {
        String s = String.valueOf(idx);
        Data d = new Data();
        dxml p = new dxml();
        p.load("/sdcard/sdt/" + s + ".xml");
        d.id = p.getText("/sys/id");
        d.name = p.getText("/sys/name");
        d.sex = p.getText("/sys/sex");
        d.nation = p.getText("/sys/nation");
        d.birthday = p.getText("/sys/birthday");
        d.address = p.getText("/sys/address");
        d.depart = p.getText("/sys/depart");
        d.ts = p.getText("/sys/ts");
        d.tp = p.getText("/sys/tp");
        d.photo = "/sdcard/sdt/" + s + "_0.jpg";
        d.camera = "/sdcard/sdt/" + s + "_1.jpg";
        d.dt = s.substring(0, 4) + "-" + s.substring(4, 6) + "-" + s.substring(6, 8) + " " + s.substring(8, 10) + ":" + s.substring(10, 12) + ":" + s.substring(12, 14);
        return d;
    }

    public static void load() {
        File f = new File("/sdcard/sdt");
        if (f != null) {
            if (!f.exists())
                f.mkdir();
            File[] fs = f.listFiles();
            if (fs != null) {
                for (int i = 0; i < fs.length; i++) {
                    if (!fs[i].isDirectory()) {
                        int e = fs[i].getName().indexOf(".xml");
                        if (e > 0) {
                            String s = fs[i].getName().substring(0, e);
                            mData[mIdx] = Long.parseLong(s);
                            mIdx++;
                            if (mIdx >= MAX)
                                break;
                        }
                    }
                }
                Arrays.sort(mData, 0, mIdx);
            }
        }
    }

    public static void insert(dxml p, Bitmap b1, Bitmap b2) {
        Date d = new Date();
        String id = String.format("%04d%02d%02d%02d%02d%02d", d.getYear() + 1900, d.getMonth() + 1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
        String url = "/sdcard/sdt/" + id + ".xml";
        p.save(url);
        saveBitmap(b1, "/sdcard/sdt/" + id + "_0.jpg");
        saveBitmap(b2, "/sdcard/sdt/" + id + "_1.jpg");
        if (mIdx >= MAX) {
            File f = new File("/sdcard/sdt/" + mData[0] + ".xml");
            if (f != null && f.exists())
                f.delete();
            f = new File("/sdcard/sdt/" + mData[0] + "_0.jpg");
            if (f != null && f.exists())
                f.delete();
            f = new File("/sdcard/sdt/" + mData[0] + "_1.jpg");
            if (f != null && f.exists())
                f.delete();

            for (int i = 0; i < (mIdx - 1); i++) {
                mData[i] = mData[i + 1];
            }
            mIdx = MAX - 1;
        }
        mData[mIdx] = Long.parseLong(id);
        mIdx++;

        dmsg req = new dmsg();
        req.to("/upgrade/sync", null);
    }

    public static void saveBitmap(Bitmap b, String url) {
        File f = new File(url);
        try {
            FileOutputStream out = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
}
