package com.sugoi.back;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.*;

import java.util.ArrayList;
import java.util.*;

import org.jsoup.safety.Whitelist;


import java.util.Iterator;


import java.util.ArrayList;
import java.util.*;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.select.Elements;

import javax.print.Doc;

public class Indexer extends Thread {
    final static ConnectionString Connection = new ConnectionString("mongodb://127.0.0.1:27017");
    final static MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(Connection).retryWrites(true).build();
    final static MongoClient mongoClient = MongoClients.create(settings);
    // get database
    final static MongoDatabase database = mongoClient.getDatabase("URLs");
    // get collection for Words
    final static MongoCollection<org.bson.Document> collectionHTML = database.getCollection("HTML");
    // get database
    final static MongoDatabase databaseindexer = mongoClient.getDatabase("Indexer");
    // get Collection for Links
    final static MongoCollection<Document> collectionWord = databaseindexer.getCollection("Words");
    // get Collection for Links
    final static MongoCollection<Document> collectionLink = databaseindexer.getCollection("Links");
    //

    public Indexer() {

    }

    public static void main(String argv[]) {
        if (collectionWord.countDocuments() == 0 & collectionLink.countDocuments() == 0)
            Definitions.CHOOSE_INDEX = true;
        else
            Definitions.CHOOSE_INDEX = false;

        Indexer[] indexers = new Indexer[Definitions.NUM_THREADS];
        for (int i = 0; i < Definitions.NUM_THREADS; i++) {
            indexers[i] = new Indexer();
        }

        for (int i = 0; i < Definitions.NUM_THREADS; i++) {
            System.out.println(indexers[i].getName() + " is starting...");
            indexers[i].start();
        }
        for (int i = 0; i < Definitions.NUM_THREADS; i++) {
            try {
                indexers[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        Parse_HTML();
    }

    public Boolean Parse_HTML() {

        // it should be edited to get link from DB
        Stemmer stemmer = new Stemmer();
        // TODO: remove if & else from here if you will use Thread !!!!!!!!!!!!!!!!!!!!!!!!!!!! SO Important
        // I really don't understand why?

        while (collectionHTML.countDocuments() > 0) {
            // connect to link
            Document doc = collectionHTML.findOneAndDelete(new Document());

            // get URL of page
            String URL = (String) doc.get("URL");
            String HTML = (String) doc.get("HTML");
            System.out.println(Thread.currentThread().getName() + ": \t I'm now stemming " + URL);

            // select all tags and words from HTML page
            org.jsoup.nodes.Document jsoupsecnod;
            jsoupsecnod = Jsoup.parse(HTML);
            Elements elements = jsoupsecnod.select("*");
            String text = Jsoup.clean(HTML.toString(), Whitelist.none());

            // only to get length to use it in TF
            String AllWords[] = stemmer.Spliter(text);

            int j = 0;
            // get all elemnts
            for (Element e : elements) {

                String[] words = stemmer.Spliter(e.text());
                for (int i = 0; i < words.length; i++) {

                    // If the this "word" is a number, skip it.
                    if (isNumeric(words[i])) continue;

                    // choose to build from zero or update
                    if (Definitions.CHOOSE_INDEX) {
                        // build indexer
//                        System.out.println("i am building  " + URL + " " + Thread.currentThread().getName());
                        Build_Indexer(URL, e.tagName(), j, words[i], AllWords.length);

                    } else {
                        // update indexer
                        //System.out.println("i am updating");
                        Update_Indexer(URL, e.tagName(), j, words[i], AllWords.length);
                    }
                    j++;
                }


            }
            if (Definitions.CHOOSE_INDEX) {
                // Update IDE
                Word Words = new Word();
                Words.UpdateIDE();
            } else {
                Link Links = new Link();
                Word Words = new Word();
                // delete removed words
                Links.DeleteWordsFromdocs(URL);
                // reset the status
                Links.Resetdrop();
                Words.Resetdrop();
                // Update IDE
                Words.UpdateIDE();
            }
        }
        System.out.println(Thread.currentThread().getName() + " terminating.");
        return true;
    }

    public void Build_Indexer(String URL, String type, int index, String word, int lengthofdoc) {
        // steps of building indexer
        Stemmer stemmer = new Stemmer();
        // stemm words
        Link Links = new Link();
        Word Words = new Word();


        word = stemmer.PorterStemming(word);
        // insert words
        if (word != null) {

            //insert words

            Words.findWord(word, index, URL, type, lengthofdoc);


            // insert docs

            Links.findDoc(word, index, URL, type);


        }


    }

    public void Update_Indexer(String URL, String type, int index, String word, int lengthofdoc) {
        // steps of Update indexer
        Stemmer stemmer = new Stemmer();
        Link Links = new Link();
        Word Words = new Word();
        // steem words
        word = stemmer.PorterStemming(word);
        if (word != null) {

            // update words

            Words.UpdateWords(word, index, URL, type, lengthofdoc);

            // update links

            Links.updateDocs(word, index, URL, type);

        }


    }

    public void CreateThreads() {
        Indexer[] arr = new Indexer[Definitions.NUM_THREADS];
        Thread[] threads = new Thread[Definitions.NUM_THREADS];
        /*
        TODO: IF YOU WILL USE THREADS THIS CONDITION MUST BE BEFORE FORKING THREADS , SO IMPORTANT !!!!!!!!!!!!!!
        * */
        if (collectionWord.countDocuments() == 0 & collectionLink.countDocuments() == 0)
            Definitions.CHOOSE_INDEX = true;
        else
            Definitions.CHOOSE_INDEX = false;
        /* ONLY FORKING NO CONDITION UNTIL NOW CAN HANDLE THE LOCK IF YOU FIND ONE , GO A HEAD*/
        for (int i = 0; i < Definitions.NUM_THREADS; i++) {
            arr[i] = new Indexer();
            threads[i] = new Thread(arr[i]);
            threads[i].setName("Thread" + Integer.toString(i));
            threads[i].start();
        }
    }

    public boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}