import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.panforge.robotstxt.RobotsTxt;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URL;
import java.util.List;
import java.text.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

// DONE Share robots.txt data between threads
/* DONE Maybe create a wrapper to println so we can enable/disable printing with one bool
    Might help since printing does affect performance, especially with how often it's done.
 */



/* DONE Look into using an iterator/cursor instead of using db.coll.finOneAndDelete over and over
    Might be faster due to fewer connections with the DB.

    Seems like using coll.find().iterator() (which returns a cursor, funnily enough) gives you the
    results of the DB query at that time (reasonable). It's also passed by reference. So when a thread
    modifies it, the remaining threads will not see the modification
 */

/* DONE Check for valid HTML
    Links like: https://upload.wikimedia.org/wikipedia/commons/a/af/Crazy_4K_drone_video_of_Hong_Kong%2C_China.webm
    Might cause issues with parsing
*/

// Known issue: sometimes at the start, you can see that one thread downloads the same URL twice, no idea why..
// Known issue: the crawler does not distinguish website.com and website.com/ It counts them as 2 different URLS

// TODO: solve the bug where a link gets visited twice at the start

/* TODO Clean up the thread prints. Can be done through breaking each stage into its own string
    then arranging them right before printing.
    urlString, accessString, errorString
    Maybe implement a logger....
 */
/* Not needed anymore?
TODO: Take into account re-crawling frequency.
I think it can be done by checking if the previous and current
versions change much, then if they do, bump the frequency score up or the time down.
Will require each visited URL to have a frequency field.
Can also include a "last visited" field for periodic re-crawling.
This will also require that we loop over already visited URLs and then add URLs that
change frequently enough or are old enough to the toVisit list.
*/

/*
visitedURL=
{
"URL" : wikipedia.org
"last_visited" : some date? can store the last visit date in days if that's easier
"freq_score" : 1337
}
 */

public class Spider extends Thread {

    final MongoClient mClient;
    // A place to store all the parsed robot.txt files that we'll be using
    Map<String, HashSet<String>> All_rules;
    String iterStart;
    StringBuilder currentIterMessage = new StringBuilder();
    /// We don't want to insert the same URL twice into the visited list.
    MongoDatabase mongoDB;
    MongoCollection<org.bson.Document> cToVisit, cVisited, cDeniedVisit, cVisitedHosts, cHTML;
    private Integer numActiveCrawlers;

    // TODO Remove these once we're fully using MongoDB
    private HashSet<String> visited = new HashSet<>();
    private HashSet<String> visitedHosts = new HashSet<>();
    private List<String> toVisit = new LinkedList<>();
    private HashSet<String> deniedVisit = new HashSet<>();

    private AtomicInteger totalDownloaded;
    private Boolean abort = false;

    // Gets a reference to the MongoDB connection and a reference to its hashmap
    public Spider(MongoClient client, Integer activeThreads, AtomicInteger totPages, Map <String, HashSet<String>> rules) {
        mClient = client;
        numActiveCrawlers = activeThreads;
        All_rules = rules;
        // Caching
        mongoDB = mClient.getDatabase(Definitions.dbURL);
        cVisited = mongoDB.getCollection(Definitions.cVisited);
        cDeniedVisit = mongoDB.getCollection(Definitions.cDeniedVisit);
        cVisitedHosts = mongoDB.getCollection(Definitions.cVisitedHosts);
        cToVisit = mongoDB.getCollection(Definitions.cToVisit);
        cHTML = mongoDB.getCollection(Definitions.cHTML);

        totalDownloaded = totPages;
    }

