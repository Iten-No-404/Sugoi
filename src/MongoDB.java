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
	private MongoCollection<Document> collection;
 public MongoDB( MongoCollection<Document> coll)
   {
	  collection=coll;

   }
   Boolean FindWord(String word)
   {

	Document dc=  collection.find(new Document("id",word)).first();
   if(!dc.isEmpty())
    return true;

	return false;
   }
	 
    void insertword(String word, int position ,int DocNumber)
	{
	

      if( !FindWord(word))
	{
		Document Doc1=new Document("id",word);
		Doc1.append(String.valueOf(DocNumber),position);
		List<Document> list = new ArrayList<Document>();
		list.add(Doc1);
       collection.insertOne(Doc1);
	}
	else
	{

	}
	   

	}
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
		
			MongoDB Mon=new MongoDB(collection);
		
		Mon.insertword("iiii",2,3);
		
	System.out.println("Collection sampleCollection selected successfully");
  }   
}
