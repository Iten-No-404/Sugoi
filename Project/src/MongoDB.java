import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayList;  
import java.util.Arrays;  
import java.io.File;
import java.io.IOException; 
import java.util.*;
public class MongoDB {
    // ...

	public	static void main(String [] argv)
  {
	ConnectionString connString = new ConnectionString(
		"mongodb://127.0.0.1:27017"
		// connect to local host
	);
	MongoClientSettings settings = MongoClientSettings.builder()
		.applyConnectionString(connString)
		.retryWrites(true)
		.build();
	MongoClient mongoClient = MongoClients.create(settings);
	MongoDatabase database = mongoClient.getDatabase("try");
	MongoCollection<Document> collection = database.getCollection("first");
			System.out.println("Collection sampleCollection selected successfully");
			Document document1 = new Document("title", "MongoDB")
			.append("description", "database")
			.append("likes", 100)
			.append("url", "http://www.tutorialspoint.com/mongodb/")
			.append("by", "tutorials point");
			List<Document> list = new ArrayList<Document>();
			list.add(document1);
			
			collection.insertMany(list);
	System.out.println("Collection sampleCollection selected successfully");
  }   
}
