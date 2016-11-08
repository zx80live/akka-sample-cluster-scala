akka-sample-cluster-scala
==============================================================

Version 1.0

Requirements
-----------------------------
- Scala: 2.11.8
- JRE:   8

Usage
-----------------------------

**1**) Clone repo:

`git clone https://github.com/zx80live/akka-sample-cluster-scala`

**2**) Change directory:

`cd ./akka-sample-cluster-scala`

**3**) Set file permissions:

`chmod 774 ./bin/*`

**4**) Run cluster admin (only `2551` port):

`./bin/activator "runMain sample.cluster.messenger.AdminFrontend 2551"`

The following commands available in StdIn console (where admin was started) at any time. 
The caret of input is not shown. Just input text any time:

 * `nodes`     - list of nodes
    
 * `stat`      - node statistics
    
 * `add`       - add node in current JVM
    
 * `del N`     - delete particular node by number, 
                 example: `del 1 3 5`
                          
 * `del addr`  - delete node by full unique actor address, example:
                 
                 del akka.tcp://ClusterSystem@127.0.0.1:50936/user/worker
                          
 * `del all`   - delete all nodes

 * `int P T`   - set messages period in ms (P) and timeout in ms (T) for all nodes
                 example: `int 10 100`
                          
 * `eval`      - start evaluator of cluster performance
    
 * `stopEval`  - stop evaluator
                         
 * `exit`


**5**) Run cluster node on another JVM with arbitrary port:

`./bin/activator "runMain sample.cluster.messenger.NodeWorker 3001"`

Screenshots:
------------

Admin frontend console (`./bin/activator "runMain sample.cluster.messenger.AdminFrontend 2551"`):
![picture alt](https://raw.githubusercontent.com/zx80live/zx80live.github.io/master/img/s1.png "Admin frontend console")
Evaluating process in admin frontend console (after `eval` command). As shown - the predicted cluster performance is 1379.0 with cluster configuration (nodes = 20, timeout = 100ms):
![picture alt](https://raw.githubusercontent.com/zx80live/zx80live.github.io/master/img/s2.png "Evaluating process in admin frontend console")
Node actor console which was run on another JVM (`./bin/activator "runMain sample.cluster.messenger.NodeWorker 3001"`):
![picture alt](https://raw.githubusercontent.com/zx80live/zx80live.github.io/master/img/s3.png "Node actor console")

Actors:
-------
 * `sample.cluster.messenger.ClusterNode` - abstract class with base functional for working with cluster.
 * `sample.cluster.messenger.NodeWorker` - messenger actor which sends the messages to another nodes.
 * `sample.cluster.messenger.AdminFrontend` - frontend with StdIn interface.
 * `sample.cluster.messenger.Evaluator` - evaluate cluster performance.
 * `sample.cluster.messenger.StatisticAggregator` - aggregate performance statistic from cluster.
