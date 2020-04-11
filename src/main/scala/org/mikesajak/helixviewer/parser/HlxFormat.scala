package org.mikesajak.helixviewer.parser

import java.time.{LocalDateTime, ZoneOffset}

import org.mikesajak.helixviewer.parser.HlxReaderProtocol.WriteConverter2
import spray.json.{DefaultJsonProtocol, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue, JsonFormat, JsonReader}

case class HlxMetadata(name: String, application: String, appVersion: Int, modifiedDate: LocalDateTime,
                       author: String = null, band: String = null, song: String = null)

object HlxMetadata {
  def apply(params: Seq[Any]): HlxMetadata = {
    val cs = classOf[HlxMetadata].getConstructors
    cs(0).newInstance(params map { _.asInstanceOf[AnyRef] } : _*).asInstanceOf[HlxMetadata]
  }
}

case class HlxData(device: Int, deviceVersion: Int, metadata: HlxMetadata, tone: HlxTone)
case class HlxTone(dsp0: HlxDsp, dsp1: HlxDsp)
case class HlxDsp(id: Int, inputA: HlxInputBlock, inputB: HlxInputBlock,
                  outputA: HlxOutputBlock, outputB: HlxOutputBlock)

case class HlxInputBlock(model: String, input: Int, noiseGate: Boolean, threshold: Double, decay: Double)
case class HlxOutputBlock(model: String, output: Int, gain: Double, pan: Double)
class HlxBlock(val model: String, val blockType: Int /*TODO: enum? */,
               val position: Int, val stereo: Boolean, val path: Int /*TODO: enum?*/,
               val enabled: Boolean, val parameters: Map[String, Any])

object HlxReaderProtocol extends DefaultJsonProtocol {
  import ProtocolUtils._

  type WriteConverter = Any => (String, JsValue)
  case class WriteConverter2(fieldName: String, targetName: String, jsonValueConverter: Any => JsValue = toJsValue)

  case class ReadConverter2(fieldName: String, valueExtractor: JsValue => Any, defaultValProvider: () => Any = () => null)
  object ReadConverter2 {
    def fromJsString(fieldName: String, defaultValue: String = null.asInstanceOf[String]): ReadConverter2 = ReadConverter2(fieldName, extractJsString, () => defaultValue)
    def jsNumberToInt(fieldName: String, defaultValue: Int = null.asInstanceOf[Int]): ReadConverter2 = ReadConverter2(fieldName, extractJsInt, () => defaultValue)
    def jsNumberToDouble(fieldName: String, defaultValue: Double = null.asInstanceOf[Double]): ReadConverter2 = ReadConverter2(fieldName, extractJsDouble, () => defaultValue)
    def fromJsBoolean(fieldName: String, defaultValue: Boolean = null.asInstanceOf[Boolean]): ReadConverter2 = ReadConverter2(fieldName, extractJsBoolean, () => defaultValue)
    def fromJsObject[A: JsonReader](fieldName: String): ReadConverter2 = ReadConverter2(fieldName, extractJsObject[A])
  }

  abstract class MyJsonFormat[A <: AnyRef](writeConverters: Map[String, WriteConverter2],
                                           readConverters: Seq[ReadConverter2],
                                           clazz: Class[A]) extends JsonFormat[A] {

    def this(writeConverters: Seq[WriteConverter2], readConverters: Seq[ReadConverter2] = Seq() /*todo: remove*/ ,
             clazz: Class[A]) =
      this(writeConverters.map(wc => (wc.fieldName, wc)).toMap, readConverters, clazz)

    override def write(meta: A): JsValue = {
      val fields = toJsValues(meta, writeConverters)
      JsObject(fields: _*)
    }

    override def read(json: JsValue): A = {
      val fieldValues = for (readConverter <- readConverters) yield {
        json.asJsObject.fields.get(readConverter.fieldName)
            .filter(jsFieldValue => jsFieldValue != JsNull)
            .map(jsFieldValue => readConverter.valueExtractor(jsFieldValue))
            .getOrElse(readConverter.defaultValProvider())
      }

      createFromList(clazz, fieldValues)
    }
  }

  import ReadConverter2._

  implicit object HlxMetadataFormat extends
      MyJsonFormat[HlxMetadata](writeConverters = Seq(WriteConverter2("modifiedDate", "modifieddate", dateTimeToJsValue),
                                                      WriteConverter2("appVersion", "appversion")),
                                readConverters = Seq(fromJsString("name"), fromJsString("application"),
                                                     jsNumberToInt("appversion"), ReadConverter2("moddate", jsNumberToDateTime),
                                                     fromJsString("author"), fromJsString("band"), fromJsString("song")),
                                classOf[HlxMetadata])

