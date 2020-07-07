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
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Iterator;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that retrieves comments from a datastore
 * and allows users to post comments.
 */
@WebServlet("/chart-data")
public class ChartData extends HttpServlet {
  private LinkedHashMap<Double, Double> theoreticalResponse = new LinkedHashMap<>();
  private LinkedHashMap<Double, Double> actualResponse = new LinkedHashMap<>();
  private LinkedHashMap<Double, Double> residuals = new LinkedHashMap<>();

  @Override
  public void init() {
    Scanner experimentalScanner = new Scanner(getServletContext().getResourceAsStream(
        "/files/TransientResponse2.csv"));
    Scanner theoreticalScanner = new Scanner(getServletContext().getResourceAsStream(
        "/files/theoreticalFit.csv"));
    
    while (experimentalScanner.hasNextLine() && theoreticalScanner.hasNextLine()) {
      String line = experimentalScanner.nextLine();
      String[] experimentCells = line.split(",");

      Double time = Double.valueOf(experimentCells[0]);
      Double experimentAmplitude = Double.valueOf(experimentCells[1]);

      actualResponse.put(time, experimentAmplitude);
      
      line = theoreticalScanner.nextLine();
      String[] theoryCells = line.split(",");

      Double theoryAmplitude = Double.valueOf(theoryCells[1]);

      theoreticalResponse.put(time, theoryAmplitude);

      residuals.put(time, experimentAmplitude - theoryAmplitude);
    }

    experimentalScanner.close();
    theoreticalScanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    response.setContentType("application/json");
    Gson gson = new Gson();
    String theoryJson = gson.toJson(theoreticalResponse);
    String experimentalJson = gson.toJson(actualResponse);
    String residualJson = gson.toJson(residuals);
    response.getWriter().println("[" + theoryJson + "," + experimentalJson + "," + residualJson + "]");
  }
}

