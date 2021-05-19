import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import java.sql.Array;
import java.util.Iterator;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.types.ObjectId;


import javax.print.Doc;
import java.util.List;
import java.util.Arrays;
import java.nio.CharBuffer;
import java.util.ArrayList;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class MongoDB2 {

	// ...
	private MongoCollection<Document> collection;
//	MongoDatabase database;
 public MongoDB2( MongoCollection<Document> coll )
   {
	  collection=coll;


   }
 private Boolean InsertWord(String word, int position ,int DocNumber,String type)
	{

		ArrayList<Document>Arr=new ArrayList<Document>();
		Document Title=new Document("index",position);
		Title.append("type",type);
		Arr.add(Title);
		ArrayList<Document> Docs= new ArrayList<Document>();
		Document dc=new Document("doc",DocNumber);
		dc.append("positions",Arr);
		Docs.add(dc);
		Document doc=new Document("id",word);
		doc.append("docs", Docs);
		collection.insertOne(doc);

		return true;


	}
  
   Boolean findWord(String myword ,int  index, int Docnumber,String type)
   {
	   Iterator it= collection.find().iterator();


	   Object next;
	   while(it.hasNext())
	   {

		   next= it.next();
		   Document doc =(Document)next;

			   String word= (String) doc.get("id");
        System.out.println(word);


		   System.out.println(word);
		   if(myword.equals(word)) {
			   List<String> Values = (List<String>) doc.get("docs");
			   ArrayList<String> arr = new ArrayList<String>();
			   arr = (ArrayList<String>) Values;

			   String[] arr2 = new String[arr.size()];
			   Object[] objects = Values.toArray();
			   int  count=0;
			   for (Object obj : objects) {
				   Document dc = (Document) obj;

				   Integer docnumber = (Integer) dc.get("doc");
				   if(docnumber==Docnumber) {
					   System.out.println(docnumber);
					   ArrayList<Document> mylink = (ArrayList<Document>)  dc.get("positions");

					  // System.out.println(URL);
					   Object[] objectsfinal = mylink.toArray();
					   for (Object ob : objectsfinal) {
					      Document my=(Document) ob;
					   	if(index==	(Integer) my.get("index"))
						   {
						   	 return true;

						   }

					   }
					   // add position
					   Document title=new Document("index",index);
					   title.append("type",type);
					   mylink.add(title);


					   ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
					   BasicDBObject query = new BasicDBObject();
					   query.put("id",myword);
					   BasicDBObject update = new BasicDBObject();
					   update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".positions",mylink));
					   collection.updateOne(
							   query,update);

					   return true;
				   }

                      count++;
			   }
			   // add link not found with first position
			   ArrayList<Document>Arr=new ArrayList<Document>();
			   Document title=new Document("index",index);
			   title.append("type",type);
			   Arr.add(title);
			   ArrayList<Document> Docs= new ArrayList<Document>();
			   Document dc=new Document("doc",Docnumber);
			   dc.append("positions",Arr);
			   ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
			   All.add(dc);
			   // update
			   BasicDBObject query = new BasicDBObject();
			   query.put("id",myword);
			   BasicDBObject update = new BasicDBObject();
			   update.put("$set", new BasicDBObject("docs",All));
			   collection.updateOne(
					   query,update);


			   return true;
		   }

	   }

	   // add word
	   System.out.println("insert");
	   InsertWord(myword,index,Docnumber,type);
   	 return false;
   }
   Boolean UpdateWords(String myword ,int  index, int Docnumber,String type)
   {
	   Iterator it= collection.find().iterator();


	   Object next;
	   while(it.hasNext())
	   {

		   next= it.next();
		   Document doc =(Document)next;

		   String word= (String) doc.get("id");
		   System.out.println(word);


		   System.out.println(word);
		   if(myword.equals(word)) {
			   List<String> Values = (List<String>) doc.get("docs");
			   ArrayList<String> arr = new ArrayList<String>();
			   arr = (ArrayList<String>) Values;

			   String[] arr2 = new String[arr.size()];
			   Object[] objects = Values.toArray();
			   int  count=0;
			   for (Object obj : objects) {
				   Document dc = (Document) obj;

				   Integer docnumber = (Integer) dc.get("doc");
				   if(docnumber==Docnumber) {

					   // add position
					   ArrayList<Document> newUpdate=new ArrayList<Document>();
					   Document title=new Document("index",index);
					   title.append("type",type);
					   newUpdate.add(title);


					   ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
					   BasicDBObject query = new BasicDBObject();
					   query.put("id",myword);
					   BasicDBObject update = new BasicDBObject();
					   update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".positions",newUpdate));
					   collection.updateOne(
							   query,update);

					   return true;
				   }

				   count++;
			   }
			   // add link not found with first position
			   ArrayList<Document>Arr=new ArrayList<Document>();
			   Document title=new Document("index",index);
			   title.append("type",type);
			   Arr.add(title);
			   ArrayList<Document> Docs= new ArrayList<Document>();
			   Document dc=new Document("doc",Docnumber);
			   dc.append("positions",Arr);
			   ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
			   All.add(dc);
			   // update
			   BasicDBObject query = new BasicDBObject();
			   query.put("id",myword);
			   BasicDBObject update = new BasicDBObject();
			   update.put("$set", new BasicDBObject("docs",All));
			   collection.updateOne(
					   query,update);


			   return true;
		   }

	   }

	   // add word
	   System.out.println("insert");
	   InsertWord(myword,index,Docnumber,type);
	   return false;
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
	MongoDatabase database = mongoClient.getDatabase("tr");
	MongoCollection<Document> collectionWord = database.getCollection("Words");
	  MongoCollection<Document> collectionLink = database.getCollection("Links");
		
			MongoDB2 ObjectWord=new MongoDB2(collectionWord);

ObjectWord.UpdateWords("Radwa",1,1,"iii");
    //ObjectDoc.findDoc("Iten",10,16);
	  //ObjectWord.findWord("Ra",12,7,"oh");
	System.out.println("Collection sampleCollection selected successfully");
  }   
}
