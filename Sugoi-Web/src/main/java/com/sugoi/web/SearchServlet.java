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
        qProcess.Query(searchWord.length > 0? searchWord[0] : null);

        response.getWriter().println("GG");
    }
}