    // Finds and deletes a URL in one step
    // If we can't visit this URL for any reason, it will be re-inserted into the DB later
    // Returns a URL or null if no URLs can be found
    private String NextURL() {
        synchronized (mClient) {
            {
                if (!Definitions.USE_MONGO) {
                    String next;
                    if (toVisit.isEmpty()) {
                        abort = true;
                        return null;
                    }
                    // Loop through the list until we find a URL we haven't visited.
                    do {
                        next = toVisit.remove(0);
                    }
                    while (visited.contains(next) && !toVisit.isEmpty());

                    // In case the last extracted item was already visited
                    if (toVisit.isEmpty() && visited.contains(next)) return null;
                    return next;
                } else {
                    String URL = "";
                    boolean Unwanted_extension;
                    int toVisitCount = (int) cToVisit.countDocuments();
                    if (toVisitCount > 0) {
                        do {

                            Unwanted_extension = false;
                            URL = (String) cToVisit.findOneAndDelete(new org.bson.Document()).get(Definitions.kURL);
                            // Some extensions that would indicate that this link points to something that's not an HTML page (video, image, etc..)
                            if (URL.contains(".webm") || URL.contains(".png") || URL.contains(".jpg") || URL.contains(".svg") || URL.contains(".jpeg") ||
                                    URL.contains(".mp4") || URL.contains(".mp3") || URL.contains(".mov") || URL.contains(".wav") || URL.contains(".wmv") ||
                                    URL.contains(".mkv") || URL.contains(".flv") || URL.contains(".avi") || URL.contains(".api") || URL.contains(".js") ||
                                    URL.contains(".script") || URL.contains(" ") || URL.contains(".pdf"))
                                Unwanted_extension = true;
                            if (Unwanted_extension && Definitions.PRIMARY_CRAWLER_PRINT)
                                System.out.println(URL + " does not point to an html page");
                        }
                        // Continue getting and removing a URL if it's already visited
                        while (cVisited.countDocuments(new org.bson.Document(Definitions.kURL, URL)) > 0 || Unwanted_extension);
                        //cVisited.countDocuments(new org.bson.Document(Definitions.kURL, URL + '/')) > 0 ||
                        // cVisited.countDocuments(new org.bson.Document(Definitions.kURL, URL.substring(0,URL.length() - 2))) > 0 ||

                        // If there were 2 docs before pulling one, there should be one more.
                        if (toVisitCount > 2) mClient.notifyAll();
                    }
                    // No more links, it's either that our previous batch ran out or there really are no more
                    // links to visit in the DB
                    else {
                        // If no more links to visit in the DB
                        // If there are other active crawlers that might add to these links, wait
                        // Otherwise, terminate.
                        if (numActiveCrawlers > 1) {
                            try {
                                numActiveCrawlers--;
                                if (Definitions.SECONDARY_CRAWLER_PRINT)
                                    System.out.println(Thread.currentThread().getName() + ": No URLs available but at least " +
                                            "one active crawler, waiting for its links...");
                                mClient.wait();
                            } catch (InterruptedException e) {
                                if (Definitions.SECONDARY_CRAWLER_PRINT)
                                    System.out.println(Thread.currentThread().getName() + " new URLs added, waking up...");
                                e.printStackTrace();
                            }
                        } else {
                            numActiveCrawlers--;
                            if (Definitions.SECONDARY_CRAWLER_PRINT)
                                System.out.println(Thread.currentThread().getName() + ": No URLs available and no other " +
                                        "active crawlers, terminating...");
                        }
                    }
                    return URL;
                }
            }
        }
    }


    public Boolean ShouldContinue() {
        return totalDownloaded.get() < Definitions.MAX_PAGES && !abort;
    }

