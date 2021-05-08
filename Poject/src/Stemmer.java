

public class Stemmer {
    private String Word;
    private String actualvalue;
   public Stemmer(String x)
    {
       
      Word= x.toLowerCase();
      actualvalue=x;
    }
    public Boolean vowel(String x)
    {
        if(x.indexOf("a")!=-1 && x.indexOf("e")!=-1 && x.indexOf("i")!=-1 && x.indexOf("o")!=-1 && x.indexOf("u")!=-1)
        {
          return true;
        }
        return false;
    }
    public Boolean vowelwithindex(String x,int index)
    {
        if(index<0)
        return false;
        if(x.charAt(index)!='a'||x.charAt(index)!='e' &&x.charAt(index)!='i' &&x.charAt(index)!='o' && x.charAt(index)!='u')
        {
          return true;
        }
        return false;

    }
    public Boolean consonant(String x)
    {
        if(vowel(x))
        return false;
        else
        {
          int index=  x.indexOf("y");
                     if( index>0)
                     {
                         if(!vowelwithindex(x,index-1))
                       {  while(index>0)
                         {
                            index=x.indexOf("y",index+1);
                            if(index>0)
                            {
                               if(vowelwithindex(x,index-1))
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
        public Boolean Doubleconsonant(String x)
        {
            if(x.length()>=2) // not sure must more than 2 or just 2 enough
            {
              if(x.charAt(x.length()-1)==x.charAt(x.length()-2)) 
               {if(!vowelwithindex(x,x.length()-1))
                {
                    if(! vowelwithindex(x,x.length()-2))
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
        public Boolean endswithS(String x) ///(and similarly for the other letters). what does it mean?  ies, ss???? or not
        {
            if(x.length()>0)
            {
                if(x.charAt(x.length()-1)=='s')
                return true;
          
            }
            return false;
        }
        public int meausre(String z)
        {
            String x="";
             for(int i=0;i<z.length();i++)
             {
                 if(vowelwithindex(z,i) ||(z.charAt(i)=='y'&& i>0 && (vowelwithindex(z,i-1))))
                 {
                     x+="v";
                 }
                 else 
                 {
                           x+="c";
                 }
             }
            
             return x.split("cv", -1).length - 1;

        }
        public Boolean endscvc(String z)
        {
            if(z.length()>=3)
            {
                String x="";
                for(int i=z.length()-3;i<z.length();i++)
                {
                    if(vowelwithindex(z,i) ||(z.charAt(i)=='y'&& i>0 && (vowelwithindex(z,i-1))))
                    {
                        x+="v";
                    }
                    else if(i==z.length()-3)
                    {
                              x+="c";
                    }
                    else if( z.charAt(i)!='w' && z.charAt(i)!='x' && z.charAt(i)!='y')
                    {
                        x+="c";
                    }

                }
               if(x.equals("cvc") )
               {
                   return true;
               }
               

            }
            return false;

        }
    
    public static void main(String argv[])
    {

    }
    
    
}
