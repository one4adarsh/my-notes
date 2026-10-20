<!-- TOC -->
* [_04_real_time_updates](#_04_real_time_updates)
  * [Problem](#problem)
  * [Solution](#solution)
    * [1. Client-Server Connection Protocols](#1-client-server-connection-protocols)
      * [Simple Polling](#simple-polling)
        * [Advantages](#advantages)
        * [Disadvantages](#disadvantages)
        * [When to use](#when-to-use)
      * [Long Polling](#long-polling)
        * [Advantages](#advantages-1)
        * [Disadvantages](#disadvantages-1)
        * [When to use](#when-to-use-1)
      * [Server-Sent Events (SSE)](#server-sent-events-sse)
        * [Advantages](#advantages-2)
        * [Disadvantages](#disadvantages-2)
        * [When to use](#when-to-use-2)
      * [WebSockets](#websockets)
        * [Advantages](#advantages-3)
        * [Disadvantages](#disadvantages-3)
        * [When to use](#when-to-use-3)
      * [WebRTC](#webrtc)
        * [Advantages](#advantages-4)
        * [Disadvantages](#disadvantages-4)
        * [When to use](#when-to-use-4)
    * [2. Server-side Push/Pull](#2-server-side-pushpull)
      * [Pulling with Simple Polling](#pulling-with-simple-polling)
        * [Advantages](#advantages-5)
        * [Disadvantages](#disadvantages-5)
        * [When to use](#when-to-use-5)
      * [Pushing via Consistent Hashes](#pushing-via-consistent-hashes)
        * [Advantages](#advantages-6)
        * [Disadvantages](#disadvantages-6)
        * [When to use](#when-to-use-6)
      * [Pushing via Pub/Sub](#pushing-via-pubsub)
        * [Advantages](#advantages-7)
        * [Disadvantages](#disadvantages-7)
        * [When to use](#when-to-use-7)
  * [Choosing the Right Approach](#choosing-the-right-approach)
  * [Common Deep Dives](#common-deep-dives)
    * [How do you maintain message ordering across distributed servers?](#how-do-you-maintain-message-ordering-across-distributed-servers)
    * [How do you handle connection failures and reconnection?](#how-do-you-handle-connection-failures-and-reconnection)
<!-- TOC -->

# _04_real_time_updates

- addresses the challenge of delivering immediate data changes from servers to clients as events occur.

## Problem

- The core challenge is establishing efficient, persistent communication channels between clients and servers.
- Standard HTTP follows a request-response model: clients ask for data, servers respond, then the connection closes.
- This works great for traditional browsing but breaks down when servers need to proactively push updates to clients.

## Solution

- The solution requires two distinct components:

1. how do we get updates from server to client?
2. how do we get updates from source to server?

### 1. Client-Server Connection Protocols

#### Simple Polling

- Client regularly poll the server for updates.
- Simple HTTP request that the client makes at a regular interval.
- The client makes a request to the server at a regular interval & the server responds with current state.

##### Advantages

- Simple to implement
- No special infrastructure required
- Stateless

##### Disadvantages

- High latency between updates
- Limited update frequency
- More bandwidth usage
- Can be resource intensive with many clients, establishing new connections, etc

##### When to use

- Baseline solution
- Unless you specifically require very-low latency, real-time updates, it's a great solution
- Appropriate when the window where you need updates is short

#### Long Polling

- The client makes a request to the server and the server holds the request open until new data is available.
- It's as if the server is just taking really long to process the request.
- Server then responds with the data, finalizes the HTTP requests, and the client immediately makes a new HTTP request.

##### Advantages

- Easy to implement
- No special infrastructure needed
- Stateless server-side

##### Disadvantages

- More HTTP overhead
- Can be resource intensive with many clients
- Makes monitoring more painful since requests can hang around for a long time

##### When to use

- Good choice when updates are infrequent and a simple solution is preferred
- If latency of a simple polling is an issue, long-polling is an obvious upgrade with minimal additional complexity

#### Server-Sent Events (SSE)

- Extension on long-polling that allows the server to send a stream of data to the client.
- SSE uses a special header `Transfer-Encoding: chunked` which tells the client that response is a series of chunks.
- Server sends a chunk of data and then keeps the request open to send more data as needed.
- SSE is perfect for scenarios where servers need to push data to clients, but clients don't need to send data back
  frequently

##### Advantages

- Simple to implement
- Built into browsers
- Automatically reconnects
- Works over HTTP
- More efficient than long-polling due to less connection initiation/teardown

##### Disadvantages

- Only supports unidirectional communication (server to client)
- Browsers limit the number of concurrent connections per domain
- Makes monitoring more painful since requests can hang around for a long time

##### When to use

- Great upgrade to long-polling because it eliminates issues around high-frequency updates while still on HTTP
- Good choice when you need real-time updates from the server to the client and don't need bidirectional communication

#### WebSockets

- Go-to choice for true bidirectional communication between client and server.
- If you have high frequency writes and reads, use WebSockets.

##### Advantages

- True bidirectional communication
- Lower latency than HTTP due to reduced overhead
- More efficient for high-frequency messages
- Wide browser support

##### Disadvantages

- Requires special infrastructure (WebSocket server)
- More complex to implement than SSE or long-polling
- Stateful connections can make load-balancing and scaling more complex

##### When to use

- High-frequency, bidirectional communication
    - `high-frequency` because you can always make additional connections for writes
        - e.g. - common pattern is to have SSE subscriptions for updates and write over HTTP POST/PUT whenever required

#### WebRTC

- Direct peer-to-peer communication between browsers, perfect for video/audio calls and some data sharing (docs).
- Clients talk to a central "signaling server" which keeps track of which peers are available together with their
  connection information.
- Once a client has the connection information for another peer, they can try to establish a direct connection without
  going through any intermediary servers.

##### Advantages

- Direct peer-to-peer communication
- Lower latency
- Reduced server costs
- Native audio/video support

##### Disadvantages

- More complex to implement than traditional client-server models
- Requires signaling server for initial connection setup
- NAT/firewall issues
- Connection setup delay

##### When to use

- Overkill for most real-time update use cases
- Great for video/audio calls, screen sharing and gaming
- Peer-to-peer file sharing
- Scenarios where reducing server load is critical

### 2. Server-side Push/Pull

- How updates finally reach clients depends a lot on how they get from the source to the server.
- There are three common ways to trigger these updates:

1. Pulling via Polling
2. Pushing via Consistent Hashes
3. Pushing via Pub/Sub

#### Pulling with Simple Polling

- With Simple Polling, we're using a pull-based model.
- Client is constantly asking the server for updates.
- Server needs to maintain the state necessary to respond to those requests.
- The most common way to do this is to have a database where we can store the updates, and from this database our
  clients can pull the updates they need when they can.

##### Advantages

- Simple to implement
- State is constrained to the server and database
- No special infrastructure needed

##### Disadvantages

- High latency between updates
- Excess DB load when updates are infrequent and polling is frequent

##### When to use

- Pull-based polling is great when responding quickly is not the main thing
- Generally speaking, if you need real-time updates this is not the best approach

#### Pushing via Consistent Hashes

- With Consistent Hashing, we're using a push-based model.
- The server is responsible for pushing updates to clients as they happen.
- With hashing, we'll identify which clients are interested in which updates and push the updates to them directly.

##### Advantages

- Easy to add/remove servers
- Predictable distribution of updates to clients
- Minimal connection disruption when scaling

##### Disadvantages

- Complex to implement
- Requires coordination service like Zookeeper
- All servers need to maintain routing information

##### When to use

- Consistent hashing is ideal when you need to maintain persistent connections (WebSocket/SSE) and your system needs to
  scale dynamically.
- It's particularly valuable when each connection requires significant server-side state that would be expensive to
  rebuild.
- e.g. Google Docs, connections are associated with specific documents that require substantial state to maintain
  collaborative editing functionality.

#### Pushing via Pub/Sub

- Pub/Sub is another push-based model.
- Single service that is responsible for collecting updates from the source and then broadcasting them to all interested
  clients.
- Clients subscribe to the updates they are interested in, and the broker ensures they receive them.

##### Advantages

- Managing load on endpoint servers is easy
- Broadcast updates to a large number of clients efficiently

##### Disadvantages

- We don't know whether subscribers are connected to the endpoint server, or when they disconnect.

##### When to use

- When you need to broadcast updates to a large number of clients

## Choosing the Right Approach

- Chat Application → WebSockets with Pub/Sub
- Live Comments → SSE with Pub/Sub
- Collaborative Document Editing → WebSockets with CRDTs
- Live Dashboards and Analytics → SSE with Pub/Sub
- Gaming and Interactive Applications → WebRTC, WebSockets

## Common Deep Dives

### How do you maintain message ordering across distributed servers?

- Partition messages by key and ensure each partition is processed in order.
- Use a combination of sequence numbers and timestamps to order messages.

### How do you handle connection failures and reconnection?

- Use heartbeats or pings to detect dead connections.
- Maintain session state on the server to allow clients to resume from where they left off.
