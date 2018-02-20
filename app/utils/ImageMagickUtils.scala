package utils

import org.im4java.core.{ConvertCmd, IMOperation}
import org.im4java.process.ProcessStarter

object ImageMagickUtils {
  ProcessStarter.setGlobalSearchPath("C:\\ImageMagick")
  val convertCmd = new ConvertCmd
  val imOperation = new IMOperation

  def resizeImage(fileUrl: String, size: Int) = {
    imOperation.addImage(fileUrl)
    imOperation.resize(size, size, '^')
    imOperation.gravity("Center")
    imOperation.crop(size, size, 0, 0)
    imOperation.addImage(fileUrl)
    convertCmd.run(imOperation)
  }
}
