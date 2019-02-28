package Log;


import Kafka.KProducer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogView {
    private long lastTimeFileSize = 0;  //上次文件大小

    //启动一个线程每10秒钟读取新增的日志信息
    private ScheduledExecutorService exec =
            Executors.newScheduledThreadPool(2);
    final KProducer producer = new KProducer();


    /**
     * 实时输出日志文件内容
     * @param logFile 日志文件
     * @throws IOException
     */
    public void realtimeShowLog(File logFile) throws IOException{
        //指定文件可读可写
        final RandomAccessFile randomFile = new RandomAccessFile(logFile,"rw");

        exec.scheduleWithFixedDelay(new Runnable(){
            public void run() {
                try {
                    //获得变化部分的
                    randomFile.seek(lastTimeFileSize);
                    String tmp = "";
                    while( (tmp = randomFile.readLine())!= null) {
//                        System.out.println(new String(tmp.getBytes("UTF8")));
                        producer.producer(new String(tmp.getBytes("UTF8")));
                    }
                    lastTimeFileSize = randomFile.length();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /*public static void main(String[] args) throws Exception {
        LogView view = new LogView();
        final File tmpLogFile = new File("1.log");
        view.realtimeShowLog(tmpLogFile);
    }*/
}
