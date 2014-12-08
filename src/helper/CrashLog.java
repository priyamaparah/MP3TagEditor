package helper;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;


public class CrashLog implements Thread.UncaughtExceptionHandler
{
    private static final String CrashFile = "crash.log";
    private static final String TAG = CrashLog.class.getSimpleName();

    private static CrashLog _instance;

    private DeviceInfo _device;
    private Writer _logFile;

    private CrashLog(DeviceInfo dev) {
        _device = dev;
        Thread main = Looper.getMainLooper().getThread();
        main.setUncaughtExceptionHandler(this);
    }

    public static CrashLog instance(DeviceInfo dev)
    {
        synchronized (CrashLog.class) {
            if (_instance == null)
                _instance = new CrashLog(dev);
        }

        return _instance;
    }

    private static class SBF
    {
        StringBuilder _sb = new StringBuilder();

        SBF format(String f, Object... a)
        {
            _sb.append(String.format(f, a));
            return this;
        }

        SBF append(String s)
        {
            _sb.append(s);
            return this;
        }

        @Override public String toString() { return _sb.toString(); }
    }

    void dumpSystemInfo(Writer w) throws IOException
    {
        SBF h = new SBF();

        h.format("Product: %s\n", Build.MODEL);
        h.format("Hardware Model: %s\n", Build.FINGERPRINT);
        h.format("Version: %s (%d)\n", _device.buildVersion(),
                 _device.buildDate().getTime());
        h.format("Report Date/Time: %d\n", new Date().getTime());
        h.format("OS Version: %s/%s\n",
                 Build.VERSION.RELEASE, Build.VERSION.INCREMENTAL);
        h.append("\n");
        w.write(h.toString());
    }

    Writer logFile() throws IOException
    {
        if (_logFile != null)
            return _logFile;

        synchronized (this) {
            Context ctx = _device.getApplicationContext();
            if (_logFile == null) {
                OutputStream ous = ctx.openFileOutput(CrashFile,
                                                      ctx.MODE_PRIVATE);
                _logFile = new OutputStreamWriter(ous);
                dumpSystemInfo(_logFile);
                _logFile.write("\n");
            }
            return _logFile;
        }
    }

    /* ------------------------------ */

    void LogException(Throwable t, Writer w) throws IOException
    {
        SBF fmt = new SBF();

        fmt.format("Uncaught Exception '%s'\n", t.toString());
        fmt.append("Exception Backtrace:\n");
        for (Throwable ex = t; true; ) {
            for (StackTraceElement ste : ex.getStackTrace())
                if (ste.isNativeMethod())
                    fmt.format("   %s.%s(native)\n",
                               ste.getClassName(),
                               ste.getMethodName());
                else fmt.format("   %s.%s(%s:%d)\n",
                                ste.getClassName(),
                                ste.getMethodName(),
                                ste.getFileName(),
                                ste.getLineNumber());
            Throwable ex2 = ex.getCause();
            if (ex2 == null || ex2 == ex) break;
            fmt.format("Caused by: %s\n", ex2.toString());
            ex = ex2;
        }

        w.write(fmt.append("\n").toString());
    }

    private static Charset ASCII;
    {
        try {
            ASCII = Charset.forName("US-ASCII");
        } catch (Exception e) {
            throw new RuntimeException("ASCII not supported.", e);
        }
    }

    @Override public void uncaughtException(Thread t, Throwable e)
    {
        /* restore default, in case handler dies. */
        t.setUncaughtExceptionHandler(t.getDefaultUncaughtExceptionHandler());
        Writer w = null;
        int l;
        byte[] buffer = new byte[102400];

        try {
            w = logFile();
            _history.dump(w);
            w.write("\n");
            LogException(e, w);
        } catch (Exception e1) {
            Log.e(TAG, "Could not create crash log", e1);
        } finally {
            try {w.close();} catch (IOException e1) {}
        }

        Log.i(TAG, "Crash log file stored in %s\n" + CrashFile);
        Log.e(TAG, "Crash reason:", e);

        System.exit(1);
    }

    /* ---------------------------------------- */

    private byte[] previousCrashLog_int() throws Exception
    {
        InputStream f = null;

        try {
            f = _device.getApplicationContext().openFileInput(CrashFile);
        } catch (FileNotFoundException fnf) {
            return null;
        }

        try {
            ByteArrayOutputStream compress = new ByteArrayOutputStream();
            GZIPOutputStream strm = new GZIPOutputStream(compress);

            byte[] input = new byte[16384];
            int l;
            while ((l = f.read(input)) > 0)
                strm.write(input, 0, l);
            strm.finish();
            strm.close();
            return compress.toByteArray();
        } finally {
            f.close();
        }
    }

    /** returns zip-compressed contents of the crash log, and deletes
     * the file */
    public byte[] previousCrashLog()
    {
        try {
            return previousCrashLog_int();
        } catch (Exception e) {
            Log.e(TAG, "Failed to package up crash log", e);
            return null;
        } finally {
            _device.getApplicationContext().deleteFile(CrashFile);
        }
    }

    private static final SimpleDateFormat dfmt =
            new SimpleDateFormat("MMM dd kk:mm:ss");
    private static CircBuf _history = new CircBuf();
    private static class CircBuf
    {
        private static final int BUFSIZE = 100 * 1024;
        private int _get, _put;
        private final char _buf[] = new char[BUFSIZE];

        void dump(Writer w) throws IOException
        {
            if (_get > _put) {
                w.write(_buf, _get, _buf.length - _get);
                w.write(_buf, 0, _put);
            } else if (_get < _put) {
                w.write(_buf, _get, _put - _get);
            }
        }

/*        void clear() { _get = _put = 0; }

        void println(String line)
        {
            append(String.format("[%s] - %s\n",
                    dfmt.format(new Date()), line));
        }

        private void append(String cs)
        {
            if (cs.length() >= _buf.length) {
                clear();
                return;
            }

            int len = _buf.length - _put;
            boolean smoosh = false;
            if (len > cs.length()) len = cs.length();
            int i = 0;
            if (_get > _put && _get <= _put + len) smoosh = true;
            while (i < len) _buf[_put++] = cs.charAt(i++);
            if (_put == _buf.length) _put = 0;
            if (i < cs.length()) {
                len = cs.length() - i;
                if (_get < len) smoosh = true;
                while (i < cs.length()) _buf[_put++] = cs.charAt(i++);
                if (_put == _buf.length) _put = 0;
            }

            if (smoosh) {
                for (_get = _put + 1; _get != _put; _get++) {
                    if (_get >= _buf.length) _get = 0;
                    if (_buf[_get] == '\n') {
                        if (++_get == _buf.length) _get = 0;
                        break;
                    }
                }
            }
        } */
    }
}
