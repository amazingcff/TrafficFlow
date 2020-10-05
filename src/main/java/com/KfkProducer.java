package com;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
public class KfkProducer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Properties properties = new Properties();
        properties.put("bootstrap.servers","localhost:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String,String> producer = new KafkaProducer<String, String>(properties);
        try {
            ProducerRecord<String,String> record;
            try {
                //读取本地文件
            	StringBuffer sb= new StringBuffer("");
                FileReader reader = new FileReader("C:\\Users\\lenovo\\Desktop\\dachuangPresentation\\frontend-prototype\\trafficFlow\\roadStatisticOrdered_zyd");
                String str = null;
                BufferedReader br = new BufferedReader(reader);
                int count=1;
                while((str = br.readLine()) != null) {
                    if(count % 100 == 0) {
                        Thread.sleep(5000);
                	}
                	// 这里的key值为null,所以kafka会根据分区总数把数据负载均衡到每个分区，如果有值，则根据值来判断存到哪个分区。
                    record = new ProducerRecord<String,String>("testtopic",null,str);
                    // 生产者有3种发送方式：1、发送并忘记；2、同步发送；3、异步发送
                    producer.send(record); // 此处是 1、发送并忘记
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            producer.close();
        }
	}
}
