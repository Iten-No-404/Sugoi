import java.util.ArrayList;  
import java.util.Arrays;  
import java.io.File;
import java.io.IOException; 
import org.jsoup.Jsoup; 
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import java.io.FileWriter; 
import java.io.IOException; 
public class Stemmer implements Runnable {
    private String Word;
    private String actualvalue;

    public Stemmer(String x) {

        Word = x.toLowerCase();
        actualvalue = x;
    }
    public void run()
    { 
        // for nothing untill now 
        System.out.print("ooo");

    }

    public Boolean vowel(String x) {
        //check the word contain  vowel char or no
        if (x.indexOf("a") != -1 || x.indexOf("e") != -1 || x.indexOf("i") != -1 || x.indexOf("o") != -1
                || x.indexOf("u") != -1) {
            return true;
        }
        return false;
    }

    public Boolean vowelwithindex(String x, int index) {
        // check the word contains vowel in specfic index
        if (index < 0)
            return false;
        if (x.charAt(index) == 'a' || x.charAt(index) == 'e' || x.charAt(index) == 'i' || x.charAt(index) == 'o'
                || x.charAt(index) == 'u') {
            return true;
        }
        return false;

    }

    public Boolean consonant(String x) {

        for (int i = 0; i < x.length(); i++) {
            Boolean v = vowelwithindex(x, i);
            if (v && i + 1 == x.length()) {
                return false;
            } else if (v && i + 1 < x.length() && x.charAt(i + 1) != 'y') {
                return false;
            }
        }
        return true;

    }

    public Boolean Doubleconsonant(String x) {
        // chck there is  two consonant in end of the word
        if (x.length() >= 2) 
        {

            if (!vowelwithindex(x, x.length() - 1)) {
                if (!vowelwithindex(x, x.length() - 2)) {
                    return true;

                } else
                    return false;
            } else
                return false;

        }
        return false;
    }

 

    public int measure(String z) {
        // compute m 
        // the vowevl char is v else is c them count vc in the word  
        String x = "";
        for (int i = 0; i < z.length(); i++) {
            if (vowelwithindex(z, i) || (z.charAt(i) == 'y' && i > 0 && (vowelwithindex(z, i - 1)))) {
                x += "v";
            } else {
                x += "c";
            }
        }
  
   
        return  x.split("vc", -1).length - 1;


    }

    public Boolean endscvc(String z) {
        // check the word ends with cvc or not 
        // c is consonant
        // v vowel
        if (z.length() >= 3) {
            String x = "";
            for (int i = z.length() - 3; i < z.length(); i++) {
                if (vowelwithindex(z, i) || (z.charAt(i) == 'y' && i > 0 && (vowelwithindex(z, i - 1)))) {
                    x += "v";
                } else if (i == z.length() - 3) {
                    x += "c";
                } else if (z.charAt(i) != 'w' && z.charAt(i) != 'x' && z.charAt(i) != 'y') {
                    x += "c";
                }

            }
            if (x.equals("cvc")) {
                return true;
            }

        }
        return false;

    }

    public String step1a(String s) {
        if (s.length() >= 4 && s.toLowerCase().endsWith("sses")) {
            s = s.substring(0, s.length() - 2); // Removes the es at the end of the word
        } else if (s.length() >= 3 && s.toLowerCase().endsWith("ies")) {
            s = s.substring(0, s.length() - 2); // Removes the es at the end of the word
        } else if (s.length() >= 2 && !(s.toLowerCase().endsWith("ss")) && s.toLowerCase().endsWith("s")) {
            s = s.substring(0, s.length() - 1); // Removes the s at the end of the word
        }
        return s;
    }

    public String step1b(String s) {
        if (s.length() >= 3 && measure(s.substring(0, s.length() - 3)) > 0 && s.toLowerCase().endsWith("eed")) {
            s = s.substring(0, s.length() - 1); // Removes the d at the end of the word
        } else if (s.length() >= 2 && vowel(s.substring(0, s.length() - 2)) && s.toLowerCase().endsWith("ed")
                && !s.toLowerCase().endsWith("eed")) {
            s = s.substring(0, s.length() - 2); // Removes the ed at the end of the word
            s = step1bfollowup(s);
        } else if (s.length() >= 3 && vowel(s.substring(0, s.length() - 3)) && s.toLowerCase().endsWith("ing")) {
            s = s.substring(0, s.length() - 3); // Removes the ing at the end of the word
            s = step1bfollowup(s);
        }
        return s;
    }

