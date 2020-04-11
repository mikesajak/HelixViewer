package org.mikesajak.helixviewer

import com.google.inject.{AbstractModule, Guice, Injector, Provides}
import net.codingwell.scalaguice.ScalaModule
import org.mikesajak.helixviewer.parser.HlxReader

//object ApplicationContext {
//  lazy val globalInjector: Injector = Guice.createInjector(new ApplicationContext)
//}

class ApplicationContext extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    //    install(new PanelContext(LeftPanel))
    //    install(new PanelContext(RightPanel))
    //
    //    install(new UIOperationControllersContext)
  }

  @Provides
  def provideHlxReader() = new HlxReader
}
