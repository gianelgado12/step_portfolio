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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.ArrayList;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Setting up necessary data structures.
    TreeSet<TimeRange> conflictEvents = new TreeSet<>(TimeRange.ORDER_BY_START);
    Collection<String> attendees = request.getAttendees();
    Collection<TimeRange> availableTimes = new TreeSet<>(TimeRange.ORDER_BY_START);
    
    int duration = (int) request.getDuration();  
    
    // If duration of request is too long return an empty list.
    if(duration > TimeRange.END_OF_DAY) {
      return new ArrayList<TimeRange>();
    }
    
    // Filtering out events with no conflicting attendees.
    for(Event currentEvent:events) {
      Collection<String> eventAttendees = currentEvent.getAttendees();
      for(String currentAttendee : attendees) {
        if(eventAttendees.contains(currentAttendee)) {
          conflictEvents.add(currentEvent.getWhen());
          break;
        }
      }
    }
    
    // If no events conflict then the whole day is free.
    if(conflictEvents.isEmpty()){
      availableTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true));
      return new ArrayList<TimeRange>(availableTimes);
    }
    
    // Variables to keep track of start and end of available and unavailable time blocks.
    int unavailableStart = conflictEvents.first().start();
    int unavailableEnd = conflictEvents.first().end();
    int availableStart = TimeRange.START_OF_DAY;
    
    // Variable to hold previous conflicting TimeRange.
    TimeRange previousConflict = null;

    for(TimeRange currentConflict : conflictEvents) {
      if(previousConflict != null) {

        // Extend unavailable time block for overlapping time conflicts
        // if necessary. Otherwise, add TimeRange available before new
        // unavailable block if it is large enough to accomodate meeting.
        if(currentConflict.overlaps(previousConflict)) {
          if(currentConflict.end() > previousConflict.end()) {
            unavailableEnd = currentConflict.end();
          }
        } else {
          TimeRange availableBlock = TimeRange.fromStartEnd(availableStart, unavailableStart, false);
          if(availableBlock.duration() >= duration) {
            availableTimes.add(availableBlock);
          }
          availableStart = unavailableEnd;
          unavailableStart = currentConflict.start();
          unavailableEnd = currentConflict.end();
        }
      }

      previousConflict = currentConflict;
    }
    
    // Add last two possible available time blocks if they are large enough. 
    TimeRange availableBlock = TimeRange.fromStartEnd(availableStart, unavailableStart, false);
    if(availableBlock.duration() >= duration) {
      availableTimes.add(availableBlock);
    }
    availableStart = unavailableEnd;

    TimeRange lastBlock = TimeRange.fromStartEnd(availableStart, TimeRange.END_OF_DAY, true);
    if(lastBlock.duration() >= duration) {
      availableTimes.add(lastBlock);
    }
    
    return new ArrayList<TimeRange>(availableTimes);
  }
}