    public String step1bfollowup(String s) {
        if(s.length() >= 2 && (s.toLowerCase().endsWith("at") || s.toLowerCase().endsWith("bl") || s.toLowerCase().endsWith("iz"))) 
        {
            s = s.concat("e"); // Add an e at the end of the word
        } else if (s.length() >= 1 && Doubleconsonant(s)
                && !(s.substring(0, s.length() - 1).toLowerCase().endsWith("l")
                        || s.substring(0, s.length() - 1).toLowerCase().endsWith("s")
                        || s.substring(0, s.length() - 1).toLowerCase().endsWith("z"))) {
            s = s.substring(0, s.length() - 1); // Removes the repeated constant at the end of the word
        } else if (measure(s) == 1 && endscvc(s)) {
            s = s.concat("e"); // Add an e at the end of the word
        }
        return s;
    }

    public String step1c(String s) {
        if (s.length() >= 1 && vowel(s.substring(0, s.length() - 1)) && s.toLowerCase().endsWith("y")) {
            s = s.substring(0, s.length() - 1) + "i";// Replaces the y by an i at the end of the word
        }
        return s;
    }

    public String step2(String s) {
      
        if (s.length() >= 7 && measure(s.substring(0, s.length() - 7)) > 0 && s.toLowerCase().endsWith("ational")) {
            s = s.substring(0, s.length() - 7) + "ate"; // ATIONAL -->  ATE
        } else if (s.length() >= 7 && measure(s.substring(0, s.length() - 7)) > 0 && s.toLowerCase().endsWith("ization")) {
            s = s.substring(0, s.length() - 7) + "ize"; // IZATION -->  IZE
        } else if (s.length() >= 7 && measure(s.substring(0, s.length() - 7)) > 0 && s.toLowerCase().endsWith("iveness")) {
            s = s.substring(0, s.length() - 7) + "ive"; // IVENESS -->  IVE
        } else if (s.length() >= 7 && measure(s.substring(0, s.length() - 7)) > 0 && s.toLowerCase().endsWith("fulness")) {
            s = s.substring(0, s.length() - 7) + "ful"; // FULNESS -->  FUL
        } else if (s.length() >= 7 && measure(s.substring(0, s.length() - 7)) > 0 && s.toLowerCase().endsWith("ousness")) {
            s = s.substring(0, s.length() - 7) + "ous"; // OUSNESS -->  OUS
        } else if (s.length() >= 6 && measure(s.substring(0, s.length() - 6)) > 0 && s.toLowerCase().endsWith("tional")) {
            s = s.substring(0, s.length() - 6) + "tion";// TIONAL  -->  TION
        } else if (s.length() >= 6 && measure(s.substring(0, s.length() - 6)) > 0 && s.toLowerCase().endsWith("biliti")) {
            s = s.substring(0, s.length() - 6) + "ble"; // BILITI  -->  BLE
        } else if (s.length() >= 5 && measure(s.substring(0, s.length() - 5)) > 0 && s.toLowerCase().endsWith("entli")) {
            s = s.substring(0, s.length() - 5) + "ent"; // ENTLI   -->  ENT
        } else if (s.length() >= 5 && measure(s.substring(0, s.length() - 5)) > 0 && s.toLowerCase().endsWith("ousli")) {
            s = s.substring(0, s.length() - 5) + "ous"; // OUSLI   -->  OUS
        } else if (s.length() >= 5 && measure(s.substring(0, s.length() - 5)) > 0 && s.toLowerCase().endsWith("ation")) {
            s = s.substring(0, s.length() - 5) + "ate"; // ATION   -->  ATE
        } else if (s.length() >= 5 && measure(s.substring(0, s.length() - 5)) > 0 && s.toLowerCase().endsWith("alism")) {
            s = s.substring(0, s.length() - 5) + "al";  // ALISM   -->  AL
        } else if (s.length() >= 5 && measure(s.substring(0, s.length() - 5)) > 0 && s.toLowerCase().endsWith("aliti")) {
            s = s.substring(0, s.length() - 5) + "al";  // ALITI   -->  AL
        } else if (s.length() >= 5 && measure(s.substring(0, s.length() - 5)) > 0 && s.toLowerCase().endsWith("iviti")) {
            s = s.substring(0, s.length() - 5) + "ive"; // IVITI   -->  IVE
        } else if (s.length() >= 4 && measure(s.substring(0, s.length() - 4)) > 0 && s.toLowerCase().endsWith("enci")) {
            s = s.substring(0, s.length() - 4) + "ence";// ENCI    -->  ENCE
        } else if (s.length() >= 4 && measure(s.substring(0, s.length() - 4)) > 0 && s.toLowerCase().endsWith("anci")) {
            s = s.substring(0, s.length() - 4) + "ance";// ANCI    -->  ANCE
        } else if (s.length() >= 4 && measure(s.substring(0, s.length() - 4)) > 0 && s.toLowerCase().endsWith("izer")) {
            s = s.substring(0, s.length() - 4) + "ize"; // IZER    -->  IZE
        } else if (s.length() >= 4 && measure(s.substring(0, s.length() - 4)) > 0 && s.toLowerCase().endsWith("abli")) {
            s = s.substring(0, s.length() - 4) + "able";// ABLI    -->  ABLE
        } else if (s.length() >= 4 && measure(s.substring(0, s.length() - 4)) > 0 && s.toLowerCase().endsWith("alli")) {
            s = s.substring(0, s.length() - 4) + "al";  // ALLI    -->  AL
        } else if (s.length() >= 4 && measure(s.substring(0, s.length() - 4)) > 0 && s.toLowerCase().endsWith("ator")) {
            s = s.substring(0, s.length() - 4) + "ate"; // ATOR    -->  ATE
        } else if (s.length() >= 3 && measure(s.substring(0, s.length() - 3)) > 0 && s.toLowerCase().endsWith("eli")) {
            s = s.substring(0, s.length() - 3) + "e";   // ELI     -->  E
        } 
        return s;
    }

