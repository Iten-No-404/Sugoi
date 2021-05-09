import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

// https://www.geeksforgeeks.org/download-web-page-using-java/
public class HTMLDownloader {
    public static void DownloadPage(String URL) {
        if(URL == null) return;
        try {
            URL url = new URL(URL);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream())); // Long boi
            String fileName = String.valueOf(URL.hashCode()) + ".HTML";
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName)); // Longer boi

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
            }
            br.close();
            bw.close();
           System.out.format("\n%s downloaded successfully", url);
        }catch(MalformedURLException me)
        {
            System.out.println("ERROR: MALFORMED URL");
        }
        catch(IOException ie)
        {
            System.out.println("ERROR: IO EXCEPTION");
        }
         catch (Exception e) {
            e.printStackTrace();
        }
    }
}
