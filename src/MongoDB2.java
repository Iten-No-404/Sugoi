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
 private Boolean InsertWord(String word, int position ,int DocNumber,String type,Boolean drop)
	{

		ArrayList<Document>Arr=new ArrayList<Document>();
		Document Title=new Document("index",position);
		Title.append("type",type);
		Arr.add(Title);
		ArrayList<Document> Docs= new ArrayList<Document>();
		Document dc=new Document("doc",DocNumber);
		dc.append("TF",1); // can be normalized by give the length of doc ????????????????? from where from stemming ?!
		// should i  do normalize?

		dc.append("positions",Arr);
		dc.append("drop",drop);
		Docs.add(dc);
		Document doc=new Document("id",word);
		doc.append("docs", Docs);
		doc.append("IDE",getNDocuments()/1);
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
				   Integer TF=(Integer) dc.get("TF");
				   System.out.println(TF);
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
					   TF++;
					   System.out.println("ia m in");
					   update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".TF",TF));
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
			   int IDE=getNDocuments();
			   Arr.add(title);
			   ArrayList<Document> Docs= new ArrayList<Document>();
			   Document dc=new Document("doc",Docnumber);
			   dc.append("positions",Arr);
			   dc.append("drop",false);
			   dc.append("TF",1);
			   ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");

			   All.add(dc);
			   // update
			   BasicDBObject query = new BasicDBObject();
			   query.put("id",myword);
			   BasicDBObject update = new BasicDBObject();
			   update.put("$set", new BasicDBObject("docs",All));
			   collection.updateOne(
					   query,update);
			   update.put("$set", new BasicDBObject("IDE",IDE/All.size()));
			   collection.updateOne(
					   query,update);


			   return true;
		   }

	   }

	   // add word
	   System.out.println("insert");
	   InsertWord(myword,index,Docnumber,type,false);
   	 return false;
   }
   int getNDocuments()
   {
          // i assume i add words before docs
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

	   MongoCollection<Document> collectionLink = database.getCollection("Links");
	   long x=collectionLink.countDocuments();
	   int y=(int)x;
	    return y+1;

   }
   Boolean UpdateWords(String myword ,int  index, int Docnumber,String type )
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
					    Boolean drop= (Boolean)  dc.get("drop");
					   Integer TF=(Integer) dc.get("TF");
					    if(drop==false) {
					    	System.out.println(" i am here");
							ArrayList<Document> newUpdate = new ArrayList<Document>();
							Document title = new Document("index", index);
							title.append("type", type);
							newUpdate.add(title);


							ArrayList<Document> All = (ArrayList<Document>) doc.get("docs");
							BasicDBObject query = new BasicDBObject();
							query.put("id", myword);
							BasicDBObject update = new BasicDBObject();
							update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".positions", newUpdate));
							collection.updateOne(
									query, update);
							update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".drop", true));
							collection.updateOne(
									query, update);

							update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".TF",1));
							collection.updateOne(
									query,update);

						}
					    else
						{
							ArrayList<Document> mylink = (ArrayList<Document>)  dc.get("positions");
							Object[] objectsfinal = mylink.toArray();
							for (Object ob : objectsfinal) {
								Document my=(Document) ob;
								if(index==	(Integer) my.get("index"))
								{
									return true;

								}

							}

							Document title=new Document("index",index);
							title.append("type",type);
							mylink.add(title);

                                TF++;
							ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
							BasicDBObject query = new BasicDBObject();
							query.put("id",myword);
							BasicDBObject update = new BasicDBObject();
							update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".positions",mylink));
							collection.updateOne(
									query,update);

							update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".TF",TF));
							collection.updateOne(
									query,update);

						}

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
			   dc.append("drop",true);
			   dc.append("TF",1);
			   ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
			   All.add(dc);
			   int IDE=getNDocuments();
			   // update
			   BasicDBObject query = new BasicDBObject();
			   query.put("id",myword);
			   BasicDBObject update = new BasicDBObject();
			   update.put("$set", new BasicDBObject("docs",All));
			   collection.updateOne(
					   query,update);
			   update.put("$set", new BasicDBObject("IDE",IDE/All.size()));
			   collection.updateOne(
					   query,update);

			   return true;
		   }

	   }

	   // add word
	   System.out.println("insert");
	   InsertWord(myword,index,Docnumber,type,true);
	   return false;
   }
   void  Resetdrop()
   {
	   Iterator it= collection.find().iterator();
	   Object next;
	   while(it.hasNext()) {

		   next = it.next();
		   Document doc = (Document) next;

		   String word = (String) doc.get("id");
		   List<String> Values = (List<String>) doc.get("docs");

		   Object[] objects = Values.toArray();
		   int  count=0;
		   for (Object obj : objects) {



			   BasicDBObject query = new BasicDBObject();
			   query.put("id",word);
			   BasicDBObject update = new BasicDBObject();
			   update.put("$set", new BasicDBObject("docs."+Integer.toString(count)+".drop",false));
			   collection.updateOne(
					   query,update);
			   count++;

		   }

	   }

   }
Boolean UpdateIDE()
{
  int NDocuments=getNDocuments();
	Iterator it= collection.find().iterator();


	Object next;
	while(it.hasNext()) {

		next = it.next();
		Document doc = (Document) next;
        String word=(String) doc.get("id");
		int IDE = (int) doc.get("IDE");
		ArrayList<Document> All=(ArrayList<Document>) doc.get("docs");
		BasicDBObject query = new BasicDBObject();
		query.put("id",word);
		BasicDBObject update = new BasicDBObject();
//		System.out.println(IDE);
//		System.out.println(All.size());
		update.put("$set", new BasicDBObject("IDE",IDE/All.size()));
		collection.updateOne(
				query,update);
	}
	return true;
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

ObjectWord.UpdateIDE();
    //ObjectDoc.findDoc("Iten",10,16);
	//  ObjectWord.findWord("Ra",12,7,"oh");
	System.out.println("Collection sampleCollection selected successfully");
  }   
}
