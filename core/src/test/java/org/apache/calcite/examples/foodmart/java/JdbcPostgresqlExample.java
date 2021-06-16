package org.apache.calcite.examples.foodmart.java;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcPostgresqlExample {
  public static void main(String[] args) throws SQLException {
    runJdbc();
  }

  private static void runJdbc() throws SQLException {
    final Connection connection = DriverManager.getConnection(
        "jdbc:calcite:model=core/src/test/resources/postgresql-modle.json");
    String sql = "select dateToLocalDate(1),2 from \"nobigo\".\"table1\"";
    final ResultSet resultSet = connection.createStatement()
        .executeQuery(sql);
    final StringBuilder buf = new StringBuilder();
    while (resultSet.next()) {
      int n = resultSet.getMetaData().getColumnCount();
      for (int i = 1; i <= n; i++) {
        buf.append(i > 1 ? "; " : "")
            .append(resultSet.getMetaData().getColumnLabel(i))
            .append("=")
            .append(resultSet.getObject(i));
      }
      System.out.println(buf.toString());
      buf.setLength(0);
    }
//    System.out.println(CalciteAssert.toString(resultSet));
    connection.close();
  }
}
