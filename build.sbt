name := "BattleshipEvolution"

version := "0.1"

scalaVersion := "2.13.16"

enablePlugins(JavaAppPackaging)

Compile / mainClass := Some("server.ServerWebSockets")

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"          % "3.5.7",
      "co.fs2"        %% "fs2-core"             % "3.11.0",
      "org.http4s"    %% "http4s-ember-server"  % "0.23.30",
      "org.http4s"    %% "http4s-dsl"           % "0.23.30",
      "org.http4s"    %% "http4s-circe"         % "0.23.25",
      "io.circe"      %% "circe-core"           % "0.14.10",
      "io.circe"      %% "circe-generic"        % "0.14.10",
      "io.circe"      %% "circe-generic-extras" % "0.14.4",
      "io.circe"      %% "circe-parser"         % "0.14.10",
      "ch.qos.logback" % "logback-classic"      % "1.5.16"
    )
  )
