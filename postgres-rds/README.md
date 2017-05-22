# Jepsen.Postgres

Jepsen is a framework to enable testing the accuracy of Consistency and Availability guarantees while undergoing
problems with network partitions of various sorts.  As such, it is most commonly used to evaluate multi-server
systems.  As such, the most commong target of such testing is various multi-server node distributed systems.
Postgres is a great relational SQL server database with full ACID guarantees.  It is a popular choice for smaller
but still decent sized websites and applications where full linear scaling isn't required.  With respect to
the CAP theorem, it is commonly thought of as a CP system since all writes are performed on a single primary
node.  Because the database is ACID compliant, the assumption is that things are always consistent.  What is
overlooked is the fact that it isn't only the server's knowledge that matters.  In a client server system, if
the client and the server don't agree on what happened, there is a problem.

But how can that occur?

A simple but readily reproducible example should suffice.  You are buying something on a website.
When you click on "Submit" on the final ordering page, you wait and you wait.  Finally the page times
out.  Thinking the transaction failed, you re-submit it again.  Later you discover that your card got 
charged twice minutes apart, and unhappiness results.  If you are lucky you notice it and call the credit
card or ecommerce retailer to fix the problem.  If you don't, you may end up getting double-charged for
your order, or even getting the same order twice.  

Well one way to look at it is that any client server interaction is a special case of a two node
cluster.  ACID consistency defines serializability and consistency guarantees with respect to the
server's response to various clients requests.  But it doesn't address the issue of the client and
the server disagreeing on what took place.  It is entirely possible that a transaction failed, but due
to a network problem, the successful confirmation got lost as a result.
In CAP theoretical terms, you encountered a Partition, and Consistency suffered.  The ugly truth of the matter
is that the basic transaction API's do nothing to address this problem, and clients don't try to fix or
address the problem.
 
<img src="https://github.com/khdegraaf/jepsen/blob/master/postgres-rds/images/failed.jpg?raw-true" />
![Failed Commit] (images/failed.jpg?raw=true "Failed Commit")
 
Let's reproduce  

But it is fixable.  If a transaction, rather than being a single distributed commit call, instead
performanced the commit in two phases, a prepare

## 
