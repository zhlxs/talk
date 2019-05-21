package com.ivsign.android.IDCReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 身份证阅读模块
 */
public class IDCReaderSDK {
    static {
        //用来装载库文件，不论是JNI库文件还是非JNI库文件
        //System.loadLibrary 参数为库文件名，不包含库文件的扩展名
        //注意：wltdecode必须是在JVM属性java.library.path所指向的路径中。
        //System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary("wltdecode");
    }

    private static int unpack(byte[] wlt) {
        byte[] license = new byte[12];
        license[0] = 5;
        license[2] = 1;
        license[4] = 91;
        license[5] = 3;
        license[6] = 51;
        license[7] = 1;
        license[8] = 90;
        license[9] = -77;
        license[10] = 30;
        return wltGetBMP(wlt, license);
    }

    private static native int wltGetBMP(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

    private static native int wltInit(String paramString);

    private static boolean isInit = false;

    public static boolean doBmp(String url) {
        if (!isInit) {
            wltInit("/var/wltlib");
            isInit = true;
        }
        try {
            FileInputStream fs = new FileInputStream(url);
            byte[] d = new byte[1384];
            fs.read(d, 0, d.length);
            fs.close();
            IDCReaderSDK.unpack(d);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        File f = new File("/var/wltlib/zp.bmp");
        if (f.exists())
            return true;
        return false;
    }
}
