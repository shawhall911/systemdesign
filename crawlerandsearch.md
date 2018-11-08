1 month = 2.5 millon seconds
1 QPS = 2.5 MLN / month
40 QPS = 100 MLBN / month

Single machine multi-thread accessing the URLqueue
caveat: how to sync?

synchronized(list)
ConcurrentLinkedQueue() avaoid ths sync()
An unbounded thread-safe queue based on linked nodes. This queue orders elements FIFO (first-in-first-out). The head of the queue is that element that has been on the queue the longest time. The tail of the queue is that element that has been on the queue the shortest time. New elements are inserted at the tail of the queue, and the queue retrieval operations obtain elements at the head of the queue. A ConcurrentLinkedQueue is an appropriate choice when many threads will share access to a common collection. This queue does not permit null elements.

This implementation employs an efficient "wait-free" algorithm based on one described in Simple, Fast, and Practical Non-Blocking and Blocking Concurrent Queue Algorithms by Maged M. Michael and Michael L. Scott.


CrawlerThread()
    while(URLqueue.isEmpty()==false){
       url = URLqueue.deque();
       page = URLdownloader.get(url);
       urlList = PageProcessor.extract(page);
       urlList.filter(); //remove dups
       URLqueue.add(urlList);
    }
    
Moving on to multi-machine
A scheduler can dispatcher tasks to each crawler machine, based a DB/cache that has the ToCrawl table:

ToCrawl:
id: PK (randomly generated)
URL: varchar
from_URL: varchar

CrawledPage:
id: PK
URL: varchar
Last_access: timestamp
PathToFS: varchar

A crawler machine has a crawler process that can launch multiple crawler threads, each doing the single machine crawling scheme (but reporting back new URLs back to scheduler who will add to ToCrawl table after dup check). When the URLqueue is empty (or almost empty), the URLqueue needs to be filled with new tasks. This requires a thread on the crawler to make a request to scheduler who will return a certain number of URLs to crawl. 

Once a page is downloaded and put into GFS, then the crawler needs to notifier the scheduler of this task completion, and scheular can write a record to CrawledPage. 

Policy of choosing what pages to crawl is based on factors such as connections of those crawler machines, popularity of pages, etc. To avoid dup, CRC or some hashing needs to be applied to each crawled page so next round, they can be skipped.

Crawler needs to have bindary backoff for downloading content.

Scaling:
Two tables need to shard to scale, based on ID. 
Crawler machines may need to put in various geo regions to capture pages there
Multiple crawler nodes are needed, all accessing the same DB and cache. 
In the memcached cache: we store URL reported by crawler, this save one DB visit from scheduler. 
