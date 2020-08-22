package whisk.console.server

import java.io.{InputStream, OutputStream}
import java.util.concurrent.ConcurrentMap

import com.google.common.collect.MapMaker
import com.linecorp.armeria.common.grpc.GrpcJsonMarshaller
import io.grpc.MethodDescriptor.Marshaller
import scalapb.json4s.{Parser, Printer}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.io.{Codec, Source}

/**
  * A [[GrpcJsonMarshaller]] that serializes and deserializes a [[GeneratedMessage]] to and from
  * JSON.
  */
class ScalaPBJsonMarshaller private (
    jsonPrinter: Printer = ScalaPBJsonMarshaller.jsonDefaultPrinter,
    jsonParser: Parser = ScalaPBJsonMarshaller.jsonDefaultParser
) extends GrpcJsonMarshaller {

  override def serializeMessage[A](
      marshaller: Marshaller[A],
      message: A,
      os: OutputStream
  ): Unit = {
    if (!message.isInstanceOf[GeneratedMessage]) {
      throw new IllegalStateException(
        s"Unexpected message type: ${message.getClass} (expected: ${classOf[GeneratedMessage]})"
      )
    }
    val msg = message.asInstanceOf[GeneratedMessage]
    os.write(jsonPrinter.print(msg).getBytes())
  }

  override def deserializeMessage[A](marshaller: Marshaller[A], in: InputStream): A = {
    val companion = getMessageCompanion(marshaller)
    val jsonString = Source.fromInputStream(in)(Codec.UTF8).mkString
    val message = jsonParser.fromJsonString(jsonString)(companion)
    message.asInstanceOf[A]
  }

  private def getMessageCompanion[A](
      marshaller: Marshaller[A]
  ): GeneratedMessageCompanion[GeneratedMessage] = {
    val companion = ScalaPBJsonMarshaller.messageCompanionCache.get(marshaller)
    if (companion != null) {
      companion
    } else {
      ScalaPBJsonMarshaller.messageCompanionCache.computeIfAbsent(
        marshaller,
        key => {
          val field = key.getClass.getDeclaredField("companion")
          field.setAccessible(true)
          field.get(marshaller).asInstanceOf[GeneratedMessageCompanion[GeneratedMessage]]
        }
      )
    }
  }
}

/**
  * A companion object for [[ScalaPBJsonMarshaller]].
  */
object ScalaPBJsonMarshaller {

  private val messageCompanionCache
      : ConcurrentMap[Marshaller[_], GeneratedMessageCompanion[GeneratedMessage]] =
    new MapMaker().weakKeys().makeMap()

  private val jsonDefaultPrinter: Printer = new Printer().includingDefaultValueFields
  private val jsonDefaultParser: Parser = new Parser()

  private val defaultInstance: ScalaPBJsonMarshaller = new ScalaPBJsonMarshaller()

  /**
    * Returns a newly-created [[ScalaPBJsonMarshaller]].
    */
  def apply(
      jsonPrinter: Printer = jsonDefaultPrinter,
      jsonParser: Parser = jsonDefaultParser
  ): ScalaPBJsonMarshaller = {
    if (jsonPrinter == jsonDefaultPrinter && jsonParser == jsonDefaultParser) {
      defaultInstance
    } else {
      new ScalaPBJsonMarshaller(jsonPrinter, jsonParser)
    }
  }
}
