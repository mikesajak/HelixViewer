package org.mikesajak.helixviewer.ui.controller

import org.mikesajak.helixviewer.parser.HlxReader
import scalafx.scene.control.Button
import scalafxml.core.macros.sfxml

@sfxml
class MainPanelController(inputBlockButton: Button,
                          block1Button: Button,
                          block2Button: Button,
                          block3Button: Button,
                          block4Button: Button,
                          block5Button: Button,
                          block6Button: Button,
                          outputBlockButton: Button,

                          hlxReader: HlxReader) {

  hlxReader.read("Plexi-Lead.hlx")

}
