ThisBuild / scalaVersion := "3.1.0"
ThisBuild / organization := "com.github.esgott"

lazy val root = (project in file("."))
  .aggregate(
    `parser-practice-cli`,
    `parser-practice-sql`
  )

lazy val `parser-practice-cli` = (project in file("cli"))
  .dependsOn(`parser-practice-sql`)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.13"
    )
  )

lazy val `parser-practice-sql` = (project in file("sql"))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-parse" % "0.3.6",
      "org.scalameta" %% "munit" % "0.7.29" % Test
    )
  )
