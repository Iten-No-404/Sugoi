package com.sugoi.web;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "SearchServlet", value = "/Search")
public class SearchServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        String searchInput = request.getParameter("search-text");
        String[] searchWord = searchInput.split(" ");
        QueryProcess qProcess = new QueryProcess();
        qProcess.Query(searchWord.length > 0 ? searchWord[0] : null);
        StringBuilder firstTenResults = new StringBuilder();
        for (int i = 0; i < qProcess.titles.size(); i++) {
            firstTenResults.append(FillResult(qProcess.urls.get(i), qProcess.titles.get(i), qProcess.paragraphs.get(i)));
        }
        String resultsPage = FillResultsPage(firstTenResults.toString());
        response.getWriter().println(resultsPage);
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
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <link rel=\"stylesheet\" href=\"./Interface/Results.css\">\n" +
                "    <script src=\"https://kit.fontawesome.com/a076d05399.js\"></script>\n" +
                "    <title>Search Results</title>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <div class=\"search-bar\">\n" +
                "        <form method=\"get\" id=\"search-form\">\n" +
                "            <div class=\"logo-holder\">\n" +
                "                <a href=\"index.html\">\n" +
                "                    <img src=\"Logo.png\" class=\"logo\" alt=\"Sugoi Logo\">\n" +
                "                </a>\n" +
                "            </div>\n" +
                "            <div class=\"bar-holder\">\n" +
                "                <input type=\"text\" class=\"search-bar-input\" placeholder=\"Type to Search..\">\n" +
                "                <div class=\"suggestions-com\">\n" +
                "                </div>\n" +
                "                <a href=\"./Interface/Results.html\">\n" +
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
                allResults +
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
                "    <script src=\"./Interface/Main.js\"></script>\n" +
                "    <script src=\"./Interface/Results.js\"></script>\n" +
                "</body>\n" +
                "\n" +
                "</html>";
    }
}