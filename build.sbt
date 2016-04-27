name := "RedisAkkaBased"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
libraryDependencies ++= Seq(
  "com.github.etaty" %% "rediscala" % "1.6.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.typesafe.akka" %% "akka-stream" % "2.4.4"
)

