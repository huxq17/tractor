package com.andbase.tractor.utils;

import android.os.Environment;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 * Created by huxq17 on 2015/11/20.
 */
public class Util {
    /**
     * 获取sd卡路径
     * @return
     */
    public static String getSdcardPath() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Closes {@code closeable}, ignoring any checked exceptions. Does nothing
     * if {@code closeable} is null.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Closes {@code socket}, ignoring any checked exceptions. Does nothing if
     * {@code socket} is null.
     */
    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (AssertionError e) {
                if (!isAndroidGetsocknameError(e)) throw e;
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Closes {@code serverSocket}, ignoring any checked exceptions. Does nothing if
     * {@code serverSocket} is null.
     */
    public static void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Closes {@code a} and {@code b}. If either close fails, this completes
     * the other close and rethrows the first encountered exception.
     */
    public static void closeAll(Closeable a, Closeable b) throws IOException {
        Throwable thrown = null;
        try {
            a.close();
        } catch (Throwable e) {
            thrown = e;
        }
        try {
            b.close();
        } catch (Throwable e) {
            if (thrown == null) thrown = e;
        }
        if (thrown == null) return;
        if (thrown instanceof IOException) throw (IOException) thrown;
        if (thrown instanceof RuntimeException) throw (RuntimeException) thrown;
        if (thrown instanceof Error) throw (Error) thrown;
        throw new AssertionError(thrown);
    }

    /**
     * Returns true if {@code e} is due to a firmware bug fixed after Android 4.2.2.
     * https://code.google.com/p/android/issues/detail?id=54072
     */
    public static boolean isAndroidGetsocknameError(AssertionError e) {
        return e.getCause() != null && e.getMessage() != null
                && e.getMessage().contains("getsockname failed");
    }

    /**
     * 从路径中获取文件名称
     *
     * @param path 下载路径
     * @return
     */
    public static String getFilename(String path) {
        int start = path.lastIndexOf("/");
        int end = path.indexOf("?");
        String endPath = null;
        if (start < 0) {
            return UUID.randomUUID().toString();
        }
        if (end > 0 && end > start) {
            endPath = path.substring(start + 1, end);
        } else {
            endPath = path.substring(start + 1);
        }
        return endPath;
    }
}
