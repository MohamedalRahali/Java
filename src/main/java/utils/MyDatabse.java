package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabse {
   final String URL = "jdbc:mysql://localhost:3306/art";

  final   String USERNAME = "root";

   final String PWD = "";

   Connection cnx ;

   public static MyDatabse instance ;
    private MyDatabse (){
       try {
           cnx = DriverManager.getConnection(URL,USERNAME,PWD);

           System.out.println("connected !!!");
       } catch (SQLException e) {
           System.err.println(e.getMessage());
       }

   }

public   static MyDatabse getInstance(){

        if(instance==null)
            instance = new MyDatabse() ;

        return  instance ;
   }

    public Connection getCnx() {
        return cnx;
    }
}
