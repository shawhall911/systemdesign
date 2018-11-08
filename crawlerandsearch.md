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
    
