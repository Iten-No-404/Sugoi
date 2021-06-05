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

import org.jsoup.safety.Whitelist;


import java.util.Iterator;


import java.util.regex.Pattern;

import org.jsoup.select.Elements;

public class Indexer extends Thread {
    final static ConnectionString Connection = new ConnectionString("mongodb://127.0.0.1:27017");
    final static MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(Connection).retryWrites(true).build();
    final static MongoClient mongoClient = MongoClients.create(settings);
    // get database
    final static MongoDatabase database = mongoClient.getDatabase("URLs");
    // get collection for Words
    final static MongoCollection<org.bson.Document> cHTMLUnindexed = database.getCollection("HTML");
    final static MongoCollection<org.bson.Document> cHTMLIndexed = database.getCollection("IHTML");

    // get database
    final static MongoDatabase databaseindexer = mongoClient.getDatabase("Indexer");
    // get Collection for Links
    final static MongoCollection<Document> collectionWord = databaseindexer.getCollection("Words");
    // get Collection for Links
    final static MongoCollection<Document> collectionLink = databaseindexer.getCollection("Links");
    //

    Whitelist wl = new Whitelist();
    String[] whitelistedTags = {"div", "h1", "h2", "h3", "h4", "h5", "h6", "p", "a",
            "tt", "i", "b", "big", "small", "em", "strong", "dfn", "code", "samp", "kbd", "var", "cite",
            "abbr", "acronym", "sub", "sup", "span", "bdo", "address", "object", "pre", "q", "ins", "del",
            "dt", "dd", "li", "label", "option", "textarea", "fieldset", "legend", "button", "caption",
            "td", "th", "title", "style", "head", "footer"};


    public Indexer() {
        wl.addTags(whitelistedTags);
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

//        Iterator it = cHTMLIndexed.find(new Document()).iterator();
        int indexed = 0;
        while (cHTMLUnindexed.countDocuments() > 0 && indexed < 9) {
            // connect to link
            /*
             *  doc = undindexed.findoneanddelete()
             *  indexed.insert(doc);
             * */
            org.bson.Document doc = cHTMLUnindexed.findOneAndDelete(new Document());
            cHTMLIndexed.insertOne(doc);

            // get URL of page
            String URL = (String) doc.get("URL");
            String HTML = (String) doc.get("HTML");
            System.out.println(Thread.currentThread().getName() + ": \t I'm now stemming " + URL);

            // select all tags and words from HTML page
            org.jsoup.nodes.Document jsoupsecnod;
            String text = Jsoup.clean(HTML.toString(), wl);
            jsoupsecnod = Jsoup.parse(text);
            Elements elements = jsoupsecnod.select("*");


            // only to get length to use it in TF
            String AllWords[] = stemmer.Spliter(text);

            int j = 0;
            int ctr = 0;
            // get all elements
            for (Element e : elements) {
                ctr++;
                if (e.tagName().equals("footer"))
                    break;
                if (e.tagName().equals("header") || e.tagName().equals("footer") || e.tagName().equals("#root")
                        || e.tagName().equals("code") || e.tagName().equals("script") || e.tagName().equals("br") ||
                        e.childNodeSize() != 1)
                    continue;

                String[] words = stemmer.Spliter(e.text());
                for (int i = 0; i < words.length; i++) {
                    // If the this "word" is a number, skip it.
                    if (isNumeric(words[i])) continue;

                    // Find the position of the word with respect to the whole text
                    String eText = e.text();
                    if (words[i] != null) {
                        j = eText.indexOf(words[i], j);
                        if (j < 0)
                            continue;
                    } else continue;

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
                Links.Resetdrop(URL);
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
