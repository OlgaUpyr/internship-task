package modules

import com.google.inject.AbstractModule
import models.{UserDAO, UserDAOImpl}

class DAOModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[UserDAO])
      .to(classOf[UserDAOImpl])
  }
}
