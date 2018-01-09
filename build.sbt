val akkaVersion = "2.5.8"
val projectName = "akka-sample-cluster-scala"

val project = Project(id = projectName, base = file("."))
  .settings(
    name := projectName,
    version := "1.0",
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
  )