    public String Step5_a(String x) {
           

        if (x.length() >= 2) {
            String y = x.substring(0, x.length() - 1);
            int m = measure(y);  //         (m>1) E     ->      
            if (m > 1 && x.charAt(x.length() - 1) == 'e') {
                return y;   // (m=1 and not *o) E ->  
            } else if (m == 1 && !endscvc(y) && x.charAt(x.length() - 1) == 'e') {
                return y;

            }
        }
        return x;
    }

    public String Step5_b(String x) {
        if (x.length() >= 2) {
            //  (m > 1 and *d and *L) -> single letter
            String y = x.substring(0, x.length() - 1);
            int m = measure(x);
            if (m > 1 && Doubleconsonant(x) && x.charAt(x.length() - 1) == 'l') {
                return y;
            }

        }

        return x;

    }
 
        public String Step3(String x)
        {
            String y;
               // (m>0) ATIVE -> 
            if(  x.endsWith("ative") || x.endsWith("ative"))
            {
                y =x.substring(0,x.length()-5);
                if(measure(y)>0)
                {
                    return y;
                }
    
            } // (m>0) ICATE ->  IC  
            else if(x.endsWith("icate"))
            {
                y =x.substring(0,x.length()-5);
                if(measure(y)>0)
                {
                    y+="ic";
                    return y;
                }
            }
             // (m>0) ALIZE ->  AL        
            else if(x.endsWith("alize"))
            {
                y =x.substring(0,x.length()-5);
                if(measure(y)>0)
                {
                    y+="al";
                    return y;
                }
            }
             // (m>0) ICITI ->  IC     
            else if(x.endsWith("iciti"))
            {
                y =x.substring(0,x.length()-5);
                if(measure(y)>0)
                {
                    y+="ic";
                    return y;
                }
            }  // (m>0) ICAL  ->  IC         
            else if(x.endsWith("ical"))
            {
                y =x.substring(0,x.length()-4);
                if(measure(y)>0)
                {
                    y+="ic";
                    return y;
                }
            }   // (m>0) NESS  ->
            else if(x.endsWith("ness"))
            {
                y =x.substring(0,x.length()-4);
                if(measure(y)>0)
                {
                  
                    return y;
                }
            }    // (m>0) FUL   ->        
            else if(x.endsWith("ful"))
            {
                y =x.substring(0,x.length()-3);
                if(measure(y)>0)
                {
                  
                    return y;
                }
            }
            return x;
        }
    

