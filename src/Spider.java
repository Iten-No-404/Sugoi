import com.panforge.robotstxt.Grant;
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
    private Set<String> deniedVisit = new HashSet<>();

    // A place to store all the parsed robot.txt files that we'll be using
    Map<String, Set<String>> All_rules = new HashMap<>();

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

    // TODO: Work on the recrawl frequency
    // TODO: Work on the multi-threading
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

            // Dump sites we can't visit due to robots.txt
            if (deniedVisit.size() > 0) {
                FileWriter vfw = new FileWriter(Definitions.DENIED_SITES, false);
                // Note that the HashSet doesn't order objects FIFO, the order is random-ish?
                Iterator<String> iter = deniedVisit.iterator();
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



        //int downloadedPage = 0;
        while (spooder.ShouldContinue()) {
            String URL = spooder.NextURL();

            // If for some reason the URL is null, skip this iteration
            // Can't say we visited this website...
            if (URL != null && !URL.equals("")) { // TODO: Look more into URIs
                String uriHost = null;
                // Will probably be useful to
                // For addresses like https://stackoverflow.com/questions/21060992/how-does-java-resolve-a-relative-path-in-new-file
                // This should return something like "stackoverflow.com"
                boolean Access = true;
                // bool that checks whether we can visit this page or not
                try {
                    URI uri = new URI(URL);
                    uriHost = uri.getHost();
                    String uriPath = uri.getPath(), uriProtocol = uri.getScheme();

                    // Extract the top domain name from the url
                    // TODO: deal cases where websites have % in their links
                    // TODO: Maybe work more on robots.txt
                    String reg = "^[^.]*\\.(?=\\w+\\.\\w+$)";

                    String TopDomainName = uriHost;
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
                    Set<String> rules = new HashSet<String>();
                    Set<String> allowed = new HashSet<String>();
                    String[] temp;
                    // Check if this robot.txt file was already parsed before
                    if (spooder.All_rules.get(TopDomainName) == null)
                    {
                        InputStream robotsTxtStream = new URL(uriProtocol, uriHost, "/robots.txt").openStream();

                        RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);

                        String[] splits = robotsTxt.toString().split("\\R");
                        boolean inside_general = false;
                        boolean inside_Sugoi = false;


                        StringBuilder Test = new StringBuilder();
                        for (String s : splits)
                        {
                            //System.out.println(s);
                            // Check If the line I'm on is the start of the general rules
                            if (s.equals("User-agent: *") && !inside_general)
                            {
                                inside_general = true;
                                continue;
                            }

                            // If I'm working on the general or specific rules and find another User-agent, I'm done with this.
                            if (s.split(":")[0].equals("User-agent"))
                            {
                                inside_general = false;
                            }
                            if (inside_general)
                            {
                                temp = s.split(": ");
                                // This is a disallow statement, store it in the hashset.
                                if (temp[0].equals("Disallow"))
                                {
                                    String decoded_path = java.net.URLDecoder.decode(temp[1], StandardCharsets.UTF_8);
                                    rules.add(decoded_path);
                                }
                                else if (temp[0].equals("Allow"))
                                {
                                    allowed.add(temp[1]);
                                }
                                else
                                    continue;
                            }
                            // Here are the sites not specifically allowed to us
                            if (s.equals("User-agent: Sugoi"))
                            {
                                inside_Sugoi = true;
                            }
                            if (inside_Sugoi)
                            {
                                temp = s.split(": ");
                                // This is a disallow statement, store it in the hashset.
                                if (temp[0].equals("Disallow"))
                                {
                                    String decoded_path = java.net.URLDecoder.decode(temp[1], StandardCharsets.UTF_8);
                                    rules.add(decoded_path);
                                }
                                else if (temp[0].equals("Allow"))
                                {
                                    allowed.add(temp[1]);
                                }
                            }
                        }
                        spooder.All_rules.put(TopDomainName, rules);
                    }
                    else
                    {
                        rules  = spooder.All_rules.get(TopDomainName);
                        System.out.println("Using the " + TopDomainName + " robots.txt");
                    }

                    temp = uriPath.split("/");

                    StringBuilder path = new StringBuilder();

                    int n = temp.length;
                    int i = 1;
                    // a bool to check whether I'm adding a "/" or a part of the urlPath
                    boolean Slash = true;

                   while (i < n)
                   {
                        if (Slash)
                        {
                            path.append("/");
                            Slash = false;
                        }
                        else
                        {
                            path.append(temp[i]);
                            Slash = true;
                            i++;
                        }
                        if (rules.contains(path.toString()))
                        {
                            Access = false;
                            break;
                        }
                   }


                   // Last check
                    path.append("/");
                   if (rules.contains(path.toString()))
                       Access = false;
                    // Time to check if the page we're visiting is blocked or not

                   /* boolean hasAccess = robotsTxt.query("Sugoi", uriPath);
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
                    }*/
                    // endregion
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }




               if (Access)
               {
                   boolean downloadSuccessful = HTMLDownloader.DownloadPage(URL);

                   // Add the URL to the visited list if the page is downloaded
                   // or re-insert it into the toVisit list if not.
                   if (downloadSuccessful) {
                       if (uriHost != null)
                       {
                           spooder.visitedHosts.add(uriHost);
                           // This part handles extracting the links from the downloaded page
                           // We first open the downloaded document
                           // Then we parse it for all the links and references in it
                           File doc = new File(Definitions.HTML_DLD_PATH + URL.hashCode() + ".HTML");
                           Document currentDoc = null;
                           try
                           {
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
                           }
                           catch (IOException e)
                           {
                               e.printStackTrace();
                           }
                           // here we extract the links from the parsed document
                           Elements links = null;
                           if (currentDoc != null)
                               links = currentDoc.select("a[href]");
                           // We insert them in a hashset because sometimes, we can extract the same link multiple times
                           Set<String> pageLinks = new HashSet<>();
                           if (links != null)
                               for (Element link : links)
                               {
                                   pageLinks.add(link.attr("abs:href"));
                               }
                           // Here, we iterate over the hashset and append its contents to the toVisit list
                           spooder.toVisit.addAll(pageLinks);
                       }
                       spooder.visited.add(URL);
                       spooder.currentPageVisitCount++;
                   }
                   else
                   {
                       spooder.toVisit.add(URL);
                   }
               }
                else
               {
                   spooder.deniedVisit.add(URL);
               }

            }
        }

        spooder.Finalize();
    }

}
