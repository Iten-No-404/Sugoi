package com.sugoi.back;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.Authenticator;
import org.bson.Document;

// Please put application constants or config variables here.
public class Definitions {
    public static final int NUM_THREADS =12;
    public static final String seedFN = "./txt/seed.txt";
    public static final String visitedFN = "./txt/visited.txt";
    public static final String toVisitFN = "./txt/to_visit.txt";
    public static final String visitedHostsFN = "./txt/visitedHosts.txt";
    public static final String HTML_DLD_PATH = "./download/";
    public static final String DENIED_SITES = "./txt/denied.txt";
    public static final boolean USE_MONGO = true;
    public static  Boolean CHOOSE_INDEX = true;
    public static final int MAX_PAGES = 1200; /// < Max number of pages the crawler is allowed to visit.
    public static final boolean PRIMARY_CRAWLER_PRINT = true;
    public static final boolean SECONDARY_CRAWLER_PRINT = true;

    // Some DB definitions in case we want to change the collection names
    // Faster than find + replace
    // db at the start indicates that this is a database. c indicates it's a collection. k indicates it's a key.
    public static final String dbURL = "URLs";
    public static final String cToVisit = "toVisit";
    public static final String cVisitedHosts = "visitedHosts";
    public static final String cVisited = "visited";
    public static final String cDeniedVisit = "deniedVisit";
    public static final String cHTML = "HTML";

    public static final String kURL = "URL";

    // Download statuses
    public enum RobotsAuth{ Granted, Denied };

    public static final Long BSON_MAX_SIZE = 16777216L;
}
