package org.mikesajak.helixviewer

import com.google.inject.{Guice, Injector, Key}
import com.typesafe.scalalogging.Logger
import org.mikesajak.helixviewer.ui.UILoader
import scalafx.animation.{KeyFrame, KeyValue, Timeline}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.util.Duration
import scalafx.Includes._

object Main extends JFXApp {
  private val logger = Logger("Main")
  private val mainPanelDef: String = "/helix-view.fxml"

  logger.info(s"HelixViewer starting")

//  private val injector = ApplicationContext.globalInjector.createChildInjector()
  private implicit val globalInjector: Injector = Guice.createInjector(new ApplicationContext)
//  private val config = injector.getInstance(classOf[Configuration])
//  private val appController = injector.getInstance(classOf[ApplicationController])
//  private val resourceMgr = injector.getInstance(Key.get(classOf[ResourceManager]))
//  private val pluginManager = injector.getInstance(Key.get(classOf[PluginManager]))

  val (root, _) = UILoader.loadScene(mainPanelDef)

  stage = new PrimaryStage() {
    title = "HelixViewer"//resourceMgr.getMessage("app.name")
//    icons += resourceMgr.getIcon("internal_drive.png")
    scene = new Scene(root)
  }

  //  Platform.implicitExit = false
//  stage.onCloseRequest = we => {
//    we.consume()
//    appController.exitApplication { () =>
//      new Timeline {
//        keyFrames.add(KeyFrame(Duration(800), "fadeOut", null, Set(KeyValue(stage.opacity, 0))))
//        onFinished = () => Platform.exit
//      }.play()
//      true
//    }
//  }

//  appController.init(stage, this)
//  pluginManager.init()

  stage.width = 1000 //config.intProperty(ConfigKeys.WindowWidth).getOrElse(1000): Int
  stage.height = 600 //config.intProperty(ConfigKeys.WindowHeight).getOrElse(600): Int

  stage.toFront()
//  stage.opacity.value = 0
  stage.show()

//  new Timeline {
//    keyFrames.add(KeyFrame(Duration(800), "fadeIn", null, Set(KeyValue(stage.opacity, 1))))
//  }.play()

  override def main(args: Array[String]): Unit = {
    super.main(args)
  }
}
