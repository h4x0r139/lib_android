package cn.yinxm.lib.utils.log;

import org.apache.log4j.Logger;

/**
 * 功能：日志打印到文件
 * Created by yinxm on 2017/7/11.
 */

public class LogFileUtil {
    public static final String TAG = LogUtil.TAG;
    private static boolean isLog2File = true;//默认允许打印到文件

    public static boolean isLog2File() {
        return isLog2File;
    }

    public static void setLog2File(boolean log2File) {
        isLog2File = log2File;
    }

    public static void d(String tag, String str) {
        if (isLog2File()) {
            if (tag == null || "".equals(tag)) {
                tag = TAG;
            }
            Logger log = Logger.getLogger(tag);
            if (log != null) {
                log.debug(str);
            }
        }
    }

    public static void i(String tag, String str) {
        if (isLog2File()) {
            if (tag == null || "".equals(tag)) {
                tag = TAG;
            }
            Logger log = Logger.getLogger(tag);
            if (log != null) {
                log.info(str);
            }
        }
    }

    public static void e(String tag, String str) {
        if (isLog2File()) {
            if (tag == null || "".equals(tag)) {
                tag = TAG;
            }
            Logger log = Logger.getLogger(tag);
            if (log != null) {
                log.error(str);
            }
        }
    }
}