    public String Step4(String x) {

        String y;            
        if (x.endsWith("ement")) {
            y = x.substring(0, x.length() - 5);
            if (measure(y) > 1) {
                return y;
            }
            
          } // (m>1) ANCE  ->        // (m>1) ENCE  ->    // (m>1) ABLE  ->         // (m>1) IBLE  ->            
         else if (x.endsWith("ance") || x.endsWith("ence") || x.endsWith("able") || x.endsWith("ible")
                || x.endsWith("ment"))    // (m>1) MENT  ->                 
                
         {
            y = x.substring(0, x.length() - 4);
            if (measure(y) > 1) {
                return y;
            }

        }    // (m>1) ANT   ->      // (m>1) ENT   ->      // (m>1) ISM   ->   // (m>1) ATE   ->         // (m>1) ITI   ->     
        else if (x.endsWith("ant") || x.endsWith("ent") || x.endsWith("ism") || x.endsWith("ate") || x.endsWith("iti")
                || x.endsWith("ous") || x.endsWith("ive") || x.endsWith("ize"))
                      // (m>1) OUS   ->       // (m>1) IVE   ->      // (m>1) IZE   ->  
        {
            y = x.substring(0, x.length() - 3);
            if (measure(y) > 1) {
                return y;
            }

        }
           // (m>1 and (*S or *T)) ION ->   
        else if (x.endsWith("ion")) {
            y = x.substring(0, x.length() - 3);
            if (measure(y) > 1 && (y.endsWith("s") || y.endsWith("t"))) {
                return y;
            }

        }  // (m>1) AL    ->           // (m>1) ER    ->         // (m>1) IC    ->         // (m>1) OU    ->         
        else if (x.endsWith("al") || x.endsWith("er") || x.endsWith("ic") || x.endsWith("ou")) {
            y = x.substring(0, x.length() - 2);
            if (measure(y) > 1) {
                return y;
            }

        }

        return x;
    }

    public String[] Spliter(String text) {
    // split text to words
        String[] words = text.split("\\s+");
        for (int i = 0; i < words.length; i++) {

            words[i] = words[i].replaceAll("[^\\w]", "");
         
        }
        ArrayList<String> ArrL = new ArrayList<String>(Arrays.asList(words));
        for(int i=0; i< ArrL.size(); i++)
            if(ArrL.get(i) == "")
                ArrL.remove(i);
        words= ArrL.toArray(words);
        return words;
    }

    public String PorterStemming(String s)
    {
        s= s.toLowerCase();
        s = step1a(s);
        s = step1b(s);
        s = step1c(s);
        s = step2(s);
        s = Step3(s);
        s = Step4(s);
        s = Step5_a(s);
        s = Step5_b(s);
        return s;
    }

