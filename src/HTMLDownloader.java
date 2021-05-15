import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

// https://www.geeksforgeeks.org/download-web-page-using-java/
// Gets a URL from the spider, tries to connect to the page and download it
// If there's an error, should return false and the URL should be returned to the toVisit list
// Otherwise, should add it to the visited list.

// Maybe we should move this to the crawler?
public class HTMLDownloader {
    public static boolean DownloadPage(String URL) {
        if (URL == null) return false;
        try {
            URL url = new URL(URL);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream())); // Long boi
            String fileName = Definitions.HTML_DLD_PATH + String.valueOf(URL.hashCode()) + ".HTML";
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName)); // Longer boi

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
            }
            br.close();
            bw.close();
            System.out.format("\n%s downloaded successfully", url);

            return true;
        }
        // There must be a better way to do this than just returning false in all blocks..
        catch (MalformedURLException me) {
            System.out.println("ERROR: MALFORMED URL");
            return false;
        } catch (IOException ie) {
            System.out.println("ERROR: IO EXCEPTION");

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
