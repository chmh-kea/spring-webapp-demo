package kea.techy.demo.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Sql
{
    public static final String CONN_STRING = "jdbc:mysql://%s:%s/%s?autoReconnect=true&useSSL=false&user=%s&password=%s";
    private String dbname = "tech";
    public static String host = "localhost";
    public static String port = "4200";
    public static String user = "root";
    public static String pass = "pass1234";
    private static String url;

    private static Sql instance;
    public static Sql getInstance() throws SQLException, ClassNotFoundException
    {
        if(instance == null)
            instance = new Sql();
        return instance;
    }

    private Sql() throws SQLException, ClassNotFoundException
    {
        // Get db config from RDS ENVIRONMENT VARIABLES
        if (System.getProperty("RDS_HOSTNAME") != null)
        {
            String dbName = System.getProperty("RDS_DB_NAME");
            String userName = System.getProperty("RDS_USERNAME");
            String password = System.getProperty("RDS_PASSWORD");
            String hostname = System.getProperty("RDS_HOSTNAME");
            String port = System.getProperty("RDS_PORT");
            url = String.format(CONN_STRING, hostname, port, dbName, userName, password);
            //url = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
        }
        // Get hardcoded local config values
        else
            url = String.format(CONN_STRING, host, port, dbname, user, pass);
//        try
//        {
            Class.forName("com.mysql.jdbc.Driver");
//        } catch (ClassNotFoundException ex)
//        {
//            ex.printStackTrace();
//            //out.print(ex.getMessage());
//        }
    }

//    private static Connection getRemoteConnection() throws SQLException
//    {
//        if (System.getProperty("RDS_HOSTNAME") != null)
//        {
//                String dbName = System.getProperty("RDS_DB_NAME");
//                String userName = System.getProperty("RDS_USERNAME");
//                String password = System.getProperty("RDS_PASSWORD");
//                String hostname = System.getProperty("RDS_HOSTNAME");
//                String port = System.getProperty("RDS_PORT");
//                String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
//               // logger.trace("Getting remote connection with connection string from environment variables.");
//                Connection con = DriverManager.getConnection(url);
//                //logger.info("Remote connection successful.");
//                return con;
//        }
//        return null;
//    }

    private Connection getConn() throws SQLException
    {
        //return getRemoteConnection();
        return DriverManager.getConnection(url);
    }

//    private ResultSet getUnsafe(Connection conn, String query) throws SQLException
//    {
//        statement = conn.createStatement();
//        return statement.executeQuery(query);
//    }

//    private ResultSet getSafe(Connection conn, String query) throws SQLException
//    {
//        PreparedStatement statement = conn.prepareStatement();
//
//        //statement.setsomething
//
//        return null;
//    }

    public List<User> getUsers() throws SQLException
    {
        List<User> users = new ArrayList<>();
        ResultSet result = null;

        try(Connection conn = getConn())
        {
            Statement statement = conn.createStatement();
            result = statement.executeQuery("SELECT users.id, users.firstname, users.lastname, users.username FROM tech.users");

            while(result.next())
            {
                User user = new User(result);
                if(user != null) // Could check if valid on user model
                    users.add(user);
            }

            if (users.size() > 0)
                return users;
        }
        return null;
    }

    public User getUser(String username, String password, boolean safe, boolean hash) throws SQLException
    {
        User user = null;
        ResultSet result = null;
        //String query = "SELECT * FROM tech.users WHERE username='%s' AND password='%s'";

        try(Connection conn = getConn())
        {
            if (safe && !hash)
            {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM tech.users WHERE username=? AND password=?");
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                result = preparedStatement.executeQuery();
            }
            else if (safe && hash)
            {
                byte[] salt = null;

                PreparedStatement getSalt = conn.prepareStatement("SELECT salt FROM tech.users WHERE username=?");
                getSalt.setString(1,username);
                result = getSalt.executeQuery();

                if (result.next())
                    salt = Base64.getDecoder().decode(result.getString(1));

                if (salt != null)
                {
                    PreparedStatement getUser = conn.prepareStatement("SELECT * FROM tech.users WHERE username=? AND password=?");
                    getUser.setString(1,username);
                    getUser.setString(2,Base64.getEncoder().encodeToString(Crypto.hashPassword(password.toCharArray(), salt)));
                    result = getUser.executeQuery();
                }
                else
                    result = null;
                    //throw new SQLException("No user or salt found");
            }
            else
            {
                Statement statement = conn.createStatement();
                result = statement.executeQuery(String.format("SELECT * FROM tech.users WHERE username='%s' AND password='%s'", username, password));
                //result = getUnsafe(conn, String.format(query, username, password));
            }

            if (result.next())
                user = new User(result);
        }
        return user;
    }

    public User createUser(User user, boolean hash) throws SQLException
    {
        ResultSet resultSet = null;

        String password = user.getPassword();
        byte[] salt = null;
        String query = "INSERT INTO tech.users (firstname, lastname, username, password, salt) VALUES (?, ?, ?,";


        if (hash)
        {
            salt = Crypto.generateSalt();
            password = Base64.getEncoder().encodeToString(Crypto.hashPassword(password.toCharArray(), salt));

            query += " ?,";
        }

        query +=  " ?)";
        try(Connection conn = getConn())
        {
            // Insert query and define to get generated keys
            PreparedStatement preparedStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,user.getFirstname());
            preparedStatement.setString(2,user.getLastname());
            preparedStatement.setString(3,user.getUsername());
            preparedStatement.setString(4,password);
            if (hash)
                preparedStatement.setString(5, Base64.getEncoder().encodeToString(salt));

            if (0 < preparedStatement.executeUpdate());
                resultSet = preparedStatement.getGeneratedKeys(); // Get the generated id

            if (resultSet.next())
            {
                user.setId(resultSet.getInt(1));
                user.setPassword("");
                return user;
            }
        }
        return null;
    }
}
