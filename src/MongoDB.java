import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;


import javax.print.Doc;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MongoDB {
    // ...
    private MongoCollection<Document> collection;
    private Object next;

    public MongoDB(MongoCollection<Document> coll) {
        collection = coll;

    }

    Object FindWord(String word) {

        FindIterable<Document> dbc = collection.find(new BasicDBObject("id", word));
        Iterator it = dbc.iterator();
        Object next;
        if (it.hasNext()) {
            return it.next();
        }
//        if (!dc.isEmpty())
//            return true;

        return null;
    }

    void insertword(String word, int position, int DocNumber) {

        Object wordFound = FindWord(word);
        if (wordFound == null) {
            List<String> paths = Arrays.asList("Nice", "Not Nice");
            Document Doc1 = new Document("id", word)
                    .append("bruh", "nice")
                    .append("paths", paths)
                    .append("nested", new BasicDBObject("array", Arrays.asList("one", "two")));
            collection.insertOne(Doc1);
        } else {
            Document doc = (Document) wordFound;
            Document nested = (Document) doc.get("nested");
            List<String> list = (List<String>) nested.get("array");

            // Sets the ID of the found document to MMXXIII
//            collection.updateOne(Filters.eq("_id", oldID), Updates.set("id", "MMXXIII"));
            System.out.println("AAAAAAAAAAAA");
        }

    }

    public static void main(String[] argv) {
        ConnectionString connString = new ConnectionString(
                "mongodb://127.0.0.1:27017"
                // connect to local host
        );
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("local");
        MongoCollection<Document> collection = database.getCollection("first");

        MongoDB Mon = new MongoDB(collection);

        Mon.insertword("v", 2, 3);


        System.out.println("Collection sampleCollection selected successfully");
    }
}