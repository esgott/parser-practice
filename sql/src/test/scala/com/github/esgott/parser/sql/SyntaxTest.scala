package com.github.esgott.parser.sql

import cats.syntax.either.*
import com.github.esgott.parser.sql.model.*
import com.github.esgott.parser.sql.model.Field.*
import com.github.esgott.parser.sql.model.SqlTransaction.*
import com.github.esgott.parser.sql.model.SqlCommand.*
import munit.FunSuite

class SyntaxTest extends FunSuite {

  private def parse(command: String) =
    syntax.parser.parse(command).map(_._2)

  test("empty transaction") {
    val cmd = """BEGIN;
                |COMMIT;
                |""".stripMargin
    
    assertEquals(parse(cmd), MultiTransaction(List.empty).asRight)
  }

  test("create table") {
    val cmd = "CREATE TABLE new_table;"
    
    assertEquals(parse(cmd), SingleTransaction(CreateTable(TableName("new_table"))).asRight)
  }

  test("insert into") {
    val cmd = "INSERT INTO table (c1, c2, c3) VALUES ('v', 22, TRUE);"

    val fields = Map(
      Column("c1") -> StringField("v"),
      Column("c2") -> IntField(22),
      Column("c3") -> BooleanField(true)
    )

    assertEquals(parse(cmd), SingleTransaction(InsertInto(TableName("table"), fields)).asRight)
  }

  test("non-empty transaction") {
    val cmd = """BEGIN;
                |CREATE TABLE table;
                |COMMIT;
                |""".stripMargin
    
    assertEquals(parse(cmd), MultiTransaction(List(CreateTable(TableName("table")))).asRight)
  }

}