  implicit object HlxInputBlockFormat
      extends MyJsonFormat[HlxInputBlock](Seq(WriteConverter2("input", "@input"),
                                              WriteConverter2("model", "@model")),
                                          Seq(fromJsString("@model"), jsNumberToInt("@input"),
                                              fromJsBoolean("noiseGate"), jsNumberToDouble("threshold"),
                                              jsNumberToDouble("decay")),
                                          classOf[HlxInputBlock])

  implicit object HlxOutputBlockFormat
      extends MyJsonFormat[HlxOutputBlock](Seq(WriteConverter2("output", "@output"),
                                               WriteConverter2("model", "@model")),
                                           Seq(fromJsString("@model"), jsNumberToInt("@output"),
                                               jsNumberToDouble("gain"), jsNumberToDouble("pan")),
                                           classOf[HlxOutputBlock])

  implicit object HlxDspFormat
      extends MyJsonFormat[HlxDsp](Seq(),
                                   Seq(jsNumberToInt("id", 0),
                                       fromJsObject[HlxInputBlock]("inputA"),
                                       fromJsObject[HlxInputBlock]("inputB"),
                                       fromJsObject[HlxOutputBlock]("outputA"),
                                       fromJsObject[HlxOutputBlock]("outputB")),
                                   classOf[HlxDsp])
}

object ProtocolUtils {
  def toJsValues(meta: AnyRef, converters: Map[String, WriteConverter2] = Map()): Seq[(String, JsValue)] = {
    getCCParams(meta).toList
                     .filter(x => x._2 != null)
                     .map { case (name, value) =>
                       converters.get(name)
                                 .map(conv => (conv.targetName, conv.jsonValueConverter(value)))
                                 .getOrElse((name, toJsValue(value)))
                     }
  }

  def convertTo(name: String, conv: Any => JsValue = toJsValue): Any => (String, JsValue) =
    (v: Any) => name -> conv(v)

  def getStrField(jsObject: JsObject, name: String)(implicit proto: JsonReader[String]): String = {
    val maybeValue = jsObject.fields.get(name)
    maybeValue
        .flatMap(f => if(f == JsNull) None else Some(f))
        .map(_.convertTo[String])
        .orNull
  }

  def toJsValue(value: Any): JsValue = value match {
    case s: String => JsString(s)
    case i: Int => JsNumber(i)
    case l: Long => JsNumber(l)
    case f: Float => JsNumber(f)
    case d: Double => JsNumber(d)
    case b: Boolean => JsBoolean(b)
    case x => JsString(x.toString)
  }

  def extractJsString(value: JsValue): String = value match {
    case JsString(str) => str
  }

  def extractJsInt(value: JsValue): Int = value match {
    case JsNumber(num) => num.toInt
  }

  def extractJsDouble(value: JsValue): Double = value match {
    case JsNumber(num) => num.toDouble
  }

  def extractJsBoolean(value: JsValue): Boolean = value match {
    case JsBoolean(bool) => bool
  }

  def extractJsObject[A: JsonReader](value: JsValue): A = value match {
    case JsObject(_) => value.convertTo[A]
  }

  def dateTimeToJsValue(value: Any): JsNumber = value match {
    case ld: LocalDateTime => JsNumber(ld.toEpochSecond(ZoneOffset.UTC))
  }

  def jsNumberToDateTime(value: JsValue): LocalDateTime = value match {
    case JsNumber(num) => intToDateTime(num.toInt)
  }

  def intToDateTime(num: Int): LocalDateTime =
    LocalDateTime.ofEpochSecond(num, 0, ZoneOffset.UTC)

  def getCCParams(cc: AnyRef): Map[String, Any] =
    cc.getClass.getDeclaredFields.foldLeft(Map.empty[String, Any]) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }

  //  trait Creatable[T <: Creatable[T]] {
  //    private val cs = this.getClass.getConstructors
  //    def createFromList(params: List[Any]): T =
  //      cs(0).newInstance(params map { _.asInstanceOf[AnyRef] } : _*).asInstanceOf[T]
  //  }
  //
  def createFromList[T](clazz: Class[T], params: Seq[Any]): T = {
    val cs = clazz.getConstructors
    cs(0).newInstance(params map { _.asInstanceOf[AnyRef] } : _*).asInstanceOf[T]
  }
}