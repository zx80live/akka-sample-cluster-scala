akka-sample-cluster-scala
==============================================================

Requirements
-----------------------------
- Scala: 2.11.8
- JRE:   8

Usage
-----------------------------

1) Clone repo:

`git clone https://github.com/zx80live/akka-sample-cluster-scala`

2) Change directory:

`cd ./akka-sample-cluster-scala`

3) Set file permissions:

`chmod 774 ./bin/*`

4) Run cluster admin (only `2551` port):

`./bin/activator "runMain sample.cluster.messenger.AdminFrontend 2551"`

or

`./bin/admin`

The following commands available in StdIn console (where admin was started) at any time:

 * `nodes`             - list of nodes
    
 * `stat`              - node statistics
    
 * `add`               - add node in current JVM
    
 * `del N`             - delete particular node by number, 
                          example: `del 1 3 5`
                          
 * `del addr`          - delete node by full unique actor address, 
                          example: `del akka.tcp://ClusterSystem@127.0.0.1:50936/user/worker`
                          
 * `del all`           - delete all nodes

 * `int P T`           - set messages period in ms (P) and timeout in ms (T) for all nodes
                          example: `int 10 100`
                          
 * `eval`              - start evaluator of cluster performance
    
 * `stopEval`          - stop evaluator
                         
 * `exit`


5) Run cluster node on another JVM with arbitrary port:

`./bin/activator "runMain sample.cluster.messenger.NodeWorker 3001"`

or

`./bin/node`