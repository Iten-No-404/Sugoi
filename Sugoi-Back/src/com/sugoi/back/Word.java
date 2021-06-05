package com.sugoi.back;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.Iterator;

import org.bson.Document;


import java.util.List;
import java.util.ArrayList;

public class Word {

    // variables for create connection with data base
    final static ConnectionString Connection = new ConnectionString("mongodb://127.0.0.1:27017");
    final static MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(Connection).retryWrites(true).build();
    final static MongoClient mongoClient = MongoClients.create(settings);
    // get database
    final static MongoDatabase database = mongoClient.getDatabase("Indexer");
    // get collection for Words
    final static MongoCollection<Document> collectionWord = database.getCollection("Words");
    // get Collection for Links
    final static MongoCollection<Document> collectionLink = database.getCollection("Links");


    public Word() {


    }

    public static void main(String[] argv) {


//			Word ObjectWord=new Word();
//ObjectWord.findWord("Esraa",5,"ll","ii");
//	  ObjectWord.findWord("Esraa",7,"kk","ii");

//	System.out.println("Collection sampleCollection selected successfully");
    }

    private Boolean InsertWord(String word, int position, String DocNumber, String type, Boolean drop, int lengthofdoc) {

        ArrayList<Document> Arr = new ArrayList<Document>();
        // postion of word
        Document Title = new Document("index", position);
        // type :H1 ,header ,// etc
        Title.append("type", type);
        Arr.add(Title);
        // add URL
        ArrayList<Document> Docs = new ArrayList<Document>();
        Document dc = new Document("doc", DocNumber);
        // add TF
        dc.append("TF", 1.0 / lengthofdoc);
        // should i  do normalize?
        // add our index in array of positions which is array of documnets contains type &index
        dc.append("positions", Arr);
        // Boolean used in Update & delete
        dc.append("drop", drop);
        Docs.add(dc);
        // insert word with its all inforamtion
        Document doc = new Document("id", word);
        doc.append("docs", Docs);
        // add IDE
        doc.append("IDE", getNDocuments() / 1);
        collectionWord.insertOne(doc);

        return true;


    }

    // use this function to bulid indexer for first time
    Boolean findWord(String myword, int index, String Docnumber, String type, int lengthofdoc) {
        Iterator it = collectionWord.find(new Document("id", myword)).iterator();


        Object next;
        while (it.hasNext()) {

            next = it.next();
            Document doc = (Document) next;
            //check if the word has been inserted before or not
            String word = (String) doc.get("id");
//           System.out.println(word);


//		   System.out.println(word);
            if (myword.equals(word)) {
                // if found check if URL has been inserted before or not
                List<String> Values = (List<String>) doc.get("docs");
                ArrayList<String> arr = new ArrayList<String>();

                Object[] objects = Values.toArray();
                int count = 0;
                for (Object obj : objects) {
                    Document dc = (Document) obj;
                    // if found check if position has been inserted
                    String docnumber = (String) dc.get("doc");
                    double TF = (double) dc.get("TF");
                    //  System.out.println(TF);
                    if (docnumber.equals(Docnumber)) {
                        //   System.out.println(docnumber);
                        ArrayList<Document> mylink = (ArrayList<Document>) dc.get("positions");


                        Object[] objectsfinal = mylink.toArray();
                        for (Object ob : objectsfinal) {
                            Document my = (Document) ob;
                            // if position found return and don't add it  , it means error happen!
                            if (index == (Integer) my.get("index")) {
                                return true;

                            }

                        }
                        // add position to URL has been found
                        Document title = new Document("index", index);
                        title.append("type", type);
                        mylink.add(title);


                        ArrayList<Document> All = (ArrayList<Document>) doc.get("docs");
                        BasicDBObject query = new BasicDBObject();
                        query.put("id", myword);
                        BasicDBObject update = new BasicDBObject();
                        // update database with new  index and TF
                        update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".positions", mylink));
                        collectionWord.updateOne(
                                query, update);
                        TF = mylink.size() / lengthofdoc;
                        // System.out.println("ia m in");
                        update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".TF", TF));
                        collectionWord.updateOne(
                                query, update);

                        return true;
                    }

