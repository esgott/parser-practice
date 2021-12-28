package com.github.esgott.parser.cli

import com.github.esgott.parser.sql.model.*
import zio.*
import zio.console.Console

object Main extends App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    val x = DbLive.layer ++ CliLive.layer
    Cli.prompt.forever.exitCode
      .provideSomeLayer[Has[Db] with Has[Console.Service]](CliLive.layer)
      .provideSomeLayer[ZEnv](DbLive.layer)

}