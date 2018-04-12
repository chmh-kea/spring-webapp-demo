package kea.techy.demo.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Sql
{
    private final String CONN_STRING = "jdbc:mysql://%s:%s/%s?autoReconnect=true&useSSL=false&user=%s&password=%s";

    private String dbname;
    private String connString;

    private static Sql instance;

    public static Sql getInstance() throws SQLException, ClassNotFoundException
    {
        if(instance == null)
            instance = new Sql();
        return instance;
    }

    private Sql() throws SQLException, ClassNotFoundException
    {
        // AWS
        // Get db config from RDS ENVIRONMENT VARIABLES for AWS
        if (System.getProperty("RDS_HOSTNAME") != null)
        {
            dbname = System.getProperty("RDS_DB_NAME");

            String userName = System.getProperty("RDS_USERNAME");
            String password = System.getProperty("RDS_PASSWORD");
            String hostname = System.getProperty("RDS_HOSTNAME");
            String port = System.getProperty("RDS_PORT");

            connString = String.format(CONN_STRING, hostname, port, dbname, userName, password);
            //connString = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
        }
        // LOCAL
        // Get hardcoded local config values
        else
        {
            dbname = "tech";
            connString = String.format(CONN_STRING, "localhost", "4200", dbname, "root", "pass1234");
        }
            Class.forName("com.mysql.jdbc.Driver");
    }
//
//    private String initializeDatabase() throws SQLException
//    {
//        try(Connection conn = getConn())
//        {
//            Statement statement = conn.createStatement();
//            ResultSet result = statement.executeQuery()
//        }
//
//    }

    private Connection getConn() throws SQLException
    {
        return DriverManager.getConnection(connString);
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
            result = statement.executeQuery(String.format("SELECT users.id, users.firstname, users.lastname, users.username FROM %s.users", dbname));

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
            // Sanitized SQL and NOT hashed password
            if (safe && !hash)
            {
                PreparedStatement preparedStatement = conn.prepareStatement(String.format("SELECT * FROM %s.users WHERE username=? AND password=?", dbname));
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                result = preparedStatement.executeQuery();
            }
            // Sanitized SQL and Hashed passwords
            else if (safe && hash)
            {
                byte[] salt = null;

                PreparedStatement getSalt = conn.prepareStatement(String.format("SELECT salt FROM %s.users WHERE username=?", dbname));
                getSalt.setString(1,username);
                result = getSalt.executeQuery();

                if (result.next())
                    salt = Base64.getDecoder().decode(result.getString(1));

                if (salt != null)
                {
                    PreparedStatement getUser = conn.prepareStatement(String.format("SELECT * FROM %s.users WHERE username=? AND password=?",dbname));
                    getUser.setString(1,username);
                    getUser.setString(2,Base64.getEncoder().encodeToString(Crypto.hashPassword(password.toCharArray(), salt)));
                    result = getUser.executeQuery();
                }
                else
                    result = null;
                    //throw new SQLException("No user or salt found");
            }
            // NOT Sanitized and NOT Hashed
            else
            {
                Statement statement = conn.createStatement();
                result = statement.executeQuery(String.format("SELECT * FROM %s.users WHERE username='%s' AND password='%s'", dbname, username, password));
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
        String query = String.format("INSERT INTO %s.users (firstname, lastname, username, password, salt) VALUES (?, ?, ?,", dbname);


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
