package com.org.security

import java.util.UUID

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.TwitterModule
import com.twitter.inject.requestscope.RequestScopeBinding
import com.typesafe.config.{Config, ConfigFactory}

object AuthModule extends TwitterModule with RequestScopeBinding {

  override def configure(): Unit = {
    val config = ConfigFactory.load()

    //maksudnya UUID singleton untuk scope Request?
    bindRequestScope[Option[UUID]]
    bind[Crypto].toInstance(new BasicCrypto(config.getString("webApi.cookie-secret")))
    bind[PasswordCrypto].toInstance(new BCryptPasswordCrypto)
  }


  /*
  @Singleton

  https://github.com/google/guice/wiki/Scopes

  By default, Guice returns a new instance each time it supplies a value.
  This behaviour is configurable via scopes. Scopes allow you to reuse instances:
    - for the lifetime of an application (@Singleton),
    - a session (@SessionScoped), or
    - a request (@RequestScoped).

    @Singleton indicates that the class is intended to be threadsafe.
    @Singleton
    public class InMemoryTransactionLog implements TransactionLog {
      /* everything here should be threadsafe! */
    }
    Scopes can also be configured in bind statements:

      bind(TransactionLog.class).to(InMemoryTransactionLog.class).in(Singleton.class);
    And by annotating @Provides methods:

      @Provides @Singleton
      TransactionLog provideTransactionLog() {
        ...
      }
   */

  /*
  @Provides
      Dipakai jika kita butuh instance object, misal karena perlu baca dari Config
     https://github.com/google/guice/wiki/ProvidesMethods

     public class BillingModule extends AbstractModule {
     @Override
     protected void configure() {
       ...
     }

     @Provides
     TransactionLog provideTransactionLog() {
       DatabaseTransactionLog transactionLog = new DatabaseTransactionLog();
       transactionLog.setJdbcUrl("jdbc:mysql://localhost/pizza");
       transactionLog.setThreadPoolSize(30);
       return transactionLog;
     }
    }
  */
  //akan dipakai untuk inject di SessionService
  @Singleton
  @Provides
  def sessionStore(config: Config): SessionStore =
    new EhcacheSessionStore()
}
