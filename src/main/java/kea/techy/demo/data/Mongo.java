package kea.techy.demo.data;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import java.util.Arrays;
import com.mongodb.Block;

import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;

public class Mongo
{
    private MongoClient client;
    private MongoDatabase db;

    public static Mongo instance;

    public static Mongo getInstance()
    {
        if(instance == null)
            instance = new Mongo();
        return instance;
    }

    private Mongo()
    {
        client = new MongoClient();

        db = client.getDatabase("docdb");
    }

    public MongoCollection<Document> getUsers()
    {
        return db.getCollection("users");
    }

    public void createUser(String fname,String lname)
    {
        Document doc = new Document("firstname", fname)
                .append("lastname", lname);

        getUsers().insertOne(doc);
    }
}
