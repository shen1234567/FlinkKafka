

import Log.LogSvr;
import Log.LogView;
import WriteIntoMysql.JdbcWriter;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple3;

import java.io.File;
import java.util.Properties;

public class FlinkCostKafka {

    private static Logger logger = LoggerFactory.getLogger(FlinkCostKafka.class);

    public static void main(String[] args) throws Exception {
        String fileName = "0.log";
        String fileName2 = "1.log";

        logger.info("========开始写入文件========");
        String mesInfo = "for Test!!!!!!!!";
        LogSvr svr = new LogSvr();
        svr.log(fileName,mesInfo);
        svr.log(fileName2,mesInfo);


        logger.info("========开始读取文件========");
        LogView view = new LogView();
        final File tmpLogFile = new File(fileName);
        view.realtimeShowLog(tmpLogFile);

        final File tmpLogFile2 = new File(fileName2);
        view.realtimeShowLog(tmpLogFile2);

        logger.info("========flink消费部分========");
        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //默认情况下，检查点被禁用。要启用检查点，请在StreamExecutionEnvironment上调用enableCheckpointing(n)方法，
        // 其中n是以毫秒为单位的检查点间隔。每隔5000 ms进行启动一个检查点,则下一个检查点将在上一个检查点完成后5秒钟内启动
        env.enableCheckpointing(5000);

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");//kafka的节点的IP或者hostName，多个使用逗号分隔
        properties.setProperty("zookeeper.connect", "127.0.0.1:2181");//zookeeper的节点的IP或者hostName，多个使用逗号进行分隔
        properties.setProperty("group.id", "test-consumer-group");//flink consumer flink的消费者的group.id

        FlinkKafkaConsumer09<String> myConsumer = new FlinkKafkaConsumer09<String>("flink02", new SimpleStringSchema(),
                properties);

//        myConsumer.setStartFromLatest();
        DataStream<String> stream = env.addSource(myConsumer);

        DataStream<Tuple4<String,String,String, Integer>> counts = stream.flatMap(new LineSplitter()).keyBy(0,2).sum(3);

//        counts.print();
        counts.addSink(new JdbcWriter());
        env.execute("Flink Streaming Java API Skeleton");

    }

    public static final class LineSplitter implements FlatMapFunction<String, Tuple4<String,String,String, Integer>> {
        private static final long serialVersionUID = 1L;

        public void flatMap(String value, Collector<Tuple4<String,String,String, Integer>> out) {
            String[] tokens = value.toLowerCase().split(",");

            out.collect(new Tuple4<String,String,String, Integer>(tokens[0],tokens[1],tokens[2], 1));

        }
    }

}
