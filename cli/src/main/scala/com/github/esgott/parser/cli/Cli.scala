package com.github.esgott.parser.cli

import cats.parse.Parser
import com.github.esgott.parser.sql.syntax.parser
import zio.*
import zio.console.Console

import java.io.IOException

trait Cli {
  def prompt: IO[IOException, Unit]
}

object Cli {

  def prompt: ZIO[Has[Cli], IOException, Unit] =
    ZIO.serviceWith[Cli](_.prompt)

}

case class CliLive(console: Console.Service, db: Db) extends Cli {

  override def prompt: IO[IOException, Unit] =
    for 
      _ <- printPrompt
      _ <- runInput
    yield ()

  private def printPrompt =
    console.putStr("> ")

  private def runInput =
    for
      userInput <- console.getStrLn
      _ <- parser.parse(userInput) match {
        case Right((_, transaction)) =>
          db.transact(transaction).catchAll(e => console.putStrLn(s"Failed to run transaction: $e"))
        case Left(error) =>
          console.putStrLn(s"Failed to parse SQL: $error")
      }
    yield ()

}

object CliLive {

  val layer: /*URLayer[Has[Console.Service] with Has[Db], Has[CliLive]]*/ ZLayer[Has[Console.Service] with Has[Db], Nothing, Has[Cli]] =
    (CliLive(_, _)).toLayer

}