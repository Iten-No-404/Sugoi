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


    public MongoDB( MongoCollection<Document> coll )
    {
        collection=coll;


    }
    private Boolean InsertDoc(String word, int position ,int DocNumber,String type)
    {


        ArrayList<Document>Arr=new ArrayList<Document>();
        Document words=new Document("index",position);
        words.append("type",type);
        Arr.add(words);
        ArrayList<Document> Docs= new ArrayList<Document>();
        Document dc=new Document("word",word);
        dc.append("positions",Arr);
        Docs.add(dc);
        Document doc=new Document("id",DocNumber);
        doc.append("words", Docs);
        collection.insertOne(doc);

        return true;


    }

    Boolean findDoc(String myword ,int  index, int Docnumber,String type)
    {
        Iterator it= collection.find().iterator();

        Object next;
        while(it.hasNext())
        {
            next= it.next();
            Document doc =(Document)next;
            Integer docnum=-1;

            docnum = (Integer) doc.get("id");


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

                        ArrayList<Document> URL = (ArrayList<Document>) dc.get("positions");

                        Object[] objectsfinal = URL.toArray();
                        for (Object ob : objectsfinal) {
                            Document po= (Document) ob;
                            if(index==	(Integer) po.get("index"))
                            {
                                return true;

                            }

                        }
                        // add position
                        Document newpo=new Document("index",index);
                        newpo.append("type",type);
                        URL.add(newpo);


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
                ArrayList<Document>Arr=new ArrayList<Document>();
                Document newone=new Document("index",index);
                newone.append("type",type);
                Arr.add(newone);

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
        InsertDoc(myword,index,Docnumber,type);
        return false;
    }
  Boolean updateDocs(String myword ,int  index, int Docnumber,String type)
  {

      Iterator it= collection.find().iterator();

      Object next;
      {
          next= it.next();
          Document doc =(Document)next;
          Integer docnum=-1;

          docnum = (Integer) doc.get("id");


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

                      // add position
                   ArrayList<Document>URL=new ArrayList<Document>();
                   Document words=new Document("index",index);
                   words.append("type",type);
                   URL.add(words);




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
              ArrayList<Document>Arr=new ArrayList<Document>();
              Document newone=new Document("index",index);
              newone.append("type",type);
              Arr.add(newone);
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
      InsertDoc(myword,index,Docnumber,type);
      return false;
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
        MongoDatabase database = mongoClient.getDatabase("tr");

        MongoCollection<Document> collectionLink = database.getCollection("Links");


        MongoDB ObjectDoc=new MongoDB(collectionLink);

        ObjectDoc.updateDocs("Esraa",8,17,"uuu");
    }
}