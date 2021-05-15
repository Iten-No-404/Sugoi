import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.IOException;


public class HTML {
    public static void main(String argv[])
    {
    	String HTMLSTring = "<!DOCTYPE html>" + "<html>" + "<head>" + "<title>JSoup Example</title>" + "</head>" + "<body>" + "<table><tr><td> <h1>HelloWorld</h1></tr>" + "</table>" + "</body>" + "</html>";
//    	Document html = Jsoup.parse(HTMLSTring);
//    	String title = html.title(); 
//    	String h1 = html.body().getElementsByTag("h1").text(); 
//    	System.out.println("Input HTML String to JSoup :" + HTMLSTring); 
//    	System.out.println("After parsing, Title : " + title);
//    	System.out.println("Afte parsing, Heading : " + h1);
    	String cleanedHTML=	Jsoup.clean(HTMLSTring, Whitelist.none());
    	String title;
      System.out.println(cleanedHTML);
      Document doc=null;
      try { doc = Jsoup.connect("https://en.wikipedia.org/wiki/Document-oriented_database").get(); 
      title = doc.title();
      }
     
      catch (IOException e) { e.printStackTrace();
      System.out.println("iii");
      }
      
      
       
      System.out.println( Jsoup.clean(doc.toString(), Whitelist.none()));
      
  
       
    	System.out.print("00");
      Document D;
    }
}
