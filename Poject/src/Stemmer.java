

public class Stemmer {
    private String Word;
    private String actualvalue;
   public Stemmer(String x)
    {
       
      Word= x.toLowerCase();
      actualvalue=x;
    }
    public Boolean vowel()
    {
        if(Word.indexOf("a")!=-1 && Word.indexOf("e")!=-1 && Word.indexOf("i")!=-1 && Word.indexOf("o")!=-1 && Word.indexOf("u")!=-1)
        {
          return true;
        }
        return false;
    }
    public Boolean vowelwithindex(int index)
    {
        if(Word.charAt(index)!='a'||Word.charAt(index)!='e' &&Word.charAt(index)!='i' &&Word.charAt(index)!='o' && Word.charAt(index)!='u')
        {
          return true;
        }
        return false;

    }
    public Boolean consonant()
    {
        if(vowel())
        return false;
        else
        {
          int index=  Word.indexOf("y");
                     if( index>0)
                     {
                         if(!vowelwithindex(index-1))
                       {  while(index>0)
                         {
                            index=Word.indexOf("y",index+1);
                            if(index>0)
                            {
                               if(vowelwithindex(index-1))
                               return false;

                            }
                         }
                         return true;
                        }


                         else
                         return false;
                     }
                    else
                    {
                        return true;
                    }
            
        }
    }
        public Boolean Doubleconsonant()
        {
            if(Word.length()>=2) // not sure must more than 2 or just 2 enough
            {
              if(Word.charAt(Word.length()-1)==Word.charAt(Word.length()-2)) 
               {if(!vowelwithindex(Word.length()-1))
                {
                    if(! vowelwithindex(Word.length()-2))
                    { 
                        return true;

                  
                    }
                    else
                    return false;
                }
                else
                   return false;
              }
              else
              return false;
            }
            return false;
        }
        public Boolean endswithS() ///(and similarly for the other letters). what does it mean?  ies, ss???? or not
        {
            if(Word.length()>0)
            {
                if(Word.charAt(Word.length()-1)=='s')
                return true;
          
            }
            return false;
        }
    
    public static void main(String argv[])
    {

    }
    
    
}
