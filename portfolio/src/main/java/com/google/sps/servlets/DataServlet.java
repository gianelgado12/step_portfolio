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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  /**
   * Takes in a query object and places its content in a json formatted string
   * and returns it.
   */
  private String getJson(Query commentQuery){
    String returnStr = "{";
    int count = 0;

    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepCommentQuery = dataStore.prepare(commentQuery);
    
    for(Entity commentEntity : prepCommentQuery.asIterable()){
      returnStr += "\"comment"+ Integer.toString(count)+"\": " + "\"" + 
        commentEntity.getProperty("content") + "\", ";
      count++;
    }

    //Lobbing off extra characters from the end of string if necessary
    if(returnStr.length() > 1){
      returnStr = returnStr.substring(0, returnStr.length() - 2);
    }

    return returnStr + "}";
  }

  @Override
  /**
   * Updating database with new comment submission and reloading page
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("text-input");

    //Ensuring comment isn't empty and returns message if it is
    if (newComment.replaceAll("\\s", "").equals("")){
      response.getWriter().println("Enter Text");
      return;
    }

    long timeStamp = System.currentTimeMillis();
    
    //Creating comment entity
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", newComment);
    commentEntity.setProperty("timestamp", timeStamp);

    //Store comment entity in datastore
    DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
    dataStore.put(commentEntity);

    response.sendRedirect("/index.html");
  }    

  @Override
  /**
   * Getting comments from database, sorting them by time, and writing them to the /data
   * page in json format   
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    Query commentQuery = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    String json = getJson(commentQuery);
    response.getWriter().println(json);
  }

}
