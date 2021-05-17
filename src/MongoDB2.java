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
 private Boolean InsertWord(String word, int position ,int DocNumber)
	{

		ArrayList<Integer>Arr=new ArrayList<Integer>();
		Arr.add(position);
		ArrayList<Document> Docs= new ArrayList<Document>();
		Document dc=new Document("doc",DocNumber);
		dc.append("positions",Arr);
		Docs.add(dc);
		Document doc=new Document("id",word);
		doc.append("docs", Docs);
		collection.insertOne(doc);

		return true;


	}
  
   Boolean find(String myword ,int  index, int Docnumber)
   {
	   Iterator it= collection.find().iterator();

	   Object next;
	   while(it.hasNext())
	   {
		   next= it.next();
		   Document doc =(Document)next;
		   String word= (String) doc.get("id");
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
					   List<Integer> mylink = (List<Integer>) dc.get("positions");
					   ArrayList<Integer> URL = new ArrayList<Integer>();
					   URL = (ArrayList<Integer>) mylink;
					   System.out.println(URL);
					   Object[] objectsfinal = URL.toArray();
					   for (Object ob : objectsfinal) {
					   	System.out.println((Integer)ob);
					   	if(index==	(Integer) ob)
						   {
						   	 return true;

						   }

					   }
					   // add position
					   URL.add(index);


					   ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
					   BasicDBObject query = new BasicDBObject();
					   query.put("id",myword);
					   BasicDBObject update = new BasicDBObject();
					   update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".positions",URL));
					   collection.updateOne(
							   query,update);

					   return true;
				   }

                      count++;
			   }
			   // add link not found with first position
			   ArrayList<Integer>Arr=new ArrayList<Integer>();
			   Arr.add(index);
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
	   InsertWord(myword,index,Docnumber);
   	 return false;
   }
	private Boolean InsertDoc(String word, int position ,int DocNumber)
	{

		ArrayList<Integer>Arr=new ArrayList<Integer>();
		Arr.add(position);
		ArrayList<Document> Docs= new ArrayList<Document>();
		Document dc=new Document("word",word);
		dc.append("positions",Arr);
		Docs.add(dc);
		Document doc=new Document("id",DocNumber);
		doc.append("words", Docs);
		collection.insertOne(doc);

		return true;


	}

	Boolean findDoc(String myword ,int  index, int Docnumber)
	{
		Iterator it= collection.find().iterator();

		Object next;
		while(it.hasNext())
		{
			next= it.next();
			Document doc =(Document)next;
			Integer docnum=-1;
			try {
				docnum = (Integer) doc.get("id");
			}
			catch (Exception e)
			{

			}

//			System.out.println(word);
			if(Docnumber==docnum) {
				List<String> Values = (List<String>) doc.get("words");
				ArrayList<String> arr = new ArrayList<String>();
				arr = (ArrayList<String>) Values;

				String[] arr2 = new String[arr.size()];
				Object[] objects = Values.toArray();
				int  count=0;
				for (Object obj : objects) {
					Document dc = (Document) obj;

					String Word = (String) dc.get("word");
					if(Word.equals(myword)) {

						List<Integer> mylink = (List<Integer>) dc.get("positions");
						ArrayList<Integer> URL = new ArrayList<Integer>();
						URL = (ArrayList<Integer>) mylink;
						System.out.println(URL);
						Object[] objectsfinal = URL.toArray();
						for (Object ob : objectsfinal) {
							System.out.println((Integer)ob);
							if(index==	(Integer) ob)
							{
								return true;

							}

						}
						// add position
						URL.add(index);


						ArrayList<Document> All=(ArrayList<Document>) doc.get("words");
						BasicDBObject query = new BasicDBObject();
						query.put("id",docnum);
						BasicDBObject update = new BasicDBObject();
						update.put("$set", new BasicDBObject("words."+Integer.toString(count)+".positions",URL));
						collection.updateOne(
								query,update);

						return true;
					}

					count++;
				}
				// add link not found with first position
				ArrayList<Integer>Arr=new ArrayList<Integer>();
				Arr.add(index);
				ArrayList<Document> Docs= new ArrayList<Document>();
				Document dc=new Document("word",myword);
				dc.append("positions",Arr);
				ArrayList<Document> All=(ArrayList<Document>) doc.get("words");
				All.add(dc);
				// update
				BasicDBObject query = new BasicDBObject();
				query.put("id",docnum);
				BasicDBObject update = new BasicDBObject();
				update.put("$set", new BasicDBObject("words",All));
				collection.updateOne(
						query,update);


				return true;
			}

		}
		// add word
		InsertDoc(myword,index,Docnumber);
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
	MongoDatabase database = mongoClient.getDatabase("try");
	MongoCollection<Document> collection = database.getCollection("ok");
		
			MongoDB2 Mon=new MongoDB2(collection);

    Mon.findDoc("Iten",10,16);
	  Mon.find("Radwa",12,16);
	System.out.println("Collection sampleCollection selected successfully");
  }   
}
