package com.github.esgott.parser.sql

object model {

  enum SqlTransaction {
    case SingleTransaction(command: SqlCommand)
    case MultiTransaction(commands: List[SqlCommand])
  }

  enum SqlCommand {
    case CreateTable(name: TableName)
    case InsertInto(name: TableName, row: Row)
  }

  type Row = Map[Column, Field]

  case class TableName(value: String) extends AnyVal
  case class Column(name: String) extends AnyVal

  enum Field {
    case BooleanField(value: Boolean)
    case IntField(value: Int)
    case StringField(value: String)
  }

}