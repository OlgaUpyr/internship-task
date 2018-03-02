package modules

import com.google.inject.AbstractModule
import models.{ProductDAO, ProductDAOImpl, UserDAO, UserDAOImpl}

class DAOModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[UserDAO]).to(classOf[UserDAOImpl])
    bind(classOf[ProductDAO]).to(classOf[ProductDAOImpl])
  }
}