                    count++;
                }
                // add link not found with first position
                ArrayList<Document> Arr = new ArrayList<Document>();
                Document title = new Document("index", index);
                title.append("type", type);
                int IDE = getNDocuments();
                Arr.add(title);
                ArrayList<Document> Docs = new ArrayList<Document>();
                Document dc = new Document("doc", Docnumber);
                // add to URL its first index & type &TF
                dc.append("positions", Arr);
                dc.append("drop", false);
                dc.append("TF", 1.0 / lengthofdoc);
                ArrayList<Document> All = (ArrayList<Document>) doc.get("docs");

                All.add(dc);
                // update data base with new URL &IDE
                BasicDBObject query = new BasicDBObject();
                query.put("id", myword);
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("docs", All));
                collectionWord.updateOne(
                        query, update);
                update.put("$set", new BasicDBObject("IDE", IDE / All.size()));
                collectionWord.updateOne(
                        query, update);


                return true;
            }

        }

        // add word not found before
        // System.out.println("insert");
        InsertWord(myword, index, Docnumber, type, false, lengthofdoc);
        return false;
    }

    int getNDocuments() {

        // get the total number of URL
        long totURL = collectionLink.countDocuments();
        return (int) totURL;


    }

    // use this function to Update indexer
    Boolean UpdateWords(String myword, int index, String Docnumber, String type, int lengthofdoc) {
        Iterator it = collectionWord.find().iterator();


        Object next;
        while (it.hasNext()) {

            next = it.next();
            Document doc = (Document) next;
            // check if the word has been inserted before
            String word = (String) doc.get("id");
            //System.out.println(word);


            //  System.out.println(word);
            if (myword.equals(word)) {
                // if found check if URL has been inserted before
                List<String> Values = (List<String>) doc.get("docs");
                ArrayList<String> arr = new ArrayList<String>();

                Object[] objects = Values.toArray();
                int count = 0;
                for (Object obj : objects) {
                    Document dc = (Document) obj;

                    String docnumber = (String) dc.get("doc");
                    if (docnumber.equals(Docnumber)) {

                        // Drop previous position if URL found ( assume position has been changed)
                        // Drop it in only first time
                        Boolean drop = (Boolean) dc.get("drop");
                        double TF =  ((Number)dc.get("TF")).doubleValue() ;
                        if (drop == false) {
                            //	System.out.println(" i am here");
                            ArrayList<Document> newUpdate = new ArrayList<Document>();
                            Document title = new Document("index", index);
                            title.append("type", type);
                            newUpdate.add(title);

                            // add position ,type & update TF to old URL
                            ArrayList<Document> All = (ArrayList<Document>) doc.get("docs");
                            BasicDBObject query = new BasicDBObject();
                            query.put("id", myword);
                            BasicDBObject update = new BasicDBObject();
                            // update database
                            update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".positions", newUpdate));
                            collectionWord.updateOne(
                                    query, update);
                            update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".drop", true));
                            collectionWord.updateOne(
                                    query, update);

                            update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".TF", 1.0 / lengthofdoc));
                            collectionWord.updateOne(
                                    query, update);

                        }
                        // drop only for first time
                        else {
                            // check if position  has been instered before
                            ArrayList<Document> mylink = (ArrayList<Document>) dc.get("positions");
                            Object[] objectsfinal = mylink.toArray();
                            for (Object ob : objectsfinal) {
                                Document my = (Document) ob;
                                // if it found it means error has happened so return
                                if (index == (Integer) my.get("index")) {

                                    return true;

                                }

                            }
                            // if not found add position ,type and update TF to old URL
                            Document title = new Document("index", index);
                            title.append("type", type);
                            mylink.add(title);

                            TF = mylink.size() / (double)lengthofdoc;
                            ArrayList<Document> All = (ArrayList<Document>) doc.get("docs");
                            BasicDBObject query = new BasicDBObject();
                            query.put("id", myword);
                            BasicDBObject update = new BasicDBObject();
                            // update database
                            update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".positions", mylink));
                            collectionWord.updateOne(
                                    query, update);

                            update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".TF", TF));
                            collectionWord.updateOne(
                                    query, update);

                        }

                        return true;
                    }

                    count++;
                }
                // add link not found with first position
                ArrayList<Document> Arr = new ArrayList<Document>();
                Document title = new Document("index", index);
                title.append("type", type);
                Arr.add(title);
                ArrayList<Document> Docs = new ArrayList<Document>();
                Document dc = new Document("doc", Docnumber);
                dc.append("positions", Arr);
                dc.append("drop", true);
                dc.append("TF", 1);
                ArrayList<Document> All = (ArrayList<Document>) doc.get("docs");
                All.add(dc);
                int IDE = getNDocuments();
                // update database with new URL
                BasicDBObject query = new BasicDBObject();
                query.put("id", myword);
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("docs", All));
                collectionWord.updateOne(
                        query, update);
                update.put("$set", new BasicDBObject("IDE", IDE / All.size()));
                collectionWord.updateOne(
                        query, update);

                return true;
            }

        }

        // add word not found
        // System.out.println("insert");
        InsertWord(myword, index, Docnumber, type, true, lengthofdoc);
        return false;
    }

    void Resetdrop() {
        // reset the value of Boolean drop to false after finishing update for all words
        Iterator it = collectionWord.find().iterator();
        Object next;
        while (it.hasNext()) {

            next = it.next();
            Document doc = (Document) next;
            String word = (String) doc.get("id");
            List<String> Values = (List<String>) doc.get("docs");
            Object[] objects = Values.toArray();
            int count = 0;
            for (Object obj : objects) {
                BasicDBObject query = new BasicDBObject();
                query.put("id", word);
                // update database with new value
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("docs." + Integer.toString(count) + ".drop", false));
                collectionWord.updateOne(
                        query, update);
                count++;

            }

        }

    }

    Boolean UpdateIDE() {
        int NDocuments = getNDocuments();
        Iterator it = collectionWord.find().iterator();


        Object next;
        // Update IDE for all words after insert or update
        while (it.hasNext()) {

            next = it.next();
            Document doc = (Document) next;
            String word = (String) doc.get("id");
            int IDE = (int) doc.get("IDE");
            ArrayList<Document> All = (ArrayList<Document>) doc.get("docs");
            BasicDBObject query = new BasicDBObject();
            query.put("id", word);
            // update database with new value
            BasicDBObject update = new BasicDBObject();
            update.put("$set", new BasicDBObject("IDE", IDE / All.size()));
            collectionWord.updateOne(
                    query, update);
        }
        return true;
    }
}
