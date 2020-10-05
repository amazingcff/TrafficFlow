package com;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

import utils.JdbcUtil;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.Date;
import java.util.Properties;

public class test {

	public static void main(String[] args) throws Exception {
        final String ZOOKEEPER_HOST = "localhost:2181";
        final String KAFKA_HOST = "localhost:9092";
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000); // 非常关键，一定要设置启动检查点！！
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties props = new Properties();
        props.setProperty("zookeeper.connect", ZOOKEEPER_HOST);
        props.setProperty("bootstrap.servers", KAFKA_HOST);
        props.setProperty("group.id", "test-consumer-group");
        //props.setProperty("auto.offset.reset", "earliest");//从latest开始消费

        DataStream<String> transction = env.addSource(new FlinkKafkaConsumer010<String>("testtopic", new SimpleStringSchema(), props));
        
        //异常筛选
        OutlierDiscovery_3 od=new OutlierDiscovery_3();
        DataStream<String> afterOd=od.Handle(transction);
        
        //行驶用户识别
        
        //道路计算
        
        
        transction.rebalance().map(new MapFunction<String, Object>() {
			private static final long serialVersionUID = 1L;
			public String map(String value)throws IOException, SQLException{
				
				System.out.println(value);
				JdbcUtil.writeIntoMysql(value);
				return null;
           }
        });
        
        WindowedStream<String, Tuple, TimeWindow> timeWindow = 
        		transction.keyBy(0).timeWindow(Time.minutes(30), Time.seconds(60));
        
        //timeWindow.apply(JdbcUtil.writeIntoMysql()).print();
        
        try {
            // 转换算子都是lazy init的, 最后要显式调用 执行程序
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
