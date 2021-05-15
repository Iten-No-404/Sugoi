import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Spider {

    private static final int MAX_PAGES = 10; /// < Max number of pages the crawler is allowed to visit.
    /// We don't want to insert the same URL twice into the visited list.
    private Set<String> visited = new HashSet<String>();
    /// Can also be an array, but a linked list is good if we want to expand so much
    /// That finding a large contiguous memory block will be hard.
    /// The speed loss of not using an array is not that big since the visiting of
    /// pages and downloading
    /// isn't really that fast anyway. We also don't access certain indecies.
    /// Instead, we go through the
    /// List element after element.
    private List<String> toVisit = new LinkedList<String>();
    private Integer currentPageVisitCount = 0;
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
        } while (visited.contains(next) && !toVisit.isEmpty());

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
            // First, to_visit.txt
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
            // Then, visited.txt. This time we want to append.
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

            boolean downloadSuccessful = HTMLDownloader.DownloadPage(URL);

            // Add the URL to the visited list if the page is downloaded
            // or re-insertt it into the toVisit list if not.
            if (downloadSuccessful) {
                spooder.visited.add(URL);
                spooder.currentPageVisitCount++;
            }
            else
            {
                spooder.toVisit.add(URL);
            }
        }

        spooder.Finalize();
    }

}
