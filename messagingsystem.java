Design a messaging service

Use cases:
1. 1-1 chat, real time, on mobile
2. message status (delivered, read, typing)
3. new message notification
3. group chat
4. file sharing
5. backup and restore messages
6. multi-device support

Basic System
mobile clients/web clients ----> server(s) --->DB of messages

clients:
* login: Login(uid, device_id)
* heartbeat to server (server maintain online users): SendHeartBeat(uid, device_id)
* see friends: GetFriendList(uid)
* send message to a friend: SendMessage(uid, to_id, message_body) return message_id
* send message to a group: SendMessage(uid, to_group_id, message_body) return message_id
* handle received message: MessageHandler(from_id, group_id, message_body)
* local DB maintaining messages (message_id, uid, to_id, group_id, status, message_body, timestamp)

Servers:
* Direcory service: maintain online users by collecting heartbeats --->DB/cache, (uid, online_since). 
    API exposed: GetUserOnlineStatus(uid); 
    High Availablity and scalability: assume 100M DAU, rouhgly 1K QPS on average; considering usage peak time, say 10K, 
    and we do heartbeat every minute randomly, 10K/60=160 request per second; one directory server can handle. Mirror it.
    
* Messager service: 
  send/receive SendMessage():
    process message request from client and either send over if recipient is online; or push to push server;
    then save the message to DB  -->uid(PK), timestamp, message_id, to_id, group_id, message_body, status
    
    String MessageHandler(String uid, String to_id, String, message){
      String message_id = GenerateMessageID();
      if(GetUserOnlineStatus == "Active")
        SendTo(uid, to_id, message);
      else 
        SendToPushServer(uid, to_id, message);
      SaveToMessageDB(uid, to_id, message, message_id);
      return message_id;
     }
    
    DAU 100M, 1K QPS, considering peak time, 5K QPS, 5 nodes load balancing. 
  retrieval GetUndeliveredMessages():
    when a user is back online, retrieve all messages not delivered to her
  restore all messages RestoreMessages(uid, timestamp)
    a client may want to retrieve all messages saved. 
    
* Fronting Server: 
    User authentication
    Provide friend list to client
* Push Server:
    Send push notification to client device (uid, to_id, message, message_id). 
    
    
Database - NoSQL
* user-friend (uid, friend_id)
* user-online (uid, online_since)
* messages(uid, timestampe, message_id, to_id, message_body)

* Fie DB (object store): store user's file transfers

* perf considerations:
  caching
  saving some heartbeat with user's SendMessage() call
  
* HA/Scalability considerations:
  replicas
  sharding: common access patttern are user_id based. use it for Partition key. 
  Use a non-crypto hashing to hash username, and each node in the DB takes a rangle of hashed values
  Thus generally all user's data are evenly distributed over nodes. 
  Pro: evenly distribution of hashed values (users). And when new DB node is added, not data shifting between nodes.
  Con: some user maybe super popular thus send/receive a lot more threads than others (chatting with 500 people). 
       But human cannot type too fast (<4 per second). Not a concern. 
       A real concern is somone who has many friends, and all of them suddenly sending messages to her, say 500. 
  That DB node will become hot. 
  Solution: Messaging server does this:
  if(to_id == "lady_gaga") {
    String prefixed_uid = AddPrefixToId(to_id);
    TrackMappedDBNodewithPrefixedID();
  }
  Thus messages are sent to multiple nodes of lady gaga. When retreiving her messages, server needs to scatter and collect.

* Push/Poll considerations
  As in twitter, the general model is push where every tweet is immediately sent to followers to refresh (as well as DB storing).
  For celebrity who has million followers, a tweet from her will put the system under pressure due to this spike. 
  So for them we do pull mode, where only when followers online then they request celebrity (that they follow) tweets.
  In messaging system, no such "broadcasting" pattern so no worry. 
  
