import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.panforge.robotstxt.RobotsTxt;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.URL;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.concurrent.TimeUnit;
// TODO: Convert the spider to use MongoDB to load and dump lists instead of files.


/*
TODO: Take into account recrawling frequency.
I think it can be done by checking if the previous and current
versions change much, then if they do, bump the frequency score up or the time down.
Will require each visited URL to have a frequency field.
Can also include a "last visited" field for periodic recrawling.
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

public class Spider {

    private static final int MAX_PAGES = 150; /// < Max number of pages the crawler is allowed to visit.
    /// We don't want to insert the same URL twice into the visited list.
    private HashSet<String> visited = new HashSet<>();
    private HashSet<String> visitedHosts = new HashSet<>();
    /// Can also be an array, but a linked list is good if we want to expand so much
    /// That finding a large contiguous memory block will be hard.
    /// The speed loss of not using an array is not that big since the visiting of
    /// pages and downloading
    /// isn't really that fast anyway. We also don't access certain indecies.
    /// Instead, we go through the
    /// List element after element.
    private List<String> toVisit = new LinkedList<>();
    private HashSet<String> deniedVisit = new HashSet<>();

    // A place to store all the parsed robot.txt files that we'll be using
    Map<String, HashSet<String>> All_rules = new HashMap<>();

    private Integer currentPageVisitCount;
    private Boolean abort = false;

    // Should check to_visit.txt, if empty, should load the seed. Otherwise, should
    // load each line as a node.
    // Does the same with visited.txt
    public Spider() {
        currentPageVisitCount = 0;
    }

    private String NextURL(boolean DB, MongoClient client) {
        String next = null;
        if (!DB) {
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
        } else {
            var iter = client.getDatabase("URLs").getCollection("toVisit").find().iterator();
            if (iter.hasNext())
                next = (String) iter.next().get("URL");
        }

        return next;
    }

    // TODO: Work on the recrawl frequency
    // TODO: Work on the multi-threading

    public Boolean ShouldContinue() {
        return currentPageVisitCount < MAX_PAGES && !abort;
    }

    public boolean CheckAcessPermissions(String URL, StringBuilder uriHost) {
        boolean Access = true; // bool that checks whether we can visit this page or not

        try {
            URI uri = new URI(URL);
            uriHost.append(uri.getHost());
            String uriPath = uri.getPath(), uriProtocol = uri.getScheme();

            // Extract the top domain name from the url
            // TODO: deal cases where websites have % in their links
            // TODO: Maybe work more on robots.txt
            String reg = "^[^.]*\\.(?=\\w+\\.\\w+$)";

            String TopDomainName = uriHost.toString();
            TopDomainName = TopDomainName.replaceAll(reg, "");

            // TODO: Look into other robot.txt parsers
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
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Access Robot.txt and parse it to chek which pages we can visit later.
            HashSet<String> rules = new HashSet<>();
            HashSet<String> allowed = new HashSet<>();
            String[] temp;
            // Check if this robot.txt file was already parsed before
            if (All_rules.get(TopDomainName) == null) {
                InputStream robotsTxtStream = new URL(uriProtocol, uriHost.toString(), "/robots.txt").openStream();
                RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);

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
                            String decoded_path = java.net.URLDecoder.decode(temp[1], StandardCharsets.UTF_8);
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
                All_rules.put(TopDomainName, rules);
            } else {
                rules = All_rules.get(TopDomainName);
                System.out.println("Using the " + TopDomainName + " robots.txt");
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
                    Access = false;
                    break;
                }
            }


            // Last check
            path.append("/");
            if (rules.contains(path.toString()))
                Access = false;

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        return Access;
    }

    private List<String> ExtractLinks(String URL) {
        // This part handles extracting the links from the downloaded page
        // We first open the downloaded document
        // Then we parse it for all the links and references in it
        File doc = new File(Definitions.HTML_DLD_PATH + URL.hashCode() + ".HTML");
        Document currentDoc = null;
        try {
            // TODO: Check if the link being for mobile matters (same with navigation stuff)
            // Parse function parses the current document for all the elements (links, images, etc..)
            // The first two arguments are simple
            // the third argument is used in case of linking to another part of the website
            // for example: the document is of Wikipedia.com/wiki/Main_Page
            // wikipedia.com/wiki/<anything here>
            // would appear as /wiki/<anything here>
            // The third argument takes the main part of the URL "wikipedia.com"
            // and in the case of /wiki/<anything here> we the third argument is put before it
            // so it becomes wikipedia.com/wiki/ <anything here>
            currentDoc = Jsoup.parse(doc, "UTF-8", URL);
        } catch (IOException e) {
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
                pageLinks.add(link.attr("abs:href"));
            }
        return pageLinks;
    }

    /// Dumps the lists and sets we use in runtime.
    public void Finalize() {
//        DumpLists();
    }

    // TODO: move MongoDB functionality into functions to avoid duplicate code.
    public static void main(String[] args) {
        // Testing if the pages downloads correctly
        Spider spooder = new Spider();

        // MongoDB connection init
        MongoClient client = MongoClients.create("mongodb://127.0.0.1:27017");

        // If no URLs in URLs.toVisit , insert the seed.
        // Should create the DB and collection if they don't exist.
        var toVisitHasElements = client.getDatabase("URLs").getCollection("toVisit").find().iterator().hasNext();
        if (!toVisitHasElements) {
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
                arr.add(new org.bson.Document("URL", url));
            }
            client.getDatabase("URLs").getCollection("toVisit").insertMany(arr);
        }

        //int downloadedPage = 0;
        while (spooder.ShouldContinue() ) {
            String URL = spooder.NextURL(Definitions.USE_MONGO, client);

            // If for some reason the URL is null, skip this iteration
            // Can't say we visited this website...
            if (URL != null && !URL.equals("")) { // TODO: Look more into URIs
                StringBuilder uriHost = new StringBuilder();
                // Will probably be useful to
                // For addresses like https://stackoverflow.com/questions/21060992/how-does-java-resolve-a-relative-path-in-new-file
                // This should return something like "stackoverflow.com"

                boolean Access = spooder.CheckAcessPermissions(URL, uriHost);

                /* TODO: Maybe move from multiple bools into a satus variable?
                    This would allow us to use a switch case instead of nested ifs.
                */
                if (Access) {
                    boolean downloadSuccessful = HTMLDownloader.DownloadPage(URL);

                    // Add the URL to the visited list if the page is downloaded
                    // or re-insert it into the toVisit list if not.
                    if (downloadSuccessful) {
                        // Remove the current URL from toVisit
                        client.getDatabase("URLs").getCollection("toVisit").deleteOne(Filters.eq("URL", URL));
                        List<String> extractedLinks = spooder.ExtractLinks(URL);
                        if (!Definitions.USE_MONGO) {
                            spooder.visitedHosts.add(uriHost.toString());
                            spooder.toVisit.addAll(extractedLinks);
                            spooder.visited.add(URL);
                        } else {
                            // Upsert the current URL into visitedHosts
                            client.getDatabase("URLs").getCollection("visitedHosts").updateOne(Filters.eq("URL", uriHost.toString()),
                                    new org.bson.Document("$set", new org.bson.Document("URL", uriHost.toString())), new UpdateOptions().upsert(true));
                            // Upsert extracted links
                            for (String extractedLink : extractedLinks) {
                                client.getDatabase("URLs").getCollection("toVisit").updateOne(Filters.eq("URL", extractedLink),
                                        new org.bson.Document("$set", new org.bson.Document("URL", extractedLink)), new UpdateOptions().upsert(true));
                            }
                            // Upsert this just visited URL
                            client.getDatabase("URLs").getCollection("visited").updateOne(Filters.eq("URL", URL),
                                    new org.bson.Document("$set", new org.bson.Document("URL", URL)), new UpdateOptions().upsert(true));
                        }
                        spooder.currentPageVisitCount++;
                    } else {
                        if (!Definitions.USE_MONGO)
                            spooder.toVisit.add(URL);
                        else
                            client.getDatabase("URLs").getCollection("toVisit").updateOne(Filters.eq("URL", URL),
                                    new org.bson.Document("$set", new org.bson.Document("URL", URL)), new UpdateOptions().upsert(true));
                    }
                } else {
                    if (!Definitions.USE_MONGO)
                        spooder.deniedVisit.add(URL);
                    else {
                        client.getDatabase("URLs").getCollection("deniedVisit").updateOne(Filters.eq("URL", URL),
                                new org.bson.Document("$set", new org.bson.Document("URL", URL)), new UpdateOptions().upsert(true));

                        // Remove the current URL from toVisit
                        client.getDatabase("URLs").getCollection("toVisit").deleteOne(Filters.eq("URL", URL));
                        List<String> extractedLinks = spooder.ExtractLinks(URL);
                    }
                }

            }
        }
        spooder.Finalize();
    }
}
