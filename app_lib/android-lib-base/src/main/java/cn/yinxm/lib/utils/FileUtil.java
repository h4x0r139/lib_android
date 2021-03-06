
package cn.yinxm.lib.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lishicong on 2016/12/13.
 */
public class FileUtil {

    /**
     * 根据uri获得file
     *
     * @param context
     * @param uri
     *
     * @return
     */
    public static File getFile(Context context, Uri uri) {
        File file = new File(getPath(context, uri));
        return file;
    }

    /**
     * 根据path获得file
     *
     * @param pathname
     *
     * @return
     */
    public static File getFile(String pathname) {
        File file = new File(pathname);
        return file;
    }

    /**
     * 根据Uri删除文件
     *
     * @param context
     * @param uri
     *
     * @return
     */
    public static boolean deleteFile(Context context, Uri uri) {
        File file = new File(getPath(context, uri));
        if (file == null || !file.exists()) {
            return false;
        }
        return file.delete();
    }

    /**
     * 根据path获得file
     *
     * @param pathname
     *
     * @return
     */
    public static boolean deleteFile(String pathname) {
        File file = new File(pathname);
        if (file == null || !file.exists()) {
            return false;
        }
        return file.delete();
    }

    /**
     * 创建路径
     */
    public static String getDirPath() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/wzm");
        //        创建文件夹
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getPath();
    }

    /**
     * 获得图片Uri,若没有文件，新建
     *
     * @return
     */
    public static Uri getImgUri() {
        File file = createImg();
        if (file == null) {
            return Uri.EMPTY;
        }
        return Uri.fromFile(file);
    }

    /**
     * 获得图片地址，若没有文件，新建
     *
     * @param context
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getImgPath(Context context) {
        Uri uri = getImgUri();
        if (uri == Uri.EMPTY) {
            return null;
        }
        return getPath(context, uri);
    }

    /**
     * 创建图片
     */
    public static File createImg() {
        // 文件名
        String fileName = System.currentTimeMillis() + ".jpeg";
        return createFile(fileName);
    }

    /**
     * 获得文件
     */
    public static File createFile(String fileName) {
        if (!checkSDStatus()) {
            return null;
        }

        // 创建文件
        File file = new File(getDirPath(), fileName);
        if (file.exists()) {
            file.delete();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 检查sd卡状态
     */
    public static boolean checkSDStatus() {
        // 判断sd是否可用
        String sdStatus = Environment.getExternalStorageState();
        return sdStatus.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 根据uri获取图片路径的函数，由于4.4之后uri发生变化，不能采用之前的方式获取路径
     *
     * @param context
     * @param uri
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                                                  Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     *
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     *
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     *
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check
     *
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     *
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 将byte[]写入文件
     *
     * @param filePath
     * @param bytes
     */
    public static void writeToFile(String filePath, byte[] bytes) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字节写入文件
     *
     * @param bytes    字节数组
     * @param fileDir  文件存放路径
     * @param fileName 文件名
     *
     * @throws IOException
     */
    public static void writeByteToFile(byte[] bytes, String fileDir, String fileName) throws IOException {

        if (hasSDCard()) {

            File file = createDir(fileDir);

            if (file != null) {
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + File.separator + fileName);
                fos.write(bytes);
                fos.close();
            }
        }
    }

    /**
     * 读取文件
     *
     * @param fileName
     *
     * @return
     *
     * @throws IOException
     */
    public static String readFile(String fileName) throws IOException {

        if (hasSDCard() && isExist(new File(fileName))) {

            FileInputStream fin = new FileInputStream(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            String res = new String(buffer, "UTF-8");
            fin.close();
            return res;
        }
        return null;
    }

    /**
     * 检查是否存在SD卡
     *
     * @return
     */
    public static boolean hasSDCard() {

        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 创建文件夹
     *
     * @param path
     *
     * @return
     */
    public static File createDir(String path) {
        File dir = new File(path);
        if (!isExist(dir)) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 判断File对象所指的目录或文件是否存在
     *
     * @param file
     *
     * @return
     */
    public static boolean isExist(File file) {
        return file.exists();
    }

    /**
     * 获取临时缓存数据目录(返回SDCard/Android/data/你的应用包名/cache)
     *
     * @param context
     * @param file    目录名
     *
     * @return
     */
    public static String getExternalCacheDir(Context context, String file) {
        return context.getExternalCacheDir().getPath() + File.separator + file;
    }

    public static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}
