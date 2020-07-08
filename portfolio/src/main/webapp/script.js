// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Load the Visualization API and the corechart package.
google.charts.load('current', {'packages':['corechart']});

// Set a callback to run when the Google Visualization API is loaded.
google.charts.setOnLoadCallback(drawChart);


/**
 * Fetches chart data from servlet and displays corresponding line charts. 
 */
function drawChart(){
  fetch('/chart-data').then(response => response.json()).then((transientResponse)=>{
   
    const responseData = new google.visualization.DataTable();
    const residualData = new google.visualization.DataTable();
    
    responseData.addColumn('number', 'Time (ms)');
    responseData.addColumn('number', 'Experimental Amplitude (Volts)');
    responseData.addColumn('number', 'Theoretical Amplitude (Volts)');

    residualData.addColumn('number', 'Time (ms)');
    residualData.addColumn('number', 'Residual(Volts)');

    theoryData = transientResponse[0]
    experimentalData = transientResponse[1]
    residuals = transientResponse[2]

    Object.keys(theoryData).forEach((timeStep) => {
      responseData.addRow([parseFloat(timeStep), parseFloat(experimentalData[timeStep]), parseFloat(theoryData[timeStep])]);
      residualData.addRow([parseFloat(timeStep), parseFloat(residuals[timeStep])]);
    });

    const responseOptions = {
      'title': 'Transient Response of an RLC Circuit',
      'legend': {'position':'bottom'},
      'width': 350,
      'vAxis': {'title':'Amplitude (Volts)'},
      'hAxis': {'title':'Time (ms)'}
    }

    const residualOptions = { 
      'title': 'Residuals',
      'legend': {'position':'bottom'},
      'width': 350,
      'vAxis': {'title':'Amplitude (Volts)'},
      'hAxis': {'title':'Time (ms)'}
    }

    const responseChart = new google.visualization.LineChart(document.getElementById('response-chart-container'));
    responseChart.draw(responseData, responseOptions);

    const residualChart = new google.visualization.LineChart(document.getElementById('residual-chart-container'));
    residualChart.draw(residualData, residualOptions);
  });
}  

/**
 * Adds comments and appropraite header to the page
 */
function setUpPage(){
  // Setting up login sensitive elements.
  setLogin();
  
  //Getting number of comments to display
  const commentNumSelectEl = document.getElementById('comments-dropdown');
  maxComments =  commentNumSelectEl.value;  
  
  //Retrieving and displaying specified number of comments
  fetch('/data?maxComments='+maxComments).then(response => response.json()).then((comments) => {
    const commentsList = document.getElementById('comments_list');
    commentsList.innerHTML = '';
    for(i = 0; i < comments.length; i++){
      comment = comments[i];
      commentsList.appendChild(createCommentElem(comment));
    }
  });
}


/**
 * Gets the login status of user and displays correct information in page header
 * and displays comments if user is logged in.
 */
function setLogin(){
  fetch('/loginStat').then(response => response.json()).then((login_status) => {
    const loginLink = document.getElementById('login-link');
    if(login_status.userStatus === "True"){
      // Showing comments and welcome message if user is logged in.
      document.getElementById('inner-comments-container').style.display = "block";
      loginLink.innerText = 'Welcome, ' + login_status.userEmail;

      // Adding logout link to header to let the user end their session.
      loginLink.href = "/logout"
    }else{
      // Showing user a request to log in to enable comments.
      const loginRequestEl = document.createElement('p');
      loginRequestEl.innerText = "Please log in to access comments"
      document.getElementById('comments-container').appendChild(loginRequestEl);

      // Hiding comments.
      document.getElementById('inner-comments-container').style.display = "none";

      // Adding login link to header.
      loginLink.innerText = 'Login';
      loginLink.href = "/login"
    } 
  });
}

/**
 * Creates an HTML comment element
 */
function createCommentElem(comment){
  // Creating comment list element
  const liElement = document.createElement('li');
  liElement.classList.add("comment");
  
  // Creating comment content element.
  const contentElem = document.createElement('p');
  contentElem.innerText = comment.content;
  
  // Creating comment header.
  const nameElem = document.createElement('b');
  nameElem.innerText = comment.userEmail + ' on ' + comment.uploadDate;
  
  liElement.appendChild(nameElem);
  liElement.appendChild(contentElem);
  
  if(comment.userEmail === comment.currentUserEmail){ 
    const deleteButton = document.createElement('button');
    deleteButton.innerText = 'Delete'
    deleteButton.addEventListener('click', () => {
      // Removing comment from database.
      deleteComment(comment);

      // Removing comment from the DOM.
      liElement.remove();
    });
    liElement.appendChild(deleteButton);
  }

  return liElement;
}

/**
 * Deletes a single comment from the webpage.
 */
function deleteComment(comment){
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-data', {method: 'POST', body: params});
}

/**
 * Deletes all comments from the webpage
 */
function deleteAllComments(){
  fetch('/delete-all', {method: 'POST'})
  setUpPage();
}


