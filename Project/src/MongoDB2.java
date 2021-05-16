import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.types.ObjectId;


import java.util.List;
import java.util.Arrays;
import java.nio.CharBuffer;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoDB2 {
    // ...
	private MongoCollection<Document> collection;
 public MongoDB2( MongoCollection<Document> coll)
   {
	  collection=coll;

   }
   Boolean FindWord(String word, int position ,int DocNumber)
   {
	Document dc;
	try
	{
	
	 dc=  collection.find(new Document("id",word)).first();
	
    if(dc.get(Integer.toString(DocNumber))!=null)
    { 
	
     
     
		Document doc=(Document) dc.get(Integer.toString(DocNumber));
		dc.remove(doc);
	    if( doc.get(Integer.toString(position))==null)
		{
			
			collection.deleteOne(eq("_id", dc.get("_id")));
		    doc.append(Integer.toString(position)," ");
			dc.append(Integer.toString(DocNumber),doc);
			List<Document> list = new ArrayList<Document>();
			list.add(dc);
		   collection.insertOne(dc);
			  
		}
	
		  
	   }
	   else
	{   Document Link= new Document(Integer.toString(position)," ");
	Link.append("key", DocNumber);
	   collection.updateOne(
		eq("_id", dc.get("_id")),
		combine(set(Integer.toString(DocNumber), Link)),
		new UpdateOptions().upsert(true).bypassDocumentValidation(true));
     }
	
	 
		
	}
	catch(Exception e)
	{
		System.out.println("not found");
		Document doc=new Document("id",word);
		Document Link= new Document(Integer.toString(position)," ");
		Link.append("key", DocNumber);
		doc.append(Integer.toString(DocNumber), Link);
		List<Document> list = new ArrayList<Document>();
		list.add(doc);
       collection.insertOne(doc);
		return false;
	}
	
   
    return true;

	
   }
	 
    void insertword(String word, int position ,int DocNumber)
	{
	

     
	{
	
		Document Doc1=new Document("id",word);
	   
		Doc1.append(String.valueOf(DocNumber),position);
		String x=Doc1.toJson();
		
	
		System.out.println("x:"+x);
	  Doc1.getDate("1");
		List<Document> list = new ArrayList<Document>();
		list.add(Doc1);
       collection.insertOne(Doc1);
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
		
			MongoDB2 Mon=new MongoDB2(collection);
		
		Mon.FindWord("hello",1,15);
		
	System.out.println("Collection sampleCollection selected successfully");
  }   
}
