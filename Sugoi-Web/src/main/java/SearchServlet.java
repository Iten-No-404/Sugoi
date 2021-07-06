import java.io.*;
import java.util.ArrayList;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.servlet.*;
import com.sugoi.back.QueryProcess;

@WebServlet(name = "com.sugoi.back.SearchServlet", value = "/Search")
public class SearchServlet extends HttpServlet {

    //String[] URLs = {"https://www.w3schools.com/java/java_arrays.asp", "https://randomwordgenerator.com/paragraph.php"};
    //String[] Titles = {"Java Arrays","Paragraph Generator"};
    //String[] Paragraphs = {"She never liked cleaning the sink. It was beyond her comprehension how it got so dirty so quickly. It seemed that she was forced to clean it every other day. Even when she was extra careful to keep things clean and orderly, it still ended up looking like a mess in a couple of days. What she didn't know was there was a tiny creature living in it that didn't like things neat.", "The amber droplet hung from the branch, reaching fullness and ready to drop. It waited. While many of the other droplets were satisfied to form as big as they could and release, this droplet had other plans. It wanted to be part of history. It wanted to be remembered long after all the other droplets had dissolved into history. So it waited for the perfect specimen to fly by to trap and capture that it hoped would eventually be discovered hundreds of years in the future."};

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws  ServletException,IOException {
        response.setContentType("text/html");
        String searchInput = request.getParameter("search-text");
        //String[] searchWord = searchInput.split(" ");
        QueryProcess myQuery = new QueryProcess();
        myQuery.Query(searchInput);

        //String resultsPage = FillResultsPage("This is a tomcat test!");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet StudentServlet</title>");
        out.println("</head>");
        out.println("<body>");
        ArrayList<String> URLs = new ArrayList<String>();
        URLs = myQuery.urls;
        //URLs.add("https://www.w3schools.com/java/java_arrays.asp");
        //URLs.add("https://randomwordgenerator.com/paragraph.php");
        //URLs.add("https://www.w3schools.com/");
        //URLs.add("https://randomwordgenerator.com/");
        request.setAttribute("data",URLs);
        ArrayList<String> Titles = new ArrayList<String>();
        //Titles.add("w3schools");
        //Titles.add("randomwordgenerator");
        //Titles.add("w3schoolsfheufheuk");
        //Titles.add("randomwordgeneratorvdetuedgwucjwui");
        Titles = myQuery.titles;
        request.setAttribute("data2",Titles);
        ArrayList<String> Paragraphs = new ArrayList<String>();
        //Paragraphs.add("She didn't understand how changed worked. When she looked at today compared to yesterday, there was nothing that she could see that was different. Yet, when she looked at today compared to last year, she couldn't see how anything was ever the same.");
        //Paragraphs.add("It was difficult for him to admit he was wrong. He had been so certain that he was correct and the deeply held belief could never be shaken. Yet the proof that he had been incorrect stood right before his eyes. \"See daddy, I told you that they are real!\" his daughter excitedly proclaimed.");
        //Paragraphs.add("He was an expert but not in a discipline that anyone could fully appreciate. He knew how to hold the cone just right so that the soft server ice-cream fell into it at the precise angle to form a perfect cone each and every time. It had taken years to perfect and he could now do it without even putting any thought behind it. Nobody seemed to fully understand the beauty of this accomplishment except for the new worker who watched in amazement.");
        //Paragraphs.add("At that moment he had a thought that he'd never imagine he'd consider. \"I could just cheat,\" he thought, \"and that would solve the problem.\" He tried to move on from the thought but it was persistent. It didn't want to go away and, if he was honest with himself, he didn't want it to.");
        Paragraphs = myQuery.paragraphs;
        request.setAttribute("data3",Paragraphs);
        RequestDispatcher rd=request.getRequestDispatcher("sendthis.jsp");
        out.println("</body>");
        out.println("</html>");
        rd.forward(request, response);//method may be include or forward
        //response.getWriter().println(resultsPage);
    }

    String FillResult(String url, String title, String snippet) {
        return "<div class=\"result\">\n" +
                "            <a href=\"" + url + "\">\n" +
                "                <h2>" + title + "</h2>\n" +
                "            </a>\n" +
                "            <p>" + snippet + "</p>\n" +
                "        </div>";
    }

