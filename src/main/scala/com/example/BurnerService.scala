package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.json._
import spray.httpx.SprayJsonSupport
import com.typesafe.config.ConfigFactory
import scala.concurrent.Future
import spray.http._
import spray.client.pipelining._
import com.example.Boot.system.dispatcher

class BurnerServiceActor extends Actor with BurnerService {
  def actorRefFactory = context
  def receive = runRoute(route)
}

case class BurnerEvent(`type`: String, fromNumber: String, toNumber: String, payload: String)
object BurnerEventJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PortofolioFormats = jsonFormat4(BurnerEvent)
}

import BurnerEventJsonSupport._

trait BurnerService extends HttpService {
  val config = ConfigFactory.load();
  val dropboxToken = config.getString("dropbox.token")
  val pipeline: HttpRequest => Future[HttpResponse] = (
    addHeader("Authorization", s"Bearer $dropboxToken")
      ~> sendReceive)
  val counts = new scala.collection.mutable.HashMap[String,Int].withDefaultValue(0)
  val eventPath =
    path("event") {
      post {
        entity(as[BurnerEvent]) { event =>
          event.`type` match {
            case "inboundMedia" =>
              val filename = s"image${counts.size}.${event.payload.reverse.takeWhile(_ != '.').reverse}"
              counts.put(filename, counts(filename) + 1)
              val url = s"https://api.dropboxapi.com/1/save_url/auto/$filename"
              complete { pipeline(Post(url, FormData(Seq("url" -> event.payload)))) }
            case "inboundText" =>
              counts.keys.find(event.payload.contains(_)).foreach(k => counts.put(k, counts(k) + 1))
              complete {StatusCodes.OK}
            case _ => complete {StatusCodes.OK}
          }
        }
      }
    }

  val reportPath =
    path("report") {
      get {
        complete {counts.map{case (k, v) => (k, new JsString(v.toString))}.toMap[String, JsValue]}
      }
    }

  val route = eventPath ~ reportPath
}
