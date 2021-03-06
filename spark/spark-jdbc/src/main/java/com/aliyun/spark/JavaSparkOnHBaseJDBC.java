package com.aliyun.spark;

import java.sql.*;

/**
 * 运行spark on HBase/Phoenix java JDBC demo 依赖的jar包请见pom文件
 */
public class JavaSparkOnHBaseJDBC {
  public static void main(String[] args) {
    // Spark JDBC Driver 路径。
    String driver = "org.apache.hive.jdbc.HiveDriver";
    // ThriftServer访问地址，可以从Spark集群详情获取。使用时请把此路径替换为你自己的Spark集群的ThriftServer访问地址。
    // 格式为：jdbc:hive2://xxx-001.spark.9b78df04-b.rds.aliyuncs.com:10000;
    String thriftServerAdress = args[0];
    //Spark侧的表名。
    String sparkTableName = "spark_hbase";
    //hbase侧的表名，需要在hbase侧提前创建。hbase表创建可以参考：https://help.aliyun.com/document_detail/52051.html?spm=a2c4g.11174283.6.577.7e943c2eiYCq4k
    String hbaseTableName = "mytable";
    //HBase集群的ZK链接地址。使用时请把此路径替换为你自己的HBase集群的zk访问地址。
    //格式为：xxx-002.hbase.rds.aliyuncs.com:2181,xxx-001.hbase.rds.aliyuncs.com:2181,xxx-003.hbase.rds.aliyuncs.com:2181
    String zkAddress = args[1];
    Connection conn = null;
    try {
      Class.forName(driver);
      conn = DriverManager.getConnection(thriftServerAdress);
      Statement stmt = conn.createStatement();
      //建表语句
      String createCmd = "CREATE TABLE " + sparkTableName + " USING org.apache.hadoop.hbase.spark\n" +
              "    OPTIONS ('catalog'= \n" +
              "    '{\"table\":{\"namespace\":\"default\", \"name\":\"" + hbaseTableName + "\"},\"rowkey\":\"rowkey\", \n" +
              "    \"columns\":{ \n" +
              "    \"col0\":{\"cf\":\"rowkey\", \"col\":\"rowkey\", \"type\":\"string\"},\n" +
              "    \"col1\":{\"cf\":\"cf\", \"col\":\"col1\", \"type\":\"String\"}}}',\n" +
              "    'hbase.zookeeper.quorum' = '" + zkAddress + "'\n" +
              "    )";
      System.out.println(" createCmd: \n" + createCmd);
      //创建表
      stmt.execute(createCmd);

      String querySql = "select * from " + sparkTableName + " limit 1";
      PreparedStatement pstmt = conn.prepareStatement(querySql);
      ResultSet resultSet = pstmt.executeQuery();
      //打印查询结果
      while (resultSet.next()) {
        System.out.println(resultSet.getString(1) + " | " +
                resultSet.getString(2));
      }

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (null != conn) {
        try {
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
