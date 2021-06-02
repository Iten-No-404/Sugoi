import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import java.io.IOException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Indexer {

    public  Indexer()
    {

    }
    public Boolean Parse_HTML() {
        try {
            // it should be edited to get link from DB
            Stemmer stemmer=new Stemmer();
            // connect to link
            Document doc = Jsoup.connect("https://en.wikipedia.org/").get();
            // get URL of page
               String URL="https://en.wikipedia.org/";
               // select all tags and words from HTML page
            Elements elements = doc.select("*");
            String text=Jsoup.clean(doc.toString(), Whitelist.none());
            // only to get length to use it in TF
             String AllWords[]=stemmer.Spliter(text);

            int j=0;
            // get all elemnts
            for(Element e : elements) {

                String []words= stemmer.Spliter(e.text());
                for(int i=0;i<words.length;i++)
                {
                    // choose to build from zero or update
                  if(Definitions.CHOOSE_INDEX)
                  {
                             // build indexer
                      Build_Indexer(URL,e.tagName(),j,words[i],AllWords.length);

                  }
                  else
                  {
                      // update indexer
                      Update_Indexer(URL,e.tagName(),j,words[i],AllWords.length);

                  }
                  j++;
                }


            }


            return  true;

        }
        catch (IOException exception)
        {
                System.out.println("error");
                return  false;
        }


    }
    public  void Build_Indexer( String URL,String type,int index,String word,int lengthofdoc)
    {
           // steps of building indexer
            Stemmer stemmer=new Stemmer();
            // stemm words
            Link Links= new Link();
            Word Words=new Word();
            stemmer.PorterStemming(word);
            // insert words
            Words.findWord(word,index,URL,type,lengthofdoc);
            // insert docs
            Links.findDoc(word,index,URL,type);
            Words.UpdateIDE();


    }
    public void Update_Indexer(String URL,String type,int index,String word,int lengthofdoc)
    {
       // steps of Update indexer
        Stemmer stemmer=new Stemmer();
        Link Links= new Link();
        Word Words=new Word();
                // steem words
            stemmer.PorterStemming(word);
            // update words
            Words.UpdateWords(word,index,URL,type,lengthofdoc);
            // update links
            Links.updateDocs(word,index,URL,type);
            // delete removed words
            Links.DeleteWordsFromdocs(URL);
            // reset the status
            Links.Resetdrop();
            Words.Resetdrop();
            // Update IDE
            Words.UpdateIDE();


    }
    public  static  void main(String argv[])
    {
        Indexer indexer=new Indexer();
        indexer.Parse_HTML();
    }
}
