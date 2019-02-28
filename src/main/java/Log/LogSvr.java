package Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *<p>title: 日志服务器</p>
 *<p>Description: 模拟日志服务器</p>
 */
public class LogSvr {

    private SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //启动一个线程每5秒钟向日志文件写一次数据
    private ScheduledExecutorService exec =
            Executors.newScheduledThreadPool(2);

    /**
     * 将信息记录到日志文件
     *
     * @param logFile 日志文件
     * @param mesInfo 信息
     * @throws
     */
    public void logMsg(File logFile, String mesInfo) throws IOException {
        if (logFile == null) {
            throw new IllegalStateException("logFile can not be null!");
        }
        Writer txtWriter = new FileWriter(logFile, true);
        txtWriter.write(logFile.getName() + "," +dateFormat.format(new Date()) + ",\t" + mesInfo + "\n");
        txtWriter.flush();
    }

    public void log(String logFileName, final String mesInfo) {
        final LogSvr logSvr = new LogSvr();
        final File tmpLogFile = new File(logFileName);
        if (!tmpLogFile.exists()) {
            try {
                tmpLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        exec.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    logSvr.logMsg(tmpLogFile, mesInfo);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);

    }

   /* public static void main(String[] args) {
        String mesInfo = "for Test!!!!!!!!";

        LogSvr svr = new LogSvr();
        svr.log("1.log",mesInfo);
    }*/

}