package io.youi.example.ui.hypertext

import io.youi.ui
import io.youi.hypertext.ImageView
import io.youi.hypertext.style.Image

import scala.concurrent.Future

object ImageExample extends HTMLScreen {
  override def name: String = "HTML Image Example"

  override protected def load(): Future[Unit] = super.load().map { _ =>
    val icon = Image("/images/icon.png")
    container.children += new ImageView {    // Top-Left
      image := icon
      position.left := 50.0
      position.top := 50.0
    }
    container.children += new ImageView {    // Top-Right
      image := icon
      position.right := ui.position.right - 50.0
      position.top := 50.0
    }
    container.children += new ImageView {    // Bottom-Left
      image := icon
      position.left := 50.0
      position.bottom := ui.position.bottom - 50.0
    }
    container.children += new ImageView {    // Bottom-Right
      image := icon
      position.right := ui.position.right - 50.0
      position.bottom := ui.position.bottom - 50.0
    }
    container.children += new ImageView {    // Center
      image := icon
      position.center := ui.position.center
      position.middle := ui.position.middle
    }
  }

  override def path: String = "/examples/html/images.html"
}
