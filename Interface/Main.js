let suggestions = [];

function readsuggestions()  
{  
     var txtFile = new XMLHttpRequest();  
     txtFile.open("GET", "http://localhost:5500/Interface/Suggestions.txt", true);  
     txtFile.onreadystatechange = function()   
     {  
          if (txtFile.readyState === 4)   
          {  
               // Makes sure the document is ready to parse.  
               if (txtFile.status === 200)   
               {  
                    // Makes sure it's found the file.  
                    //document.getElementById("div").innerHTML = txtFile.responseText;  
                    //console.log(txtFile.responseText);
                    suggestions = txtFile.responseText.split('\n');
                    //for(i=0; i<suggestions.length; i++)
                        //console.log("Suggestion #"+i+" "+suggestions[i]);
                    
               }  
          }  
     }  
     txtFile.send(null)  
}  

// getting all required elements
const searchWrapper = document.querySelector(".bar-holder");
const inputBox = searchWrapper.querySelector("input");
const suggBox = searchWrapper.querySelector(".suggestions-com");
const icon = searchWrapper.querySelector(".search-icon");
let linkTag = searchWrapper.querySelector("a");
let webLink;

// if user press any key and release
inputBox.onkeyup = (e)=>{
    let userData = e.target.value; //user entered data
    //console.log(userData);
    let emptyArray = [];
    if(userData){
        emptyArray = suggestions.filter((data)=>{
            //filtering array value and user characters to lowercase and return only those words which are start with user enetered chars
            return data.toLocaleLowerCase().startsWith(userData.toLocaleLowerCase()); 
        });
        emptyArray = emptyArray.map((data)=>{
            // passing return data inside li tag
            return data = '<li>'+ data +'</li>';
        });
        //console.log(emptyArray);
        searchWrapper.classList.add("active"); //show autocomplete box
        showSuggestions(emptyArray);
        let allList = suggBox.querySelectorAll("li");
        for (let i = 0; i < allList.length; i++) {
            //adding onclick attribute in all li tag
            allList[i].setAttribute("onclick", "select(this)");
        }
    }else{
        searchWrapper.classList.remove("active"); //hide autocomplete box
    }
}

function select(element){
    let selectData = element.textContent;
    inputBox.value = selectData;
    searchWrapper.classList.remove("active");
}

function showSuggestions(list){
    let listData;
    if(!list.length){
        userValue = inputBox.value;
        listData = '<li>'+ userValue +'</li>';
    }else{
        listData = list.join('');
    }
    suggBox.innerHTML = listData;
}

const searchForm = document.querySelector("#search-form");
const info = document.querySelector(".info");

// The speech recognition interface lives on the browser’s window object
const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition; // if none exists -> undefined

if(SpeechRecognition) {
  console.log("Your Browser supports speech Recognition");
  
  const recognition = new SpeechRecognition();
  recognition.continuous = true;
   //recognition.lang = "ar-EG"; //If we want an Arabic recognition instead.//Very Inacurate though
   //recognition.lang = "ja";// The Japanese one on the other hand is pretty spot on --> I tried saying this long name (乙女ゲームの破滅フラグしかない悪役令嬢に転生してしまった) and it caught it all correctly. :O
  inputBox.style.paddingRight = "50px";

  const micBtn = searchForm.querySelector("button");
  const micIcon = micBtn.firstElementChild;

  micBtn.addEventListener("click", micBtnClick);
  function micBtnClick() {
    if(micIcon.classList.contains("fa-microphone")) { // Start Voice Recognition
      recognition.start(); // First time you have to allow access to mic!
    }
    else {
      recognition.stop();
    }
  }

  recognition.addEventListener("start", startSpeechRecognition); // <=> recognition.onstart = function() {...}
  function startSpeechRecognition() {
    micIcon.classList.remove("fa-microphone");
    micIcon.classList.add("fa-microphone-slash");
    inputBox.focus();
    console.log("Voice activated, SPEAK");
  }

  recognition.addEventListener("end", endSpeechRecognition); // <=> recognition.onend = function() {...}
  function endSpeechRecognition() {
    micIcon.classList.remove("fa-microphone-slash");
    micIcon.classList.add("fa-microphone");
    inputBox.focus();
    console.log("Speech recognition service disconnected");
  }

  recognition.addEventListener("result", resultOfSpeechRecognition); // <=> recognition.onresult = function(event) {...} - Fires when you stop talking
  function resultOfSpeechRecognition(event) {
    const current = event.resultIndex;
    const transcript = event.results[current][0].transcript;
    
    if(transcript.toLowerCase().trim()==="stop recording") {
      recognition.stop();
    }
    else if(!inputBox.value) {
      inputBox.value = transcript;
    }
    else {
      if(transcript.toLowerCase().trim()==="go") {
        searchForm.submit();
      }
      else {
        inputBox.value = transcript;
      }
    }
  }  
}
else {
  console.log("Your Browser does not support speech Recognition");
  console.error("Your Browser does not support Speech Recognition");
}

////// TO DO: 
//  1. Add the searched terms to the Suggestions.txt.
//  2. Finish the HTML and CSS for the results page.
//  3. Paging for the results page.
//  4. 
