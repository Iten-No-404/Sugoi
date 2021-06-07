<%--
  Created by IntelliJ IDEA.
  User: hp
  Date: 6/7/2021
  Time: 10:42 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="Results.css">
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
    <title>Search Results</title>
</head>

<body>
<div class="search-bar">
    <form method="get" id="search-form">
        <div class="logo-holder">
            <a href="index.html">
                <img src="Logo.png" class="logo" alt="Sugoi Logo">
            </a>
        </div>
        <div class="bar-holder">
            <input name="search-text" type="text" class="search-bar-input" placeholder="Type to Search..">
            <div class="suggestions-com">
            </div>
            <a href="Results.html">
                <div class="search-icon">
                    <i class="fas fa-search"></i>
                </div>
            </a>
            <div class="search-icon-2">
                <button type="button">
                    <i class="fas fa-microphone"></i>
                </button>
            </div>
        </div>
    </form>
</div>

<div class="search-results">

</div>

<div class="pagination">
    <ul>
    </ul>
</div>

<!-- <div class="footer">
    <div class="footnote">
        <p>Made by the Sugoi Search Engine Team with <i class="fas fa-heart"></i>. Made for educational purposes. <i
                class="fas fa-copyright"></i></p>
    </div>
</div> -->

<script language='javascript'>
    let Urls = {};
    let Titles = {};
    let Paragraphs = {};
    
    <%  // this is Java, run when the page is generated
      String[] urls = (String[])session.getAttribute("URLs");
      String[] titles = (String[])session.getAttribute("Titles");
      String[] para = (String[])session.getAttribute("Paragraphs");
      for (int i = 0; i < urls.length; i++)
      {
    %>
    // this is Javascript, built when the page is generated
    // and run when the page us displayed by the browser
    Urls["<%= i %>"] = "<%= urls[i] %>"
    Titles["<%= i %>"] = "<%= titles[i] %>"
    Paragraphs["<%= i %>"] = "<%= para[i] %>"
    <%
      }
    %>
</script>
<script src="https://code.jquery.com/jquery-1.10.2.js" type="text/javascript"></script>
<script src="app-ajax.js" type="text/javascript"></script>
<script src="Main.js"></script>
<script src="Results.js"></script>
</body>

</html>
