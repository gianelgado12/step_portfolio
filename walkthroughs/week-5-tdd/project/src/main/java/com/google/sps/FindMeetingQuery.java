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
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Setting up necessary data and structures.
    Collection<String> attendees = request.getAttendees();
    Collection<TimeRange> availableTimes = new TreeSet<>(TimeRange.ORDER_BY_START);
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    Collection<TimeRange> optionalAvailableTimes = new TreeSet<>(TimeRange.ORDER_BY_START); 
    
    int duration = (int) request.getDuration();  
    
    // If duration of request is too long return an empty list.
    if(duration > TimeRange.END_OF_DAY) {
      return new ArrayList<TimeRange>();
    }
    
    // Filtering out events with no conflicting attendees.
    TreeSet<TimeRange> conflictEvents = getConflicts(events, attendees);
     
    // If no events conflict then the whole day is free. Otherwise find
    // available blocks of time.
    if(conflictEvents.isEmpty()){
      availableTimes.add(TimeRange.WHOLE_DAY);
    }else{
      availableTimes = getAvailableTimes(conflictEvents, duration);
    }
    
    // Getting free time intervals for optional attendees that fit mandatory attendee free time blocks. 
    for(String optionalAttendee : optionalAttendees) {
      TreeSet<TimeRange> attendeeConflicts = getConflicts(events, Arrays.asList(optionalAttendee));
      TreeSet<TimeRange> attendeeAvailableTimes = getAvailableTimes(attendeeConflicts, duration);
      optionalAvailableTimes.addAll(getLargeEnoughOverlaps(attendeeAvailableTimes, availableTimes, duration));
    }
   
    if((!optionalAttendees.isEmpty() && attendees.isEmpty()) || !optionalAvailableTimes.isEmpty()){
      return new ArrayList<TimeRange>(optionalAvailableTimes);
    }
    return new ArrayList<TimeRange>(availableTimes);
  }

  // Given two collections of TimeRanges return TimeRanges for which these two collections overlap.
  private static TreeSet<TimeRange> getLargeEnoughOverlaps(Collection<TimeRange> firstCollection, 
                                                           Collection<TimeRange> secondCollection,
                                                           int minimumDuration){
    TreeSet<TimeRange> overlaps = new TreeSet<>(TimeRange.ORDER_BY_START);
    for(TimeRange firstTimeBlock : firstCollection) {
      for(TimeRange secondTimeBlock : secondCollection) {
        if(firstTimeBlock.overlaps(secondTimeBlock)) {
          TimeRange overlapTime = getOverlap(firstTimeBlock, secondTimeBlock, false);
          if(overlapTime.duration() >= minimumDuration) {
            overlaps.add(overlapTime);
          }
        }
      }
    }
    return overlaps;
  }

  private static TimeRange getOverlap(TimeRange rangeOne, TimeRange rangeTwo, boolean inclusive){
   assert rangeOne.overlaps(rangeTwo) : "The two ranges don't overlap"; 
   int startOverlap = Math.max(rangeOne.start(), rangeTwo.start());
   int endOverlap = Math.min(rangeOne.end(), rangeTwo.end());
   if(endOverlap == TimeRange.END_OF_DAY){
     inclusive = true;
   }
   return TimeRange.fromStartEnd(startOverlap, endOverlap, inclusive);
  }
  
  // Gets all available timeblocks for the meeting throughout the day given a list of conflicts
  private static TreeSet<TimeRange> getAvailableTimes(TreeSet<TimeRange> conflicts, int meetingDuration){
    TreeSet<TimeRange> availableTimes = new TreeSet<>(TimeRange.ORDER_BY_START);
    int unavailableStart = conflicts.first().start();
    int unavailableEnd = conflicts.first().end();
    int availableStart = TimeRange.START_OF_DAY;
    
    TimeRange previousConflict = null;

    // Extend unavailable time block for overlapping time conflicts
    // if necessary. Otherwise, add TimeRange available before new
    // unavailable block if it is large enough to accomodate meeting.
    for(TimeRange currentConflict : conflicts) {
      if(previousConflict != null) {
        if(currentConflict.overlaps(previousConflict)) {
          if(currentConflict.end() > previousConflict.end()) {
            unavailableEnd = currentConflict.end();
          }
        } else {
          addIfLargeEnough(availableTimes, availableStart, unavailableStart, meetingDuration, false);
          availableStart = unavailableEnd;
          unavailableStart = currentConflict.start();
          unavailableEnd = currentConflict.end();
        }
      }

      previousConflict = currentConflict;
    }
    
    // Add last two possible available time blocks if they are large enough. 
    addIfLargeEnough(availableTimes, availableStart, unavailableStart, meetingDuration, false);
    availableStart = unavailableEnd;
    addIfLargeEnough(availableTimes, availableStart, TimeRange.END_OF_DAY, meetingDuration, true);
    
    return availableTimes;
  }

  // Adding a time block to a specfied collection if it is long enough for the meeting.
  private static void addIfLargeEnough(Collection<TimeRange> availableTimes, int start, int end, 
    int meetingDuration, boolean inclusive){
    TimeRange availableBlock = TimeRange.fromStartEnd(start, end, inclusive);
    if(availableBlock.duration() >= meetingDuration) {
      availableTimes.add(availableBlock);
    }
  }

  // Filtering out events that don't contain any confliciting attendees.
  private static TreeSet<TimeRange> getConflicts(Collection<Event> events, Collection<String> attendees){
    TreeSet<TimeRange> conflictingTimes = new TreeSet<>(TimeRange.ORDER_BY_START);
    for(Event currentEvent:events) {
      Collection<String> eventAttendees = currentEvent.getAttendees();
      for(String currentAttendee : attendees) {
        if(eventAttendees.contains(currentAttendee)) {
          conflictingTimes.add(currentEvent.getWhen());
          break;
        }
      }
    }
    return conflictingTimes;
  }

}

