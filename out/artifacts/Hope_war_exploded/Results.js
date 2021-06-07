function parseURLParams(url) {
    var queryStart = url.indexOf("?") + 1,
        queryEnd = url.indexOf("#") + 1 || url.length + 1,
        query = url.slice(queryStart, queryEnd - 1),
        pairs = query.replace(/\+/g, " ").split("&"),
        parms = {}, i, n, v, nv;

    if (query === url || query === "") return;

    for (i = 0; i < pairs.length; i++) {
        nv = pairs[i].split("=", 2);
        n = decodeURIComponent(nv[0]);
        v = decodeURIComponent(nv[1]);

        if (!parms.hasOwnProperty(n)) parms[n] = [];
        parms[n].push(nv.length === 2 ? v : null);
    }
    return parms;
}

// selecting required element
const element = document.querySelector(".pagination ul");
let totalPages = 20;
let page = 1;
let urlString = window.location.search;
let urlParams;


function loadpage() {
    urlParams = parseURLParams(urlString);
    readsuggestions();
    CreateResults();
    console.log("Query= " + urlParams['q']);
    console.log("Current Page= " + urlParams['page']);
    page = parseInt(urlParams['page'][0]);
    console.log("Current Page= " + page);
    //calling function with passing parameters and adding inside element which is ul tag
    element.innerHTML = createPagination(totalPages, page);
    var searchresultshei = document.getElementById('search-results').style.height;
    var footnote = document.getElementById('extra-padding');
    footnote.style['top'] = searchresultshei;
}

function createPagination(totalPages, page) {
    let liTag = '';
    let active;
    let beforePage = page - 1;
    let afterPage = page + 1;
    if (page > 1) { //show the next button if the page value is greater than 1
        liTag += `<li class="btn prev" onclick="movetopage(${page - 1})"><span><i class="fas fa-angle-left"></i> Prev</span></li>`;
    }

    if (page > 2) { //if page value is less than 2 then add 1 after the previous button
        liTag += `<li class="first numb" onclick="movetopage(${1})"><span>1</span></li>`;
        if (page > 3) { //if page value is greater than 3 then add this (...) after the first li or page
            liTag += `<li class="dots"><span>...</span></li>`;
        }
    }

    // how many pages or li show before the current li
    if (page == totalPages) {
        beforePage = beforePage - 2;
    } else if (page == totalPages - 1) {
        beforePage = beforePage - 1;
    }
    // how many pages or li show after the current li
    if (page == 1) {
        afterPage = afterPage + 2;
    } else if (page == 2) {
        afterPage = afterPage + 1;
    }

    for (var plength = beforePage; plength <= afterPage; plength++) {
        if (plength > totalPages) { //if plength is greater than totalPage length then continue
            continue;
        }
        if (plength == 0) { //if plength is 0 than add +1 in plength value
            plength = plength + 1;
        }
        if (page == plength) { //if page is equal to plength than assign active string in the active variable
            active = "active";
        } else { //else leave empty to the active variable
            active = "";
        }
        liTag += `<li class="numb ${active}" onclick="movetopage(${plength})"><span>${plength}</span></li>`;
    }

    if (page < totalPages - 1) { //if page value is less than totalPage value by -1 then show the last li or page
        if (page < totalPages - 2) { //if page value is less than totalPage value by -2 then add this (...) before the last li or page
            liTag += `<li class="dots"><span>...</span></li>`;
        }
        liTag += `<li class="last numb" onclick="movetopage(${totalPages})"><span>${totalPages}</span></li>`;
    }

    if (page < totalPages) { //show the next button if the page value is less than totalPage(20)
        liTag += `<li class="btn next" onclick="movetopage(${page + 1})"><span>Next <i class="fas fa-angle-right"></i></span></li>`;
    }
    element.innerHTML = liTag; //add li tag inside ul tag
    return liTag; //reurn the li tag
}

function movetopage(newpagenum) {
    var redirect = "./Results.html?q=" + urlParams['q'] + "&page=" + parseInt(newpagenum);
    icon.setAttribute("href", redirect);
    window.location.replace(redirect);
}

function CreateResults() {
    var min = min(Urls.length, 10);
    var resultselement = document.querySelector(".search-results");
    var allresults='';
    console.log(Urls);
    console.log(Titles);
    console.log(Paragraphs);
    console.log("Min = "+min);
    for (i = 0; i < min; i++)
    {
        var template = `<div class="result">` + `<a href="`+ Urls[i]+ `"><h2>`+Titles[i]+`</h2>></a>`;
        template+= `<p>`+Paragraphs[i]+`</p></div>`;
        console.log(template);
        allresults +=template;
    }
    resultselement.innerHTML= allresults;
}