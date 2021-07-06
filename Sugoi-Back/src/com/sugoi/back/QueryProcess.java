package com.sugoi.back;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.ArrayList;

import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

public class QueryProcess {
    final static ConnectionString Connection = new ConnectionString("mongodb://127.0.0.1:27017");
    final static MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(Connection).retryWrites(true).build();
    final static MongoClient mongoClient = MongoClients.create(settings);
    // get database
    final static MongoDatabase database = mongoClient.getDatabase("URLs");
    // get collection for Words
    final static MongoCollection<Document> cHTMLIndexed = database.getCollection("IHTML");
    // get database
    final static MongoDatabase databaseindexer = mongoClient.getDatabase("Indexer");
    // get Collection for Links
    final static MongoCollection<Document> collectionWord = databaseindexer.getCollection("Words");
    // get Collection for Links
    final static MongoCollection<Document> collectionLink = databaseindexer.getCollection("Links");
    // urls of word
    public ArrayList<String> urls;
    // titles of urls
    public ArrayList<String> titles;
    // paragraphs of word
    public ArrayList<String> paragraphs;
    FindIterable<Document> ourlink;

    int RESULTS_PER_PAGE = 10;

    public QueryProcess() {

    }

    public static void main(String args[]) {
        QueryProcess q = new QueryProcess();
        q.Query("scienc");
    }

    public void Query(String word) {
        Stemmer stemmer = new Stemmer();
        // stemming the word
        word = stemmer.PorterStemming(word);
        int count = 0;
        urls = new ArrayList<String>();
        titles = new ArrayList<String>();
        paragraphs = new ArrayList<String>();
        if (word != null) {
            // If the word doesn't exist in the DB, do nothing and return.
            if (!(collectionWord.countDocuments(new Document("id", word)) > 0)) return;

            // find word in indexer db
            FindIterable<Document> iterable = collectionWord.find(new Document("id", word));
            Document doc = iterable.iterator().next();
            // get URLS of words
            ArrayList<Document> links = (ArrayList<Document>) doc.get("docs");

            for (int i = 0; i < RESULTS_PER_PAGE && i < links.size(); i++) {
                // loops on every ulr
                String URL = (String) links.get(i).get("doc");
                // get positions to get tags
                ArrayList<Document> positions = (ArrayList<Document>) links.get(i).get("positions");
                if (positions.size() > 1) {

                    // check if the word is found in tags but not in #root " not good tag"
                    Document index = positions.get(0);
                    String tag = (String) index.get("type");

                    // try to get snippets;
                    // add ulrs
                    urls.add(URL);
                    ourlink = cHTMLIndexed.find(new Document("URL", URL));

                    if (!ourlink.iterator().hasNext()) break;

                    Document thelink = ourlink.iterator().next();
                    // get html form db which id downloaded by craweler
                    String html = (String) thelink.get("HTML");
                    // get the text from html
                    org.jsoup.nodes.Document jsoupsecnod;
                    jsoupsecnod = Jsoup.parse(html);
                    // get the title of url
                    Elements element = jsoupsecnod.select("title");
                    titles.add(element.text());
                    // get the tags which word is found
                    Elements div = jsoupsecnod.select(tag);

                    for (Element e : div) {
                        // try to get snippets
                        String text = e.text();
                        Stemmer stemmer1 = new Stemmer();
                        // stemming text
                        String words[] = stemmer1.Spliter(text);
                        Boolean found = false;

                        for (int k = 0; k < words.length; k++) {
                            if(words[k] != null && !e.text().contains(words[k])) continue;

                            if (words[k] != null)
                                words[k] = stemmer1.PorterStemming(words[k]);

                            if (words[k] != null)
                                if (words[k].equals(word)) {
                                    // if you found snipp  add it to paragraph
                                    // one paragraph is enough

                                    // Check for first occurrence
                                    int firstOccurence = (int)positions.get(0).get("index");

                                    // Get the paragraph from the first occurrence to 800 chars or the length of the paragraph
                                    int pLength = e.text().length();
                                    int minEnd = Math.min(pLength, firstOccurence+800);
                                    String paragraph = e.text().substring(firstOccurence,minEnd );

                                    paragraphs.add(paragraph);

                                    System.out.println(paragraph);
                                    found = true;
                                    break;
                                }
                        }
                        if (found)
                            break;
                    }
                    count++;
                }
            }

        }
    }
}