    String FillResultsPage(String allResults) {

        //String[] URLs = {"https://www.w3schools.com/java/java_arrays.asp", "https://randomwordgenerator.com/paragraph.php"};
        //String[] Titles = {"Java Arrays","Paragraph Generator"};
        //String[] Paragraphs = {"She never liked cleaning the sink. It was beyond her comprehension how it got so dirty so quickly. It seemed that she was forced to clean it every other day. Even when she was extra careful to keep things clean and orderly, it still ended up looking like a mess in a couple of days. What she didn't know was there was a tiny creature living in it that didn't like things neat.", "The amber droplet hung from the branch, reaching fullness and ready to drop. It waited. While many of the other droplets were satisfied to form as big as they could and release, this droplet had other plans. It wanted to be part of history. It wanted to be remembered long after all the other droplets had dissolved into history. So it waited for the perfect specimen to fly by to trap and capture that it hoped would eventually be discovered hundreds of years in the future."};
        return "<%@ page contentType=\"text/html;charset=UTF-8\" language=\"java\" %>\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <link rel=\"stylesheet\" href=\"Results.css\">\n" +
                "    <script src=\"https://kit.fontawesome.com/a076d05399.js\"></script>\n" +
                "    <title>Search Results</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <div class=\"search-bar\">\n" +
                "        <form method=\"get\" id=\"search-form\">\n" +
                "            <div class=\"logo-holder\">\n" +
                "                <a href=\"index.jsp\">\n" +
                "                    <img src=\"Logo.png\" class=\"logo\" alt=\"Sugoi Logo\">\n" +
                "                </a>\n" +
                "            </div>\n" +
                "            <div class=\"bar-holder\">\n" +
                "                <input name=\"search-text\" type=\"text\" class=\"search-bar-input\" placeholder=\"Type to Search..\">\n" +
                "                <div class=\"suggestions-com\">\n" +
                "                </div>\n" +
                "                <a href=\"Results.html\">\n" +
                "                    <div class=\"search-icon\">\n" +
                "                        <i class=\"fas fa-search\"></i>\n" +
                "                    </div>\n" +
                "                </a>\n" +
                "                <div class=\"search-icon-2\">\n" +
                "                    <button type=\"button\">\n" +
                "                        <i class=\"fas fa-microphone\"></i>\n" +
                "                    </button>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </form>\n" +
                "    </div>\n" +
                "\n" +
                "    <div class=\"search-results\">\n" +
                "\n" +
                //allResults +
                //"Hello From IntelliJ xD"+
                "    </div>\n" +
                "\n" +
                "    <div class=\"pagination\">\n" +
                "        <ul>\n" +
                "        </ul>\n" +
                "    </div>\n" +
                "\n" +
                "    <!-- <div class=\"footer\">\n" +
                "        <div class=\"footnote\">\n" +
                "            <p>Made by the Sugoi Search Engine Team with <i class=\"fas fa-heart\"></i>. Made for educational purposes. <i\n" +
                "                    class=\"fas fa-copyright\"></i></p>\n" +
                "        </div>\n" +
                "    </div> -->\n" +
                "\n" +
                "<script language='javascript'>\n" +
                " let Urls = {};\n"+
                " let Titles = {};\n"+
                " let Paragraphs = {};\n"+
                "<%=" +
                "  // this is Java, run when the page is generated\n" +
                "  String[] urls = (String[])session.getAttribute(\"URLs\"); \n" +
                "  String[] titles = (String[])session.getAttribute(\"Titles\"); \n" +
                "  String[] para = (String[])session.getAttribute(\"Paragraphs\"); \n" +
                "  for (i = 0; i < urls.length; ++i)\n" +
                "  {\n" +
                "%>\n" +
                "    // this is Javascript, built when the page is generated\n" +
                "    // and run when the page us displayed by the browser\n" +
                "    Urls[\"<%= i %>\"] = \"<%= urls[i] %>\"\n" +
                "    Titles[\"<%= i %>\"] = \"<%= titles[i] %>\"\n" +
                "    Paragraphs[\"<%= i %>\"] = \"<%= para[i] %>\"\n" +
                "<%\n" +
                "  }\n" +
                "%>\n" +
                "</script>"+
                "    <script src=\"Main.js\"></script>\n" +
                "    <script src=\"Results.js\"></script>\n" +
                "</body>\n" +
                "\n" +
                "</html>";
    }
}