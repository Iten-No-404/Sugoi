import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

// https://www.geeksforgeeks.org/download-web-page-using-java/
// Gets a URL from the spider, tries to connect to the page and download it
// If there's an error, should return false and the URL should be returned to the toVisit list
// Otherwise, should add it to the visited list.

// Maybe we should move this to the crawler?
public class HTMLDownloader {
    public static boolean DownloadPage(String URL, StringBuilder HTML) {
        if (URL == null) return false;
        String threadID = String.valueOf(Thread.currentThread().getId());
        try {

            URL url = new URL(URL);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream())); // Long boi

            String line;
            /* TODO Find a way to not throw away anything past 16MB of HTML
                Suggestion: use a backup buffer, better yet, a buffer list
                pass the array to this function and when you're too big on one
                you can grow the array and load into that buffer.
                In the main loop, you can insert multiple documents under the same URL
                 */
            boolean tooBig =false;
            while ((line = br.readLine()) != null && !tooBig) {
                HTML.append(line);
                tooBig = HTML.toString().length() > Definitions.BSON_MAX_SIZE;
            }
            // If too big, keep removing until under 16MB
            // While loop since we don't know what the last line's length is.
            // Altough we can find it's length....
            // TODO convert this from a while loop to just removing the last line's length off the end
            if (tooBig)
            {
                while (HTML.toString().length() > Definitions.BSON_MAX_SIZE)
                {
                    HTML.delete(HTML.toString().length() - 1024,HTML.toString().length());
                }
                System.out.println(Thread.currentThread().getName() + ": URL " + URL + " EXCEEDED BSON MAX SIZE, REMOVING LAST 255 CHARS");
            }

            br.close();
//            System.out.format("\n Thread %s: URL %s downloaded successfully\n",threadID ,url);

            // If the download returned nothing, this URL hasn't been really visited
            if(HTML.toString().equals(""))
                return false;
            return true;
        }
        // There must be a better way to do this than just returning false in all blocks..
        catch (MalformedURLException me) {
            System.out.format("Thread %s, URL: %s, ERROR: MALFORMED URL\n", threadID,URL);
            return false;
        } catch (IOException ie) {
            System.out.format("Thread %s, URL: %s, ERROR: IO EXCEPTION\n", threadID,URL);


            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
