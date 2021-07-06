<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="Main.css">
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
    <title>Sugoi Search Home</title>
</head>

<body onload="readsuggestions();">
<div class="search-bar">
    <div class="logo-holder">
        <a href="index.jsp">
            <img src="Logo.png" class="logo" alt="Sugoi Logo">
        </a>
    </div>
    <br>
    <form action="Search" method="GET" id="search-form">
        <div class="bar-holder">
            <input name="search-text" type="text" class="search-bar-input" placeholder="Type to Search..">
            <div class="suggestions-com">
            </div>
            <a>
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

<div class="footer">
    <div class="footnote">
        <p>Made by the Sugoi Search Engine Team with <i class="fas fa-heart"></i>. Made for educational
            purposes. <i class="fas fa-copyright"></i></p>
    </div>
    <div class="extra-padding"></div>
</div>
<script src="Main.js"></script>
</body>
</html>