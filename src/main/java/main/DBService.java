package main;

import java.sql.*;
import java.util.HashMap;

public class DBService {
    private static final String url = "jdbc:sqlite:assets/results.db";

    public DBService() {
        createNewDatabase("results.db");
    }

    public void connect() {
        Connection conn = null;
        try {
            // db parameters

            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:assets/" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


     public void storeResultMap(String tableName, HashMap<String,String> resultMap) {
         StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (\n");
         resultMap.keySet().forEach(entry->{
             createSql.append(entry+" text ,\n");
         });
         createSql.deleteCharAt(createSql.length()-1);
         createSql.deleteCharAt(createSql.length()-1);
         createSql.append(" PRIMARY KEY ");
         createSql.append(");");
         System.out.println("SQL: "+createSql.toString());
         try (Connection conn = DriverManager.getConnection(url);
              Statement stmt = conn.createStatement()) {
             // create a new table
             stmt.execute(createSql.toString());

              StringBuilder insertSql = new StringBuilder("INSERT OR REPLACE INTO " + tableName + " (\n");
             resultMap.keySet().forEach(entry->{
                 insertSql.append(entry+", ");
             });
             insertSql.deleteCharAt(insertSql.length()-1);
             insertSql.deleteCharAt(insertSql.length()-1);
             insertSql.append(") VALUES (");
             resultMap.values().forEach(value->{
                 insertSql.append("\""+value+"\""+", ");
             });
             insertSql.deleteCharAt(insertSql.length()-1);
             insertSql.deleteCharAt(insertSql.length()-1);
             insertSql.append(");");
             System.out.println("SQL: "+insertSql.toString());
             stmt.execute(insertSql.toString());

         } catch (SQLException e) {
             e.printStackTrace();
             System.out.println(e.getMessage());
         }
    }



}
