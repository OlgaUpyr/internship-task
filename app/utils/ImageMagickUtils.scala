package utils

import org.im4java.core.{ConvertCmd, IMOperation}
import org.im4java.process.ProcessStarter

object ImageMagickUtils {
  ProcessStarter.setGlobalSearchPath("C:\\ImageMagick")
  val convertCmd = new ConvertCmd

  def resizeImage(fileUrl: String, size: Int, outputFile: String) = {
    val imOperation = new IMOperation
    imOperation.addImage(fileUrl)
    imOperation.resize(size, size, '^')
    imOperation.gravity("Center")
    imOperation.crop(size, size, 0, 0)
    imOperation.addImage(outputFile)
    convertCmd.run(imOperation)
  }
}
