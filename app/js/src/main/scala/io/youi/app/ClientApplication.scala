package io.youi.app

import io.youi.ajax.AjaxRequest
import io.youi.app.screen.ScreenManager
import io.youi.{History, JavaScriptError, JavaScriptLog}
import io.youi.app.sourceMap.ErrorTrace
import org.scalajs.dom._
import io.youi.dom._
import io.youi.net.URL
import profig.JsonUtil
import scribe.LogRecord
import scribe.output.LogOutput
import scribe.writer.Writer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.|

trait ClientApplication extends YouIApplication with ScreenManager {
  ClientApplication.instance = this

  addScript("/source-map.min.js")

  override def isClient: Boolean = true

  override def isServer: Boolean = false

  // Configure communication end-points
  private var configuredConnectivity: Map[ApplicationConnectivity, ClientConnectivity] = Map.empty

  def clientConnectivity(connectivity: ApplicationConnectivity): ClientConnectivity = configuredConnectivity(connectivity)

  private val errorFunction: js.Function5[String, String, Int, Int, Throwable | js.Error, Unit] = (message: String, source: String, line: Int, column: Int, err: Throwable | js.Error) => {
    err match {
      case null => ErrorTrace.toError(message, source, line, column, None).map(ClientApplication.sendError)
      case t: Throwable => ErrorTrace.toError(message, source, line, column, Some(t)).map(ClientApplication.sendError)
      case e: js.Error => ErrorTrace.toError(message, source, line, column, Some(js.JavaScriptException(e))).map(ClientApplication.sendError)
    }
    ()
  }

  if (logJavaScriptErrors) {
    js.Dynamic.global.window.onerror = errorFunction
    scribe.Logger.root.withHandler(writer = ErrorTrace).replace()
  }

  connectivityEntries.attachAndFire { entries =>
    entries.foreach { connectivity =>
      if (!configuredConnectivity.contains(connectivity)) {
        configuredConnectivity += connectivity -> new ClientConnectivity(connectivity, this)
      }
    }
  }

  def autoReload: Boolean = true

  override def cached(url: URL): String = url.asPath()
}

object ClientApplication {
  lazy val logWriter: Writer = new Writer {
    override def write[M](record: LogRecord[M], output: LogOutput): Unit = sendLog(JavaScriptLog(output.plainText))
  }

  private var instance: ClientApplication = _

  def sendError(throwable: Throwable): Future[XMLHttpRequest] = {
    ErrorTrace.toError(throwable).flatMap(sendError)
  }

  def sendError(error: JavaScriptError): Future[XMLHttpRequest] = {
    val formData = new FormData
    val jsonString = JsonUtil.toJsonString(error)
    formData.append("error", jsonString)
    val request = new AjaxRequest(History.url().replacePathAndParams(instance.logPath), data = Some(formData))
    request.send()
  }

  def sendError(event: ErrorEvent): Future[XMLHttpRequest] = {
    ErrorTrace.toError(event).flatMap(sendError)
  }

  def sendLog(log: JavaScriptLog): Future[XMLHttpRequest] = {
    val formData = new FormData
    val jsonString = JsonUtil.toJsonString(log)
    formData.append("message", jsonString)
    val request = new AjaxRequest(History.url().replacePathAndParams(instance.logPath), data = Some(formData))
    request.send()
  }
}