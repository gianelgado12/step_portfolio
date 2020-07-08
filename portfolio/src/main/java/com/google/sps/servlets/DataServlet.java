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

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that retrieves comments from a datastore
 * and allows users to post comments.
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  /**
   * Takes in a query object and places its content in a json formatted string
   * and returns it.
   */
  private String getJson(Query commentQuery, int maxComm, String currentUser){
    List<Comment> commentList = new ArrayList<>();

    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    Iterator<Entity> commentIterator = dataStore.prepare(commentQuery).asIterator();
    
    //Counter to limit number of comments displayed to the desired amount.
    int commentCounter = 0; 
    
    while(commentIterator.hasNext() && commentCounter < maxComm){
      Entity commentEntity = commentIterator.next();
      String userEmail = (String)commentEntity.getProperty("email");
      String content = (String)commentEntity.getProperty("content");
      long timestamp = (long)commentEntity.getProperty("timestamp");
      long id = commentEntity.getKey().getId();
      commentList.add(new Comment(id, userEmail, timestamp, content, currentUser));
      commentCounter++;
    }

    Gson gson = new Gson();

    return gson.toJson(commentList);
  }

  /**
   * Updating database with new comment submission and reloading page.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("comment-input");
    
    UserService userService = UserServiceFactory.getUserService();
    if(!userService.isUserLoggedIn()){
      response.sendRedirect("/login");
    }
    
    // Ensuring comment or name isn't empty and returns message if it is.
    if (newComment.replaceAll("\\s", "").equals("")){
      response.getWriter().println("Enter Text");
      return;
    }

    long timeStamp = System.currentTimeMillis();
    
    // Creating comment entity.
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("email", userService.getCurrentUser().getEmail());
    commentEntity.setProperty("content", newComment);
    commentEntity.setProperty("timestamp", timeStamp);

    // Store comment entity in datastore.
    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    dataStore.put(commentEntity);

    response.sendRedirect("/index.html");
  }    

  /**
   * Getting comments from database, sorting them by time, and writing them to the /data
   * page in json format.   
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Getting user email
    UserService userService = UserServiceFactory.getUserService();
    String currentUser = "";
    if(userService.isUserLoggedIn()){
      currentUser = userService.getCurrentUser().getEmail();
    }

    // Parsing max amount of comments to be retrieved.
    int maxComm;
    try{
      maxComm = Integer.parseInt(request.getParameter("maxComments"));
    }catch(NumberFormatException e){
      System.err.println("Could not parse maximum number of comments");
      return;
    }
    
    //Retrieving comments and returning them as a JSON string.
    response.setContentType("application/json;");
    Query commentQuery = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    String json = getJson(commentQuery, maxComm, currentUser);
    response.getWriter().println(json);
  }

}
