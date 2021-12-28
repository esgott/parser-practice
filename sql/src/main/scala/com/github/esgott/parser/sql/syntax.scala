package com.github.esgott.parser.sql

import cats.data.NonEmptyList
import cats.parse.Parser
import cats.parse.Numbers.{digit, digits}
import cats.parse.Rfc5234.{alpha, char, lwsp}
import com.github.esgott.parser.sql.model.*
import com.github.esgott.parser.sql.model.Field.*
import com.github.esgott.parser.sql.model.SqlCommand.*
import com.github.esgott.parser.sql.model.SqlTransaction.*


object syntax {

  private val whitespace =
    Parser.charIn(" \t\r\n").rep0.void

  private val end =
    Parser.char(';').surroundedBy(whitespace)

  private def command(words: String*) =
    words.foldLeft(Parser.unit){ (p, word) =>
      p *> whitespace *> Parser.ignoreCase(word)
    }

  private val beginTransaction =
    command("BEGIN").with1 <* end

  private val commitTransaction =
    command("COMMIT").with1 <* end

  private val name =
    (alpha | digit | Parser.charIn("_")).rep.string

  private val tableName =
    whitespace.with1 *> name.map(TableName.apply)

  private val column =
    whitespace.with1 *> name.map(Column.apply)

  private val booleanField = 
    (Parser.ignoreCase("TRUE").map(_ => true) | Parser.ignoreCase("FALSE").map(_ => false))
      .map(BooleanField.apply)

  private val intField =
    digits.map(_.toInt).map(IntField.apply)

  private val stringField =
    (alpha | digit | Parser.charIn(" -.!?")).rep.string.surroundedBy(Parser.char('\'')).map(StringField.apply)

  private val field =
    booleanField | intField | stringField.backtrack

  private def listInParens[T](parser: Parser[T]) =
    parser
      .repSep(Parser.char(',').surroundedBy(whitespace))
      .between(Parser.char('(') *> whitespace, whitespace <* Parser.char(')'))

  private val createTable: Parser[CreateTable] =
    (command("CREATE", "TABLE").with1 *> tableName <* end)
      .map(CreateTable.apply)

  private val insertInto: Parser[InsertInto] =
    ((command("INSERT", "INTO").with1 *> tableName) ~
      (whitespace.with1 *> listInParens(column)) ~
      (command("VALUES") *> whitespace *> listInParens(field) <* end)
    ).map { case ((table, columns), fields) =>
      InsertInto(table, (columns zip fields).toList.toMap)
    }
      

  private val sqlCommand: Parser[SqlCommand] =
    createTable | insertInto

  private val singleTransaction: Parser[SingleTransaction] =
    sqlCommand.map(SingleTransaction.apply)

  private val multiTransaction: Parser[MultiTransaction] =
    (beginTransaction *> sqlCommand.rep0 <* commitTransaction).map(MultiTransaction.apply)

  val parser: Parser[SqlTransaction] = singleTransaction | multiTransaction

}