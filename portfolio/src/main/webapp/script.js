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

/**
 * Adds comments to the page
 */
function getComments(){
  fetch('/data').then(response => response.json()).then((comments) => {
    const commentsList = document.getElementById('comments_list');
    commentsList.innerHTML = '';
    for(i = 0; i < comments.length; i++){
      comment = comments[i];
      commentsList.appendChild(createCommentElem(comment));
    }
  });
}

/**
 *Creates an HTML comment element
 */
function createCommentElem(comment){
  //
  const liElement = document.createElement('li');
  liElement.classList.add("comment");
  
  //Creating comment content element.
  const contentElem = document.createElement('p');
  contentElem.innerText = comment.content;
  
  //Creating comment header.
  const nameElem = document.createElement('b');
  nameElem.innerText = comment.userName + ' on ' + comment.uploadDate;
 
  const deleteButton = document.createElement('button');
  deleteButton.innerText = 'Delete'
  deleteButton.addEventListener('click', () => {
    // Removing comment from database.
    deleteComment(comment);

    // Removing comment from the DOM.
    liElement.remove();
  });

  liElement.appendChild(nameElem);
  liElement.appendChild(contentElem);
  liElement.appendChild(deleteButton);

  return liElement;
}


function deleteComment(comment){
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-data', {method: 'POST', body: params});
}

function deleteAllComments(){
  fetch('/delete-all', {method: 'POST'})
  getComments();
}


