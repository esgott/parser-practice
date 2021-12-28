package com.github.esgott.parser.cli

import com.github.esgott.parser.cli.Db.DbError
import com.github.esgott.parser.cli.Db.DbError.*
import com.github.esgott.parser.sql.model.*
import com.github.esgott.parser.sql.model.SqlCommand.*
import com.github.esgott.parser.sql.model.SqlTransaction.*
import zio.*
import zio.stm.{STM, TMap}

trait Db {
  def transact(transaction: SqlTransaction): IO[DbError, Unit]
}

object Db {

  enum DbError {
    case TableNotFound(table: TableName)
  }

  def transact(transaction: SqlTransaction): ZIO[Has[Db], DbError, Unit] =
    ZIO.serviceWith[Db](_.transact(transaction))

}

case class DbLive(tMap: TMap[TableName, List[Row]]) extends Db {

  override def transact(transaction: SqlTransaction): IO[DbError, Unit] =
    transactSTM(transaction).commit

  private def transactSTM(transaction: SqlTransaction): STM[DbError, Unit] =
    transaction match {
      case SingleTransaction(command) => runCommand(command)
      case MultiTransaction(commands) => STM.foreach(commands)(runCommand).as(())
    }

  private def runCommand(command: SqlCommand): STM[DbError, Unit] =
    command match {
      case command: CreateTable => createTable(command)
      case command: InsertInto  => insertInto(command)
    }

  private def createTable(create: CreateTable): STM[DbError, Unit] =
    tMap.putIfAbsent(create.name, List.empty)

  private def insertInto(insert: InsertInto): STM[DbError, Unit] =
    for
      maybeTable <- tMap.get(insert.name)
      table      <- STM.fromEither(maybeTable.toRight(TableNotFound(insert.name)))
      _          <- tMap.put(insert.name, insert.row :: table)
    yield ()

}

object DbLive {

  val layer: /*ULayer[Has[Db]]*/ ZLayer[Any, Nothing, Has[Db]] =
    ZLayer.fromEffect {
      for
        tMap <- TMap.empty[TableName, List[Row]].commit
      yield DbLive(tMap)
    }

}