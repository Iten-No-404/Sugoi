package com.sugoi.back;
import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.*;
import org.bson.BsonDocument;
import org.bson.Document;


import java.util.ArrayList;
import java.util.*;

public class Link {
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


    public Link() {


    }

    public static void main(String[] argv) {


        Link ObjectDoc = new Link();
        ObjectDoc.DeleteWordsFromdocs("ll");

    }

    private Boolean InsertDoc(String word, int position, String DocNumber, String type, Boolean drop) {

        //  Insert  new URL
        ArrayList<Document> Arr = new ArrayList<Document>();
        // add index (position ) of Word
        Document words = new Document("index", position);
        // add type: h1,h2, header ,...
        words.append("type", type);
        Arr.add(words);
        ArrayList<Document> Docs = new ArrayList<Document>();
        Document dc = new Document("word", word);
        // add all indexes in array position which is array of documents contains index & type
        dc.append("positions", Arr);
        dc.append("drop", drop);
        Docs.add(dc);
        Document doc = new Document("id", DocNumber);
        // add  word for array of words & add it for URL
        doc.append("words", Docs);
        // insert in database
        collectionLink.insertOne(doc);

        return true;


    }

    // use it for build indexer for first time
    public Boolean findDoc(String myword, int index, String Docnumber, String type) {

        Object next;
        Iterator it = collectionLink.find(new BasicDBObject("id",Docnumber)).iterator();
        ArrayList<String> arr = new ArrayList<>();


        if (it.hasNext()) {


            next = it.next();
            Document doc = (Document) next;
            String docnum;
            docnum = (String) doc.get("id");
            // check if URL has been inserted before
            if (Docnumber.equals(docnum)) {

                List<String> Values = (List<String>) doc.get("words");

                Object[] objects = Values.toArray();
                int count = 0;
                // if found , check if word has been inserted before
                for (Object obj : objects) {
                    Document dc = (Document) obj;
                    String Word = (String) dc.get("word");
                    if (Word.equals(myword)) {
                        ArrayList<Document> URL = (ArrayList<Document>) dc.get("positions");
                        Object[] objectsfinal = URL.toArray();
                        // if found check if index has been inseted before
                        for (Object ob : objectsfinal) {
                            Document po = (Document) ob;
                            // if found it means error has been happened so return
                            if (index == (Integer) po.get("index")) {
                                return true;

                            }

                        }
                        // add new position to old URL and old word
                        Document newpo = new Document("index", index);
                        newpo.append("type", type);
                        URL.add(newpo);
                        ArrayList<Document> All = (ArrayList<Document>) doc.get("words");
                        BasicDBObject query = new BasicDBObject();
                        query.put("id", docnum);
                        BasicDBObject update = new BasicDBObject();
                        // update database with new values
                        update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".positions", URL));
                        collectionLink.updateOne(
                                query, update);

                        return true;
                    }

                    count++;
                }
                // add link not found with first position
                ArrayList<Document> Arr = new ArrayList<Document>();
                Document newone = new Document("index", index);
                newone.append("type", type);
                Arr.add(newone);
                ArrayList<Document> Docs = new ArrayList<Document>();
                Document dc = new Document("word", myword);
                dc.append("positions", Arr);
                dc.append("drop", false);
                ArrayList<Document> All = (ArrayList<Document>) doc.get("words");
                All.add(dc);
                // update database  with new values
                BasicDBObject query = new BasicDBObject();
                query.put("id", docnum);
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("words", All));
                collectionLink.updateOne(
                        query, update);


