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

package com.google.sps.data;

import java.util.Date;

// A user-submitted comment
public final class Comment{
  private final String userEmail;
  private final long timestamp;
  private final String content;
  private final Date uploadDate;
  private final long id;
  private final String currentUserEmail;

  public Comment(long id, String userEmail, long timestamp, String content, String currentUserEmail){
    this.userEmail = userEmail;
    this.timestamp = timestamp;
    this.content = content;
    this.uploadDate = new Date(timestamp);
    this.id = id;
    this.currentUserEmail = currentUserEmail;
  }

  public String getUserEmail(){
    return this.userEmail;
  }

  public long getTimestamp(){
    return this.timestamp;
  }

  public String getContent(){
    return this.content;
  }

  public Date getUploadDate(){
    return this.uploadDate;
  }
  
  public long getId(){
    return this.id;
  }

  public String getCurrentUserEmail(){
    return this.currentUserEmail;
  }
} 
