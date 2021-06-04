import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import org.bson.BsonMaximumSizeExceededException;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

/* TODO Create a URL queue to streamline and speedup crawler URL acquisition.
    It will work like this:
    The queue is 2*NUM_THREADS in size. Before the threads start, we query the DB for that number of URLS
    Then when we're low on URLs and while the threads are working, we can ask for more.
    Might be faster...
 */

// A class to manage crawlers
public class SpiderHive {
    Spider[] crawlers;
    MongoClient mClient;

    SpiderHive(int numThreads) {

        // MongoDB connection init
        mClient = MongoClients.create("mongodb://127.0.0.1:27017");
        Integer activeThreads = numThreads;

        crawlers = new Spider[numThreads];

        for (int i = 0; i < numThreads; i++) {
            crawlers[i] = new Spider(mClient, activeThreads);
        }

    }

    public static void main(String[] args) {
        SpiderHive hive = new SpiderHive(Definitions.NUM_THREADS);
        hive.BeginCrawl();
    }

    // Writes to MongoDB
    public void BeginCrawl() {
        // If no URLs in URLs.toVisit , insert the seed.
        // Should create the DB and collection if they don't exist.
        long toVisitElements = mClient.getDatabase("URLs").getCollection("toVisit").countDocuments();
        if (toVisitElements == 0) {
            System.out.println("The toVisit collection has no elements, loading seed...");
            ArrayList<org.bson.Document> arr = new ArrayList<>();
            ArrayList<String> seeds = new ArrayList<>();

            // Load seed data
            try (FileInputStream seed = new FileInputStream(Definitions.seedFN)) {
                Scanner sc = new Scanner(seed);
                while (sc.hasNextLine()) {
                    seeds.add(sc.nextLine());
                }
                sc.close();
            } catch (Exception e) {
                System.out.println("ERROR WHILE SEEDING DATABASE");
                e.printStackTrace();
            }

            for (String url : seeds) {
                System.out.println("Seed URL Loaded: " + url);
                arr.add(new org.bson.Document("URL", url));
            }
            mClient.getDatabase("URLs").getCollection("toVisit").insertMany(arr);

            System.out.println("Beginning crawl...");
        }


        Long startTime = System.currentTimeMillis();
        // Start each crawler thread
        for (Spider spooder : crawlers) {
            spooder.start();
        }
        // Wait for each crawler thread to return
        for (Spider spooder : crawlers) {
            try {
                spooder.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Long endTime = System.currentTimeMillis();

        // We should have numThreads hashmaps with the websites each crawler has visited
        // }
        System.out.println("Crawl finished. Approximately " + Definitions.NUM_THREADS*Definitions.MAX_PAGES + " links crawled in " + (endTime - startTime) / 1000 + " seconds with " + Definitions.NUM_THREADS
                + " threads");

    }
}
