akka-sample-cluster-scala
==============================================================

Requirements
-----------------------------
- Scala: 2.11.8
- JRE:   8


Usage
-----------------------------

1) Run cluster admin (only `2551` port):

`./bin/activator "runMain sample.cluster.messenger.AdminFrontend 2551"`

or

`./bin/admin`

The following commands available in stdin console:

    > nodes             - list of nodes
    > stat              - node statistics
    > add               - add node in current JVM
    > del all           - delete all nodes
    > del N             - delete particular nodes, example: del 1 3 5
    > del addr          - delete node by full unique actor address
    > exit


2) Run cluster node on another JVM with arbitrary port:

`./bin/activator "runMain sample.cluster.messenger.NodeWorker 3001"`

or

`./bin/node`