package com.emprovise.api.campfire;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.emprovise.api.campfire.models.Message;
import com.emprovise.api.campfire.models.MessageList;
import com.emprovise.api.campfire.models.SingleMessage;
import com.emprovise.api.campfire.models.SingleRoom;
import com.emprovise.api.campfire.models.SingleUpload;
import com.emprovise.api.campfire.models.SingleUser;
import com.emprovise.api.campfire.models.Upload;
import com.emprovise.api.campfire.models.UploadList;
import com.emprovise.api.campfire.models.User;

/**
 * The {@link Room} class represents an actual room in the Campfire lobby. A user can
 * join a room, send messages, upload files, and even start listening to new messages
 * through a streaming interface by attaching a new {@link Listener}. Do not instantiate
 * a Room directly, but instead request the list of available Rooms from a {@link Campfire}
 * instance.
 * 
 * @author flintinatux
 * @see Campfire
 * @see Listener
 * @see Message
 * @see Upload
 * @see User
 */
public class Room extends GenericJson {
  
  // Campfire API data model: Room
  
  @Key public long id;
  @Key public String name;
  @Key private String topic;
  @Key public long membership_limit;
  @Key public Boolean full;
  @Key public Boolean open_to_guests;
  @Key public String active_token_value;
  @Key public String updated_at;
  @Key public String created_at;
  @Key public Boolean locked;
  @Key private List<User> users;
  
  // instance fields
  
  private Connection connection;
  private Listener listener;
  private boolean loaded;
  
  // constructors
  
  public Room() { }
  
  protected Room(String name, String topic) {
    this.name = name;
    this.topic = topic;
  }
  
  // public methods
  
  public List<Upload> files() throws IOException {
    return get("uploads").parseAs(UploadList.class).uploads();
  }
  
  public boolean guestAccessEnabled() throws IOException {
    load();
    return open_to_guests;
  }
  
  public String guestInviteCode() throws IOException {
    load();
    return active_token_value;
  }
  
  public GenericUrl guestUrl() throws IOException {
    if (guestAccessEnabled()) {
      return connection.urlFor("/" + active_token_value);
    }
    return null;
  }
  
  public HttpResponse join() throws IOException {
    log(Level.INFO, "Joining room: " + name);
    return post("join", null);
  }
  
  public HttpResponse leave() throws IOException {
    stopListening();
    log(Level.INFO, "Leaving room: " + name);
    return post("leave", null);
  }
  
  public void listen(Listener listener) throws IOException {
    setListener(listener);
    new Thread(listener).start();
  }
  
  public boolean isListening() {
    return listener != null;
  }
  
  public HttpResponse lock() throws IOException {
    return post("lock", null);
  }
  
  public Message paste(String message) throws IOException {
    return sendMessage(message, "PasteMessage").parseAs(SingleMessage.class).message;
  }
  
  public Message play(String sound) throws IOException {
    return sendMessage(sound, "SoundMessage").parseAs(SingleMessage.class).message;
  }
  
  public List<Message> recent() throws IOException {
    return get("recent").parseAs(MessageList.class).messages;
  }
  
  public List<Message> recent(long sinceMessageId) throws IOException {
    String recentUrl = roomUrlFor("recent") + "?since_message_id=" + sinceMessageId;
    return connection.get(recentUrl).parseAs(MessageList.class).messages;
  }
  
  public HttpResponse rename(String name) throws IOException {
    return setName(name);
  }
  
  public List<Message> search(String term) throws IOException {
    return connection.get("/search?q=" + term + "&format=json").parseAs(MessageList.class).messages;
  }
  
  public HttpResponse setName(String name) throws IOException {
    return update(name, this.topic);
  }
  
  public HttpResponse setTopic(String topic) throws IOException {
    return update(this.name, topic);
  }
  
  public Message speak(String message) throws IOException {
    return sendMessage(message, "TextMessage").parseAs(SingleMessage.class).message;
  }
  
  public void stopListening() {
    if (isListening()) {
      log(Level.INFO, "Stopped listening to room: " + name);
      listener.stop();
      listener = null;
    }
  }
  
  public String topic() throws IOException {
    reload();
    return topic;
  }
  
  public List<Message> transcript(Calendar date) throws IOException {
    return get(transcriptPath(date)).parseAs(MessageList.class).messages;
  }
  
  public Message tweet(String url) throws IOException {
    return sendMessage(url, "TweetMessage").parseAs(SingleMessage.class).message;
  }
  
  public HttpResponse unlock() throws IOException {
    return post("unlock", null);
  }
  
  public Upload upload(File file) throws IOException {
    RawPost.Response response = connection.rawPost(roomUrlFor("uploads"), file);
    return connection.jsonFactory().fromInputStream(response.getContent(), SingleUpload.class).upload;
  }

  public User user(long id) throws IOException {
    load();
    User found = null;
    for (User user : users) {
      if(user.id == id) { found = user; }
    }
    if (found == null) {
      found = connection.get(userPath(id)).parseAs(SingleUser.class).user;
      users.add(found);
    }
    return found;
  }
  
  public List<User> users() throws IOException {
    reload();
    return users;
  }
  
  // protected methods

  protected void setConnection(Connection connection) {
    this.connection = connection;
  }
  
  protected void setListener(Listener listener) {
    listener.setConnection(connection);
    listener.setRoom(this);
    this.listener = listener;
  }
  
  // private methods
  
  private HttpResponse get(String action) throws IOException {
    return connection.get(roomUrlFor(action));
  }
  
  private void load() throws IOException {
    if (!loaded) { reload(); }
  }
  
  private void log(Level level, String message) {
    connection.log(level, message);
  }
  
  private void reload() throws IOException {
    Room room = connection.get(roomPath()).parseAs(SingleRoom.class).room;
    this.putAll(room);
    loaded = true;
  }
  
  private String roomPath() {
    return "/room/" + id + ".json";
  }
  
  private String roomUrlFor(String action) {
    return "/room/" + id + "/" + action + ".json";
  }
  
  private HttpResponse post(String action, Object object) throws IOException {
    return connection.post(roomUrlFor(action), object);
  }
  
  private HttpResponse sendMessage(String message, String type) throws IOException {
    SingleMessage newMessage = new SingleMessage();
    newMessage.message = new Message(message, type);
    return connection.post(roomUrlFor("speak"), newMessage);
  }
  
  private String transcriptPath(Calendar date) {
    return "transcript/" + date.get(Calendar.YEAR) + "/" + date.get(Calendar.MONTH) + "/" + date.get(Calendar.DAY_OF_MONTH);
  }
  
  private HttpResponse update(String name, String topic) throws IOException {
    SingleRoom updatedRoom = new SingleRoom();
    updatedRoom.room = new Room(name, topic);
    return connection.put(roomPath(), updatedRoom);
  }
  
  private String userPath(long id) {
    return "/users/" + id + ".json";
  }

}
