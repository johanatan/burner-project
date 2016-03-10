package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import spray.httpx.SprayJsonSupport

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
  val counts = new scala.collection.mutable.HashMap[String,Int].withDefaultValue(0)
  val eventPath =
    path("event") {
      post {
        entity(as[BurnerEvent]) { event =>
          event.`type` match {
            case "inboundMedia" => counts.put(event.payload, counts(event.payload) + 1)
            case "inboundText" => counts.keys.find(event.payload.contains(_)).foreach(k => counts.put(k, counts(k) + 1))
            case _ => ;
          }
          complete {StatusCodes.OK}
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
