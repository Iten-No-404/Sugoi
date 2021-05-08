
public class Stemmer {
    private String Word;
    private String actualvalue;

    public Stemmer(String x) {

        Word = x.toLowerCase();
        actualvalue = x;
    }

    public Boolean vowel(String x) {
        if (x.indexOf("a") != -1 || x.indexOf("e") != -1 || x.indexOf("i") != -1 || x.indexOf("o") != -1
                || x.indexOf("u") != -1) {
            return true;
        }
        return false;
    }

    public Boolean vowelwithindex(String x, int index) {
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
        if (x.length() >= 2) // not sure must more than 2 or just 2 enough
        {
            if (x.charAt(x.length() - 1) == x.charAt(x.length() - 2)) {
                if (!vowelwithindex(x, x.length() - 1)) {
                    if (!vowelwithindex(x, x.length() - 2)) {
                        return true;

                    } else
                        return false;
                } else
                    return false;
            } else
                return false;
        }
        return false;
    }

    public Boolean endswithS(String x) /// (and similarly for the other letters). what does it mean? ies, ss???? or not
    {
        if (x.length() > 0) {
            if (x.charAt(x.length() - 1) == 's')
                return true;

        }
        return false;
    }

    public int measure(String z) {
        String x = "";
        for (int i = 0; i < z.length(); i++) {
            if (vowelwithindex(z, i) || (z.charAt(i) == 'y' && i > 0 && (vowelwithindex(z, i - 1)))) {
                x += "v";
            } else {
                x += "c";
            }
        }

        return x.split("cv", -1).length - 1;

    }

    public Boolean endscvc(String z) {
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
            s = s.substring(0, s.length() - 3); // Removes the es at the end of the word
        } else if (s.length() >= 3 && s.toLowerCase().endsWith("ies")) {
            s = s.substring(0, s.length() - 3); // Removes the es at the end of the word
        } else if (s.length() >= 2 && !(s.toLowerCase().endsWith("ss")) && s.toLowerCase().endsWith("s")) {
            s = s.substring(0, s.length() - 2); // Removes the s at the end of the word
        }
        return s;
    }

    public String step1b(String s) {
        if (measure(s) > 0 && s.toLowerCase().endsWith("eed")) {
            s = s.substring(0, s.length() - 2); // Removes the d at the end of the word
        } else if (vowel(s) && s.toLowerCase().endsWith("ed")) {
            s = s.substring(0, s.length() - 3); // Removes the ed at the end of the word
            s = step1bfollowup(s);
        } else if (vowel(s) && s.toLowerCase().endsWith("ing")) {
            s = s.substring(0, s.length() - 4); // Removes the ing at the end of the word
            s = step1bfollowup(s);
        }
        return s;
    }

    public String step1bfollowup(String s) {
        if (measure(s) == 1 && endscvc(s)) {
            s = s.concat("e"); // Add an e at the end of the word
        } else if (Doubleconsonant(s)
                && !(s.toLowerCase().endsWith("l") || s.toLowerCase().endsWith("s") || s.toLowerCase().endsWith("z"))) {
            s = s.substring(0, s.length() - 2); // Removes the repeated constant at the end of the word
        }
        return s;
    }

    public String step1c(String s) {
        if (vowel(s) && s.toLowerCase().endsWith("y")) {
            s = s.substring(0, s.length() - 2) + "i";// Replaces the y by an i at the end of the word
        }
        return s;
    }

    public String Step5_a(String x) {
        if (x.length() >= 2) {
            String y = x.substring(0, x.length() - 1);
            int m = measure(y);
            if (m > 1 && x.charAt(x.length() - 1) == 'e') {
                return y;
            } else if (m == 1 && !endscvc(y) && x.charAt(x.length() - 1) == 'e') {
                return y;

            }
        }
        return x;
    }

    public String Step5_b(String x) {
        if (x.length() >= 2) {
            String y = x.substring(0, x.length() - 1);
            int m = measure(x);
            if (m > 1 && Doubleconsonant(x) && x.charAt(x.length() - 1) == 'l') {
                return y;
            }

        }

        return x;

    }

    public static void main(String argv[]) {
        // let us check

        Stemmer s = new Stemmer("SSES");
        System.out.println(s.endswithS("sess"));
        System.out.println(s.vowel("xox"));
        System.out.println(s.vowelwithindex("xox", 1));
        System.out.println(s.consonant("toy")); 
        System.out.println(s.Step5_a("probate"));
    }

}
