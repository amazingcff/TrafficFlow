package utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

//获取到db.properties文件中的数据库信息
public class JdbcUtil {
  //私有变量
  private static String driver;
  private static String url;
  private static String user;
  private static String password;

  //静态块
  static{
      try{
          //1.新建属性集对象
          Properties properties = new Properties();
          //2.通过反射，新建字符输入流，读取db.properties文件
          InputStream input = JdbcUtil.class.getClassLoader().getResourceAsStream("db.properties");
          //3.将输入流中读取到的属性，加载到properties属性集对象中
          properties.load(input);
          //4.根据键，获取properties中对应的值
          driver = properties.getProperty("driver");
          url = properties.getProperty("url");
          user = properties.getProperty("user");
          password = properties.getProperty("password");
      }catch(Exception e){
          e.printStackTrace();
      }
  }

  //返回数据库连接
  public static Connection getConnection(){
      try{
          //注册数据库的驱动
          Class.forName(driver);
          //获取数据库连接（里面内容依次是：主机名和端口、用户名、密码）
          Connection connection = DriverManager.getConnection(url,user,password);
          //返回数据库连接
          return connection;
      }catch (Exception e){
          e.printStackTrace();
      }
      return null;
  }
  
	public static void writeIntoMysql(String m)throws IOException, SQLException{
        //获取数据库连接
        Connection connection = JdbcUtil.getConnection();
        //需要执行的sql语句
        String sql = "insert into roadConditions"
        		+ "(timeIndex,roadname,roadId,avgspeed,congestlevel,congesttrend,congestdesc,roadpts) "
        		+ "values(?,?,?,?,?,?,?,?)";
        //获取预处理对象，并给参数赋值
        PreparedStatement statement = connection.prepareCall(sql);
        statement.setDate(1, new java.sql.Date(System.currentTimeMillis()));
        statement.setString(2,"蓝天路");
        statement.setString(3,m);
        statement.setDouble(4,23.2);
        statement.setString(5,"3");
        statement.setString(6,"4");
        statement.setString(7,"5");
        statement.setString(8,"[123.23,24.24;123.32,24.42]");
        //执行sql语句（插入了几条记录，就返回几）
        int i = statement.executeUpdate();  //executeUpdate：执行并更新
        System.out.println(i);
        //关闭jdbc连接
        statement.close();
        connection.close();
	}
}
