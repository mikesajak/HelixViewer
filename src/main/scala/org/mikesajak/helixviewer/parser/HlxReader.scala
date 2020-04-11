package org.mikesajak.helixviewer.parser

import spray.json.JsValue

import scala.io.Source
import scala.language.implicitConversions

class HlxReader {

  import HlxReaderProtocol._
  import spray.json._

  def read(filename: String) = {
    val jsonStr = Source.fromFile(filename).mkString
    val jsonAst = jsonStr.parseJson
//    jsonAst.asJsObject.

    val metaAst = jsonAst.asJsObject.getFields("data").head.asJsObject.getFields("meta").head
    val meta = metaAst.convertTo[HlxMetadata]

    println(meta)
    println(meta.toJson)


    val inputAst = jsonAst.asJsObject.getFields("data").head
        .asJsObject.getFields("tone").head
        .asJsObject.getFields("dsp0").head
        .asJsObject.getFields("inputA").head

    val inputA = inputAst.convertTo[HlxInputBlock]

    println(inputA)
    println(inputA.toJson)

    val outputAst = jsonAst.asJsObject.getFields("data").head
                          .asJsObject.getFields("tone").head
                          .asJsObject.getFields("dsp0").head
                          .asJsObject.getFields("outputA").head

    val outputA = outputAst.convertTo[HlxOutputBlock]

    println(outputA)
    println(outputA.toJson)

    import Implicits._

    val dsp0Ast = jsonAst/"data"/"tone"/"dsp0"
    val dsp0 = dsp0Ast.convertTo[HlxDsp]
    println(dsp0)
    println(dsp0.toJson)

  }
}


object Implicits {
  implicit class JsonValue(val value: JsValue) {
    def /(childName: String) = new JsonValue(value.asJsObject().getFields(childName).head)
  }

  implicit def stripJsonValue(jsonValue: JsonValue): JsValue = jsonValue.value
}