                return true;
            }

        }
        // add URL not found before
        InsertDoc(myword, index, Docnumber, type, false);
        return false;
    }

    // use it Update indexer
    public Boolean updateDocs(String myword, int index, String Docnumber, String type) {

        // TODO Here too! Don't forget about the while condition
        Iterator it = collectionLink.find(new BasicDBObject("id",Docnumber)).iterator();

        Object next;
        if (it.hasNext()) {
            next = it.next();
            Document doc = (Document) next;
            String docnum;

            docnum = (String) doc.get("id");
            // check if URL has been inserted before
            if (Docnumber.equals(docnum)) {
                List<String> Values = (List<String>) doc.get("words");
                ArrayList<String> arr = new ArrayList<String>();
                Object[] objects = Values.toArray();
                int count = 0;
                // if found check if word has been inserted before
                for (Object obj : objects) {
                    Document dc = (Document) obj;

                    String Word = (String) dc.get("word");
                    if (Word.equals(myword)) {

                        // if found  drop all position assuming it has been changed
                        if (!(Boolean) dc.get("drop")) {
                            // add your first position & type to old ULR & old word
                            ArrayList<Document> URL = new ArrayList<Document>();
                            Document words = new Document("index", index);
                            words.append("type", type);
                            URL.add(words);
                            ArrayList<Document> All = (ArrayList<Document>) doc.get("words");
                            BasicDBObject query = new BasicDBObject();
                            // update data base with new values
                            query.put("id", docnum);
                            BasicDBObject update = new BasicDBObject();
                            update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".positions", URL));
                            collectionLink.updateOne(
                                    query, update);
                            update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".drop", true));
                            collectionLink.updateOne(
                                    query, update);
                        }
                        // drop only first time
                        else {
                           // System.out.println("i am here");
                            ArrayList<Document> URL = (ArrayList<Document>) dc.get("positions");
                            // chek if position has inserted before
                            Object[] objectsfinal = URL.toArray();
                            for (Object ob : objectsfinal) {
                                Document my = (Document) ob;
                                // if it found it means error has happened so return
                                if (index == (Integer) my.get("index")) {

                                    return true;

                                }

                            }
                            //  add new postion ,type not found to old URL & old word
                            Document newpo = new Document("index", index);
                            newpo.append("type", type);
                            URL.add(newpo);
                            ArrayList<Document> All = (ArrayList<Document>) doc.get("words");
                            BasicDBObject query = new BasicDBObject();
                            query.put("id", docnum);
                            // update database with new values
                            BasicDBObject update = new BasicDBObject();
                            update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".positions", URL));
                            collectionLink.updateOne(
                                    query, update);

                        }
                        return true;
                    }

                    count++;
                }
                // add word not found with first position & type to old URL
                ArrayList<Document> Arr = new ArrayList<Document>();
                Document newone = new Document("index", index);
                newone.append("type", type);
                Arr.add(newone);
                ArrayList<Document> Docs = new ArrayList<Document>();
                Document dc = new Document("word", myword);
                dc.append("positions", Arr);
                dc.append("drop", true);
                ArrayList<Document> All = (ArrayList<Document>) doc.get("words");
                All.add(dc);
                // update database with new values
                BasicDBObject query = new BasicDBObject();
                query.put("id", docnum);
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("words", All));
                collectionLink.updateOne(
                        query, update);


                return true;
            }

        }
        // add  new URL not found
        InsertDoc(myword, index, Docnumber, type, true);
        return false;
    }

    public void Resetdrop() {
        // TODO Here too! Don't forget about the while condition
        Iterator it = collectionLink.find().iterator();
        Object next;
        // reset the value of Boolean drop to false after finishing update for all URLs
        while (it.hasNext()) {

            next = it.next();
            Document doc = (Document) next;

            String docnum = (String) doc.get("id");
            List<String> Values = (List<String>) doc.get("words");

            Object[] objects = Values.toArray();
            int count = 0;
            for (Object obj : objects) {

                // Update your data base with new values
                BasicDBObject query = new BasicDBObject();
                query.put("id", docnum);
                BasicDBObject update = new BasicDBObject();
                update.put("$set", new BasicDBObject("words." + Integer.toString(count) + ".drop", true));
                collectionLink.updateOne(query, update);
                count++;

            }
        }
    }

    // it is used to check if the word has been deleted from URL to delete it from Links collection & Words collections
    public Boolean DeleteWordsFromdocs(String docvalue) {


        Iterator it = collectionLink.find().iterator();
        ArrayList<String> arr = new ArrayList<>();

        Object next;
        while (it.hasNext()) {
            // go to the   current URL data after update
            next = it.next();
            Document doc = (Document) next;
            String docnum;

            docnum = (String) doc.get("id");
            if (docvalue.equals(docnum)) {
                List<String> Values = (List<String>) doc.get("words");

                // check if any word sitll its value with  false drop so delete it which means not updated ( deleted from URL)
                Object[] objects = Values.toArray();
                int count = 0;
                for (Object obj : objects) {
                    Document dc = (Document) obj;

                    String Word = (String) dc.get("word");
                    Boolean drop = (Boolean) dc.get("drop");
                    if (drop == false) //delete the word if the drop still false which means the word deleted from URL
                    {
                        // remove it from URl
                        Values.remove(count);
                        // add it to list to know what is the removed words
                        arr.add(Word);


                    }
                    count++;

                }
                // update database by delete word from URL
                BasicDBObject query = new BasicDBObject();
                query.put("id", docnum);
                // check if URL has another Words ( not empty website)
                if (Values.size() > 0) {

                    BasicDBObject update = new BasicDBObject();
                    update.put("$set", new BasicDBObject("words", Values));
                    // update database
                    collectionLink.updateOne(
                            query, update);
                }
                // if URL becomes empty website , delete it from database
                else {

                    collectionLink.deleteOne(query);
                }
                // use the array which contains removed words to delete it from Words collection
                DeleteWordsFroWords(arr, docvalue);
                return true;

            }
        }
        return false;
    }

    // don't call it , it is already called in previous  function
    private void DeleteWordsFroWords(ArrayList<String> arr, String docValue) {

        // the first parameter is the removed words , the second is the URL which remove the words

        // the first parameter is the removed words , the second is the URL which remove the words
        Iterator it = collectionWord.find().iterator();


        Object next;
        // loop on array of removed words to delete them
        for (int i = 0; i < arr.size(); i++) {
            while (it.hasNext()) {
                next = it.next();
                Document doc = (Document) next;
                // check if the current word in database
                String word = (String) doc.get("id");
                if (word.equals(arr.get(i))) {
                    List<String> Values = (List<String>) doc.get("docs");
                    // if found check if current URL if found
                    Object[] objects = Values.toArray();
                    int count = 0;
                    for (Object obj : objects) {
                        Document dc = (Document) obj;

                        String docnumber = (String) dc.get("doc");
                        if (docnumber.equals(docValue)) {
                            // if found remove URL
                            Values.remove(count);
                            break;
                        }
                        count++;
                    }
                    // update database
                    BasicDBObject query = new BasicDBObject();
                    query.put("id", word);
                    // check if words still found in any URL
                    if (Values.size() > 0) {
                        // update database to remove URL
                        BasicDBObject update = new BasicDBObject();
                        update.put("$set", new BasicDBObject("docs", Values));
                        collectionWord.updateOne(
                                query, update);

                    } else {
                        // if not found delete it from data base
                        collectionWord.deleteOne(query);
                    }

                }
            }

        }
    }
}