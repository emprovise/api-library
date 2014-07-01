package com.emprovise.api.campfire.models;

import java.util.List;

import com.google.api.client.util.Key;
import com.emprovise.api.campfire.Room;

public class RoomList {

  // Campfire API data model: Rooms
  
  @Key public List<Room> rooms;
  
}