    public Definitions.RobotsAuth CheckAccessPermissions(String URL, StringBuilder uriHost) {
        Definitions.RobotsAuth status = Definitions.RobotsAuth.Granted; // bool that checks whether we can visit this page or not
        try {
            URI uri = new URI(URL);
            uriHost.append(uri.getHost());
            String uriPath = uri.getPath(), uriProtocol = uri.getScheme();
            // Extract the top domain name from the url
            String reg = "^[^.]*\\.(?=\\w+\\.\\w+$)";

            String TopDomainName = uriHost.toString();
            TopDomainName = TopDomainName.replaceAll(reg, "");

            /*
             * Since the library seems to be off? (test with facebook and youtube's home pages,
             * fb should block while yt should let the crawler through)
             * The manual approach is to read through the file line by line, checking each user
             * agent for the crawler's name, or checking user-agent *, then somehow checking
             * if our current link follows from a blocked one.
             * Ex:
             * User-agent: *
             * Disallow: /forum/
             *
             * Let's say were at /forum/post1337, we maybe we need to split at "/" ?
             * Hmmmmmmmm
             * */
            // Delay so as to not get yeeted
            // idk if one second is enough


            // Access Robot.txt and parse it to chek which pages we can visit later.
            HashSet<String> rules = new HashSet<>();
            HashSet<String> allowed = new HashSet<>();
            String[] temp;
            // Check if this robot.txt file was already parsed before
            if (All_rules.get(TopDomainName) == null) {
                RobotsTxt robotsTxt;
                try (InputStream robotsTxtStream = new URL(uriProtocol, uriHost.toString(), "/robots.txt").openStream()) {
                    robotsTxt = RobotsTxt.read(robotsTxtStream);
                } catch (IOException e) {
//                    System.out.format("Thread " + Thread.currentThread().getId() + ", URL: %s does not have a robots.txt file\n", uriHost);
                    if (Definitions.PRIMARY_CRAWLER_PRINT)
                        currentIterMessage.append(TopDomainName).append(" does not have a robots.txt file. ");
                    return Definitions.RobotsAuth.Granted;
                }
                String[] splits = robotsTxt.toString().split("\\R");

                boolean inside_general = false, inside_Sugoi = false;
                for (String s : splits) {
                    //System.out.println(s);
                    // Check If the line I'm on is the start of the general rules
                    if (s.equals("User-agent: *") && !inside_general) {
                        inside_general = true;
                        continue;
                    }

                    // If I'm working on the general or specific rules and find another User-agent, I'm done with this.
                    if (s.split(":")[0].equals("User-agent")) {
                        inside_general = false;
                    }
                    if (inside_general) {
                        temp = s.split(": ");
                        // This is a disallow statement, store it in the hashset.
                        if (temp[0].equals("Disallow")) {
                            String decoded_path;
                            try {
                                decoded_path = java.net.URLDecoder.decode(temp[1], StandardCharsets.UTF_8);
                            } catch (Exception e) {
                                if (Definitions.PRIMARY_CRAWLER_PRINT)
                                    currentIterMessage.append("ERROR TRYING TO DECODE URL ").append(URL).append(" RETURNING ACCESS DENIED. ");
                                return Definitions.RobotsAuth.Denied;
                            }
                            rules.add(decoded_path);
                        } else if (temp[0].equals("Allow")) {
                            allowed.add(temp[1]);
                        } else
                            continue;
                    }
                    // Here are the sites not specifically allowed to us
                    if (s.equals("User-agent: Sugoi")) {
                        inside_Sugoi = true;
                    }
                    if (inside_Sugoi) {
                        temp = s.split(": ");
                        // This is a disallow statement, store it in the hashset.
                        if (temp[0].equals("Disallow")) {
                            String decoded_path = java.net.URLDecoder.decode(temp[1], StandardCharsets.UTF_8);
                            rules.add(decoded_path);
                        } else if (temp[0].equals("Allow")) {
                            allowed.add(temp[1]);
                        }
                    }
                }
                synchronized (All_rules) {
                    All_rules.put(TopDomainName, rules);
                }
            } else {
                rules = All_rules.get(TopDomainName);
                if (Definitions.PRIMARY_CRAWLER_PRINT)
                    currentIterMessage.append("using ").append(TopDomainName).append("'s robots.txt. ");
//                System.out.println("Using the " + TopDomainName + " robots.txt");
            }

            temp = uriPath.split("/");

            StringBuilder path = new StringBuilder();

            int n = temp.length;
            int i = 1;
            // a bool to check whether I'm adding a "/" or a part of the urlPath
            boolean Slash = true;

            while (i < n) {
                if (Slash) {
                    path.append("/");
                    Slash = false;
                } else {
                    path.append(temp[i]);
                    Slash = true;
                    i++;
                }
                if (rules.contains(path.toString())) {
                    status = Definitions.RobotsAuth.Denied;
                    break;
                }
            }


            // Last check
            path.append("/");
            if (rules.contains(path.toString()))
                status = Definitions.RobotsAuth.Denied;

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return status;
    }

    private List<String> ExtractLinks(String HTML) {
        // This part handles extracting the links from the downloaded page
        // We first open the downloaded document
        // Then we parse it for all the links and references in it
        Document currentDoc = null;
        try {
            // TODO: Check if the link being for mobile matters -- Can't do it for mobile view links as they can be very different
            // Parse function parses the current document for all the elements (links, images, etc..)
            // The first two arguments are simple
            // the third argument is used in case of linking to another part of the website
            // for example: the document is of Wikipedia.com/wiki/Main_Page
            // wikipedia.com/wiki/<anything here>
            // would appear as /wiki/<anything here>
            // The third argument takes the main part of the URL "wikipedia.com"
            // and in the case of /wiki/<anything here> we the third argument is put before it
            // so it becomes wikipedia.com/wiki/ <anything here>

            currentDoc = Jsoup.parse(HTML/*, "UTF-8"*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // here we extract the links from the parsed document
        Elements links = null;
        if (currentDoc != null)
            links = currentDoc.select("a[href]");
        // We insert them in a hashset because sometimes, we can extract the same link multiple times
        List<String> pageLinks = new ArrayList<>();
        if (links != null)
            for (Element link : links) {
                String attr = link.attr("abs:href");
                // For some reason, the attr function returned empty strings
                if (!attr.equals("")) {
                    // Before adding the link, remove all navigation hashes (#) from it
                    int con = attr.indexOf("#");
                    if (con != -1)
                        attr = attr.split("#")[0]; // select the first part before the #
                    pageLinks.add(attr);
                }
            }
        return pageLinks;
    }

    // TODO: move MongoDB functionality into functions to avoid duplicate code.
    public void run() {
        if (Definitions.SECONDARY_CRAWLER_PRINT)
            System.out.println("Crawler " + getName().toLowerCase(Locale.ROOT) + " Started");

        while (this.ShouldContinue()) {
            String URL;
            URL = NextURL();

            // If for some reason the URL is null, skip this iteration
            // Can't say we visited this website...
            if (URL != null && !URL.equals("")) {
                currentIterMessage.delete(0, currentIterMessage.length()); // Clear message
                iterStart = getName() + "\t [";
                currentIterMessage.append("\t]: \t");

                StringBuilder uriHost = new StringBuilder();
                Definitions.RobotsAuth status = CheckAccessPermissions(URL, uriHost);

                switch (status) {
                    case Granted: {
                        if (Definitions.PRIMARY_CRAWLER_PRINT)
                            currentIterMessage.append("ACCESS GRANTED, PROCEEDING TO ").append(URL);

                        StringBuilder downloadedHTML = new StringBuilder();
                        boolean downloadSuccessful = HTMLDownloader.DownloadPage(URL, downloadedHTML);

                        // Add the URL to the visited list if the page is downloaded
                        if (downloadSuccessful) {
                            if (Definitions.PRIMARY_CRAWLER_PRINT)
                                currentIterMessage.append(", download successful ");

                            // Insert the downloaded HTML into the URL-HTML hashMap. Then Extract links
                            org.bson.Document urlPage = new org.bson.Document(Definitions.kURL, URL).append("HTML", downloadedHTML.toString());
                            cHTML.insertOne(urlPage);

                            List<String> extractedLinks = ExtractLinks(downloadedHTML.toString());

                            if (!Definitions.USE_MONGO) {
                                visitedHosts.add(uriHost.toString());
                                toVisit.addAll(extractedLinks);
                                visited.add(URL);
                            }
                            // Update visitedHosts, toVisit, and visited
                            else {
                                // Upsert the base URL into visitedHosts
                                cVisitedHosts.updateOne(Filters.eq(Definitions.kURL, uriHost.toString()),
                                        new org.bson.Document("$set", new org.bson.Document("URL", uriHost.toString())), new UpdateOptions().upsert(true));

                                // Upsert extracted links
                                for (String extractedLink : extractedLinks) {
                                    cToVisit.updateOne(Filters.eq(Definitions.kURL, extractedLink),
                                            new org.bson.Document("$set", new org.bson.Document("URL", extractedLink)), new UpdateOptions().upsert(true));
                                }

                                // Upsert this just visited URL
                                cVisited.updateOne(Filters.eq(Definitions.kURL, URL),
                                        new org.bson.Document("$set", new org.bson.Document("URL", URL)), new UpdateOptions().upsert(true));
                            }

                            totalDownloaded.incrementAndGet();
                        }
                        // If download unsuccessful, reinsert/upsert the URL back into toVisit
                        else {
                            if (Definitions.PRIMARY_CRAWLER_PRINT)
                                currentIterMessage.append(", download unsuccessful ");
                            if (!Definitions.USE_MONGO)
                                toVisit.add(URL);
                            else
                                cToVisit.updateOne(Filters.eq(Definitions.kURL, URL),
                                        new org.bson.Document("$set", new org.bson.Document(Definitions.kURL, URL)), new UpdateOptions().upsert(true));
                        }
                        break;
                    }
                    // If access denied, add it to deniedVisit
                    case Denied: {
                        if (Definitions.PRIMARY_CRAWLER_PRINT)
                            currentIterMessage.append("ACCESS DENIED to URL ").append(URL);
                        if (!Definitions.USE_MONGO)
                            deniedVisit.add(URL);
                        else {
                            cDeniedVisit.insertOne(new org.bson.Document(Definitions.kURL, URL));
                        }
                        break;
                    }
                }
            }
            if (currentIterMessage.length() > 0 && Definitions.PRIMARY_CRAWLER_PRINT)
            {System.out.println(iterStart+totalDownloaded.getAcquire() + currentIterMessage);}
        }
        if (Definitions.SECONDARY_CRAWLER_PRINT)
            System.out.println("Crawler " + getName() + " terminating. Crawled over " + totalDownloaded + " pages");
    }

}
