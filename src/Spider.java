import com.panforge.robotstxt.Grant;
import com.panforge.robotstxt.RobotsTxt;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.net.URL;

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

    private static final int MAX_PAGES = 10; /// < Max number of pages the crawler is allowed to visit.
    /// We don't want to insert the same URL twice into the visited list.
    private Set<String> visited = new HashSet<>();
    private Set<String> visitedHosts = new HashSet<>();
    /// Can also be an array, but a linked list is good if we want to expand so much
    /// That finding a large contiguous memory block will be hard.
    /// The speed loss of not using an array is not that big since the visiting of
    /// pages and downloading
    /// isn't really that fast anyway. We also don't access certain indecies.
    /// Instead, we go through the
    /// List element after element.
    private List<String> toVisit = new LinkedList<>();
    private Integer currentPageVisitCount;
    private Boolean abort = false;

    // Should check to_visit.txt, if empty, should load the seed. Otherwise, should
    // load each line as a node.
    // Does the same with visited.txt
    public Spider() {
        File tV = new File(Definitions.toVisitFN);
        if (tV.length() == 0) {
            // Should load the seed into toVisit
            LoadList(toVisit, Definitions.seedFN);
        } else // Otherwise, load to_visit.
            LoadList(toVisit, Definitions.toVisitFN);

        // Load visited.txt
        LoadList(visited, Definitions.visitedFN);

        // Load visitedHosts.txt
        LoadList(visitedHosts, Definitions.visitedHostsFN);

        currentPageVisitCount = 0;
    }

    private String NextURL() {
        if (toVisit.isEmpty()) {
            abort = true;
            return null;
        }

        String next;
        // Loop through the list until we find a URL we haven't visited.
        do {
            next = toVisit.remove(0);
        }
        while (visited.contains(next) && !toVisit.isEmpty());

        // In case the last extracted item was already visited
        if (toVisit.isEmpty() && visited.contains(next)) return null;

        return next;
    }

    // POLYMORPHISM FTW!!
    public void LoadList(Collection<String> list, String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Scanner sc = new Scanner(fis);
            while (sc.hasNextLine()) {
                list.add(sc.nextLine());
            }
            sc.close();
        } catch (IOException ie) {
            System.out.println("ERROR WHILE INITIALIZING SPIDER: IO EXCEPTION");
            ie.printStackTrace();
        } catch (Exception e) {
            System.out.println("ERROR WHILE INITIALIZING SPIDER: EXCEPTION");
            e.printStackTrace();
        }
    }

    public Boolean ShouldContinue() {
        return currentPageVisitCount < MAX_PAGES && toVisit.size() > 0 && !abort;
    }

    public void DumpLists() {
        // The second parameter specifies whether we want to append to the file (true)
        // or overwrite (false)
        try {
            // Dump toVisit into to_visit.txt
            FileWriter tvfw = new FileWriter(Definitions.toVisitFN, false);
            if (toVisit.size() == 0) {
                tvfw.write("");
            } else {
                while (toVisit.size() > 0) {
                    String URL = toVisit.remove(0);
                    tvfw.write(URL + "\n");
                }
            }
            tvfw.close();


            // TODO: Maybe make a function to dump hashsets into a file since both hashsets use about the same code?
            // Dump visited into visited.txt
            if (visited.size() > 0) {
                FileWriter vfw = new FileWriter(Definitions.visitedFN, false);
                // Note that the HashSet doesn't order objects FIFO, the order is random-ish?
                Iterator<String> iter = visited.iterator();
                do {
                    String URL = iter.next();
                    vfw.write(URL + "\n");
                } while (iter.hasNext());
                vfw.close();
            }

            // Dump visitedHosts into visitedHosts.txt
            if (visitedHosts.size() > 0) {
                FileWriter vfw = new FileWriter(Definitions.visitedHostsFN, false);
                // Note that the HashSet doesn't order objects FIFO, the order is random-ish?
                Iterator<String> iter = visitedHosts.iterator();
                do {
                    String URL = iter.next();
                    vfw.write(URL + "\n");
                } while (iter.hasNext());
                vfw.close();
            }
        } catch (Exception e) {
            System.out.println("ERROR WHILE DUMPING LISTS");
        }
    }

    /// Writes the content of toVisit into to_visit.txt
    /// and the same with visited and visited.txt
    public void Finalize() {
        DumpLists();
    }

    public static void main(String[] args) {
        // Testing if the pages downloads correctly
        Spider spooder = new Spider();

        while (spooder.ShouldContinue()) {
            String URL = spooder.NextURL();

            // If for some reason the URL is null, skip this iteration
            // Can't say we visited this website...
            if (URL != null) { // TODO: Look more into URIs


                // Will probably be useful to
                // For addresses like https://stackoverflow.com/questions/21060992/how-does-java-resolve-a-relative-path-in-new-file
                // This should return something like "stackoverflow.com"
                try {
                    URI uri = new URI(URL);
                    String uriHost = uri.getHost(), uriPath = uri.getPath(), uriProtocol = uri.getScheme();

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


                    // region not really sure about this part
                    InputStream robotsTxtStream = new URL(uriProtocol, uriHost, uriPath).openStream();
                    RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);

                    boolean hasAccess = robotsTxt.query("Sugoi", uriPath);
                    if (hasAccess) {
                        Grant grant = robotsTxt.ask("Sugoi", uriPath);

                        if (grant == null || grant.hasAccess()) {
                            // do something, immediate access?
                            System.out.println("Grant is null or grant has access");
                        }
                        if (grant != null && grant.getCrawlDelay() != null) {
                            // wait till next time
                            // We have access, but should wait until we can recrawl again.
                            System.out.println("Grant is not null and there's a recrawl delay");

                        }
                    }
                    // endregion
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }

                boolean downloadSuccessful = HTMLDownloader.DownloadPage(URL);

                // Add the URL to the visited list if the page is downloaded
                // or re-insert it into the toVisit list if not.
                if (downloadSuccessful) {
//                        spooder.visitedHosts.add(uriHost);
                    spooder.visited.add(URL);
                    spooder.currentPageVisitCount++;
                } else {
                    spooder.toVisit.add(URL);
                }
            }
        }

        spooder.Finalize();
    }

}
