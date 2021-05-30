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
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MongoDB {
    final  static ConnectionString Connection=new ConnectionString("mongodb://127.0.0.1:27017");
    final  static  MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(Connection).retryWrites(true).build();
    final  static  MongoClient mongoClient = MongoClients.create(settings);
    final static MongoDatabase database = mongoClient.getDatabase("tr");
    final static MongoCollection<Document> collectionWord = database.getCollection("Words");
    final static MongoCollection<Document> collectionLink = database.getCollection("Links");


    public MongoDB( )
    {



    }

    private Boolean InsertDoc(String word, int position ,String DocNumber,String type,Boolean drop)
    {


        ArrayList<Document>Arr=new ArrayList<Document>();
        Document words=new Document("index",position);
        words.append("type",type);
        Arr.add(words);
        ArrayList<Document> Docs= new ArrayList<Document>();
        Document dc=new Document("word",word);
        dc.append("positions",Arr);
        dc.append("drop",drop);
        Docs.add(dc);
        Document doc=new Document("id",DocNumber);
        doc.append("words", Docs);
        collectionLink.insertOne(doc);

        return true;


    }

    public Boolean findDoc(String myword ,int  index, String Docnumber,String type)
    {
        Iterator it= collectionLink.find().iterator();

        Object next;
        while(it.hasNext())
        {
            next= it.next();
            Document doc =(Document)next;
            String docnum;

            docnum = (String) doc.get("id");


//			System.out.println(word);
            if(Docnumber.equals(docnum)) {
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
                        collectionLink.updateOne(
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
                dc.append("drop",false);
                ArrayList<Document> All=(ArrayList<Document>) doc.get("words");
                All.add(dc);
                // update
                BasicDBObject query = new BasicDBObject();
                query.put("id",docnum);
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("words",All));
                collectionLink.updateOne(
                        query,update);


                return true;
            }

        }
        // add word
        InsertDoc(myword,index,Docnumber,type,false);
        return false;
    }
    public Boolean updateDocs(String myword ,int  index, String Docnumber,String type)
  {

      Iterator it= collectionLink.find().iterator();

      Object next;
      while(it.hasNext())
      {
          next= it.next();
          Document doc =(Document)next;
          String docnum;

          docnum =(String) doc.get("id");


//			System.out.println(word);
          if(Docnumber.equals(docnum)) {
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

                      System.out.println("hhhh");
                      // add position
                      if(!(Boolean) dc.get("drop")) {
                          System.out.println("hhhh");
                          ArrayList<Document> URL = new ArrayList<Document>();
                          Document words = new Document("index", index);
                          words.append("type", type);
                          URL.add(words);


                          ArrayList<Document> All = (ArrayList<Document>) doc.get("words");
                          BasicDBObject query = new BasicDBObject();
                          query.put("id", docnum);
                          BasicDBObject update = new BasicDBObject();
                          update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".positions", URL));
                          collectionLink.updateOne(
                                  query, update);
                          update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".drop", true));
                          collectionLink.updateOne(
                                  query, update);
                      }
                      else
                      {
                          System.out.println("i am here");
                          ArrayList<Document> URL = (ArrayList<Document>) dc.get("positions");
                          Document newpo=new Document("index",index);
                          newpo.append("type",type);
                          URL.add(newpo);


                          ArrayList<Document> All=(ArrayList<Document>) doc.get("words");
                          BasicDBObject query = new BasicDBObject();
                          query.put("id",docnum);
                          BasicDBObject update = new BasicDBObject();
                          update.put("$set", new BasicDBObject("words."+Integer.toString(count)+".positions",URL));
                          collectionLink.updateOne(
                                  query,update);

                      }
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
              dc.append("drop",true);
              ArrayList<Document> All=(ArrayList<Document>) doc.get("words");
              All.add(dc);
              // update
              BasicDBObject query = new BasicDBObject();
              query.put("id",docnum);
              BasicDBObject update = new BasicDBObject();
              update.put("$set", new BasicDBObject("words",All));
              collectionLink.updateOne(
                      query,update);


              return true;
          }

      }
      // add word
      InsertDoc(myword,index,Docnumber,type,true);
      return false;
  }
    public void  Resetdrop() {
        Iterator it = collectionLink.find().iterator();
        Object next;
        while (it.hasNext()) {

            next = it.next();
            Document doc =(Document)next;

            Integer docnum = (Integer) doc.get("id");
            List<String> Values = (List<String>) doc.get("words");

            Object[] objects = Values.toArray();
            int count = 0;
            for (Object obj : objects) {


                BasicDBObject query = new BasicDBObject();
                query.put("id", docnum);
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".drop", true));
                collectionLink.updateOne(query, update);
                count++;

            }
        }
    }
    public Boolean DeleteWordsFromdocs(String docvalue) {

        Iterator it = collectionLink.find().iterator();
        ArrayList<String>arr=new ArrayList<>();

        Object next;
        while (it.hasNext()) {
            next = it.next();
            Document doc = (Document) next;
            String docnum;

            docnum = (String) doc.get("id");
            if (docvalue.equals(docnum)) {
                List<String> Values = (List<String>) doc.get("words");


                Object[] objects = Values.toArray();
                int count = 0;
                for (Object obj : objects) {
                    Document dc = (Document) obj;

                    String Word = (String) dc.get("word");
                    Boolean drop=(Boolean) dc.get("drop");
                    if(drop==false) //delete
                    {
                             Values.remove(count);
                             arr.add(Word);


                    }
                    count++;

                }
                BasicDBObject query = new BasicDBObject();
                query.put("id", docnum);
                if(Values.size()>0) {

                    BasicDBObject update = new BasicDBObject();
                    update.put("$set", new BasicDBObject("words", Values));
                    collectionLink.updateOne(
                            query, update);
                }
                else
                {

                    collectionLink.deleteOne(query);
                }

                DeleteWordsFroWords(arr,docvalue);
                return  true;

            }


        }
        return  false;
    }
    public void DeleteWordsFroWords(ArrayList<String>arr,String docValue) {


        Iterator it = collectionWord.find().iterator();


        Object next;
        for (int i = 0; i < arr.size(); i++) {
            while (it.hasNext()) {
                next = it.next();
                Document doc = (Document) next;


                String word = (String) doc.get("id");
                if (word.equals(arr.get(i))) {
                    List<String> Values = (List<String>) doc.get("docs");


                    Object[] objects = Values.toArray();
                    int count = 0;
                    for (Object obj : objects) {
                        Document dc = (Document) obj;

                        String docnumber = (String) dc.get("doc");
                        if (docnumber.equals(docValue)) {

                                    Values.remove(count);
                        }
                        count++;
                    }
                    BasicDBObject query = new BasicDBObject();
                    query.put("id",word);
                    if(Values.size()>0) {

                        BasicDBObject update = new BasicDBObject();
                        update.put("$set", new BasicDBObject("docs",Values));
                        collectionWord.updateOne(
                                query,update);

                    }
                    else
                    {

                        collectionWord.deleteOne(query);
                    }

                }
            }

        }
    }



    public static void main(String[] argv) {



        MongoDB ObjectDoc=new MongoDB();
         ObjectDoc.DeleteWordsFromdocs("ll");
     //ObjectDoc.findDoc("Esraa",7,"ll","uuu");
    }
}