    public static void main(String argv[]) {
        // let us check

        Stemmer s = new Stemmer("SSES");

        // System.out.println(s.vowel("xox"));
        // System.out.println(s.vowelwithindex("xox", 1));
        // System.out.println(s.consonant("toy"));

        // System.out.println("");
        // System.out.println(s.step1a("caresses"));
        // System.out.println(s.step1a("ponies"));
        // System.out.println(s.step1a("ties"));
        // System.out.println(s.step1a("caress"));
        // System.out.println(s.step1a("cats"));

        // System.out.println("");
        // System.out.println(s.step1b("feed"));
        // System.out.println(s.step1b("agreed"));
        // System.out.println(s.step1b("plastered"));
        // System.out.println(s.step1b("bled"));
        // System.out.println(s.step1b("motoring"));
        // System.out.println(s.step1b("sing"));

        // System.out.println("");
        // Testing the 1b followup function
        // System.out.println(s.step1b("conflated"));
        // System.out.println(s.step1b("troubled"));
        // System.out.println(s.step1b("sized"));
        // System.out.println(s.step1b("hopping"));
        // System.out.println(s.step1b("tanned"));
        // System.out.println(s.step1b("falling"));
        // System.out.println(s.step1b("hissing"));
        // System.out.println(s.step1b("fizzed"));
        // System.out.println(s.step1b("failing"));
        // System.out.println(s.step1b("filing"));

        // System.out.println("");
        // System.out.println(s.step1c("happy"));
        // System.out.println(s.step1c("sky"));

        // System.out.println("");
        // System.out.println(s.step2("relational"));
        // System.out.println(s.step2("conditional"));
        // System.out.println(s.step2("rational"));
        // System.out.println(s.step2("valenci"));
        // System.out.println(s.step2("hesitanci"));
        // System.out.println(s.step2("digitizer"));
        // System.out.println(s.step2("conformabli"));
        // System.out.println(s.step2("radicalli"));
        // System.out.println(s.step2("differentli"));
        // System.out.println(s.step2("vileli"));
        // System.out.println(s.step2("analogousli"));
        // System.out.println(s.step2("vietnamization"));
        // System.out.println(s.step2("predication"));
        // System.out.println(s.step2("operator"));
        // System.out.println(s.step2("feudalism"));
        // System.out.println(s.step2("decisiveness"));
        // System.out.println(s.step2("hopefulness"));
        // System.out.println(s.step2("callousness"));
        // System.out.println(s.step2("formaliti"));
        // System.out.println(s.step2("sensitiviti"));
        // System.out.println(s.step2("sensibiliti"));

        // System.out.println("");
        // System.out.println(s.Step5_a("probate"));
        // System.out.println(s.Step5_b("controll"));
        // System.out.println(s.Step5_b("roll"));
        // System.out.println(s.Step4("revival"));
        // System.out.println(s.Step4("allowance"));
        // System.out.println(s.Step4("inference"));
        // System.out.println(s.Step4("airliner"));
        // System.out.println(s.Step4("gyroscopic"));
        // System.out.println(s.Step4("adjustable"));
        // System.out.println(s.Step4("defensible"));
        // System.out.println(s.Step4("irritant"));
        // System.out.println(s.Step4("replacement"));
        // System.out.println(s.Step4("adjustment"));
        // System.out.println(s.Step4("dependent"));
        // System.out.println(s.Step4("adoption"));
        // System.out.println(s.Step4("homologou"));
        // System.out.println(s.Step4("communism"));
        // System.out.println(s.Step4("activate"));
        // System.out.println(s.Step4("angulariti"));
        // System.out.println(s.Step4("homologous"));
        // System.out.println(s.Step4("effective"));
        // System.out.println(s.Step4("bowdlerize"));
        // System.out.println(s.Step3("triplicate"));
        // System.out.println(s.Step3("formative"));
        // System.out.println(s.Step3("formalize"));
        // System.out.println(s.Step3("electriciti"));
        // System.out.println(s.Step3("electrical"));
        // System.out.println(s.Step3("hopeful"));
        // System.out.println(s.Step3("goodness"));
 
        // System.out.println(" ");
        // String Arr[] = s.Spliter(
        //         "In the rules below , examples of their application successful or otherwise , are given on the right in lower case. The algorithm now follows:");
        // for(int i=0; i<Arr.length; i++)
        //     System.out.println(Arr[i]);
        // //Clear empty words
      
        // System.out.println(" ");
        // for(int i=0; i<Arr.length; i++)
        //     System.out.println(Arr[i]);

        

        //     ///Big Test Here:

        //     String title;
        //     Document doc=null;
        //     String FileName = "-808050354.html";
        //     try { doc = Jsoup.parse(new File(FileName), "ISO-8859-1");
        //     title = doc.title();
        //     }
           
        //     catch (IOException e) { e.printStackTrace();
        //     System.out.println("iii");
        //     }
            
        //     String Arrr = Jsoup.clean(doc.toString(), Whitelist.none());
        //     Arr = s.Spliter(Arrr);
        //     ArrL = new ArrayList<String>(Arrays.asList(Arr));
        //     for(int i=0; i< ArrL.size(); i++)
        //         if(ArrL.get(i) == "")
        //             ArrL.remove(i);
        //     Arr = ArrL.toArray(Arr);
        //     System.out.println(" ");

        //     try {
        //         FileWriter myWriter = new FileWriter(FileName.substring(0, FileName.length()-5)+".txt");
        //         for(int i=0; i<Arr.length; i++)
        //         {
        //             if(Arr[i] != null)
        //                 myWriter.write(Arr[i] + "\n");
        //             System.out.println(Arr[i]);
        //         }
        //         myWriter.close();
        //         System.out.println("Successfully wrote to the file.");
        //       } catch (IOException e) {
        //         System.out.println("An error occurred.");
        //         e.printStackTrace();
        //       }
    
        //       try {
        //         FileWriter myWriter = new FileWriter(FileName.substring(0, FileName.length()-5)+ "-stemmed"+".txt");
        //         for(int i=0; i<Arr.length; i++)
        //         {
        //             if(Arr[i] != null)
        //                 myWriter.write(Arr[i] + "\t\t\t\t\t\t\t" + s.PorterStemming(Arr[i]) + "\n");
        //             System.out.println(Arr[i]);
        //         }
        //         myWriter.close();
        //         System.out.println("Successfully wrote to the file.");
        //       } catch (IOException e) {
        //         System.out.println("An error occurred.");
        //         e.printStackTrace();
        //       }
    }

}
