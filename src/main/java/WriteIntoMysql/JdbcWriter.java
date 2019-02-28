package WriteIntoMysql;


import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class JdbcWriter extends RichSinkFunction<Tuple4<String,String,String, Integer>>{
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private PreparedStatement preparedStatement;
    String username = "root";
    String password = "root";
    String drivername = "com.mysql.jdbc.Driver";
    String dburl = "jdbc:mysql://localhost:3306/flink";

    public void invoke(Tuple4<String,String,String, Integer> value) throws Exception {
        Class.forName(drivername);
        connection = DriverManager.getConnection(dburl, username, password);
//        String sql = "insert into logserv(postdate,filename,logInfo,count) values(?,?,?,?)";
        String sql = "replace into logserv(postdate,filename,logInfo,count) values(?,?,?,?)";
        preparedStatement = connection.prepareStatement(sql);
//        preparedStatement.setDate(1, new Date(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(value.f1).getTime()));
        preparedStatement.setString(1,value.f1);
        preparedStatement.setString(2, value.f0);
        preparedStatement.setString(3, value.f2);
        preparedStatement.setInt(4, value.f3);
        int insert = preparedStatement.executeUpdate();
        if (insert > 0) {
            System.out.println("value = [" + value + "]");
        }
        if (preparedStatement != null) {
            preparedStatement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

}
