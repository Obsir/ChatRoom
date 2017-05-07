package utils;

import bean.User;
import protocol.Protocol;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by Obser on 2017/5/6.
 */
public class DBUtils {
    private static final String driver = "com.mysql.jdbc.Driver";
    private static final String url = "jdbc:mysql://127.0.0.1:3306/mydatabase";
    private static final String user = "root";
    private static final String password = "cx951227";
    private Connection conn = null;
    private static DBUtils dbUtils = null;

    private DBUtils(){
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查用户的信息正确性
     * @param name
     * @param password
     * @return
     */
    public boolean check(String name, String password){
        try {
            System.out.println(name);
            PreparedStatement statement = conn.prepareStatement("SELECT user_pwd,user_state FROM USER WHERE user_name = ?");
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                if(password.equals(resultSet.getString(1)) && !resultSet.getBoolean(2))
                    return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean check(Protocol.Message message){
       return check(message.getName(), message.getPassword());
    }


    public boolean updateState(boolean state, String name){
        try {
            PreparedStatement statement = conn.prepareStatement("UPDATE USER SET user_state = ? WHERE user_name = ?");
            statement.setBoolean(1, state);
            statement.setString(2, name);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 用户注册
     * @param name
     * @param password
     * @return
     */
    public boolean add(String name, String password){
        try{
            PreparedStatement statement = conn.prepareStatement("INSERT INTO User VALUES (?, ?, ?)");
            statement.setString(1, name);
            statement.setString(2, password);
            statement.setBoolean(3, false);
            statement.executeUpdate();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建DBUtils的单例对象
     * @return
     */
    public static DBUtils newInstance(){
        if(dbUtils == null)
            dbUtils = new DBUtils();
        return dbUtils;
    }
}
