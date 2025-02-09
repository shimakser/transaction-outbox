package com.gruelbox.transactionoutbox;

import static com.gruelbox.transactionoutbox.Utils.uncheckedly;

import java.sql.Connection;
import java.sql.DriverManager;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link ConnectionProvider} which requests connections directly from {@link DriverManager}.
 *
 * <p>Unlikely to be suitable for most production applications since it doesn't use any sort of
 * connection pool.
 *
 * <p>Usage:
 *
 * <pre>ConnectionProvider provider = SimpleConnectionProvider.builder()
 *   .driverClassName("org.postgresql.Driver")
 *   .url(myJdbcUrl)
 *   .user("myusername")
 *   .password("mypassword")
 *   .build()</pre>
 */
@SuperBuilder
@Slf4j
final class DriverConnectionProvider implements ConnectionProvider, Validatable {

  private final String driverClassName;
  private final String url;
  private final String user;
  private final String password;

  private volatile boolean initialized;

  @Override
  public Connection obtainConnection() {
    return uncheckedly(
        () -> {
          if (!initialized) {
            synchronized (this) {
              log.debug("Initialising {}", driverClassName);
              Class.forName(driverClassName);
              initialized = true;
            }
          }
          log.debug("Opening connection to {}", url);
          Connection connection = DriverManager.getConnection(url, user, password);
          connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
          return connection;
        });
  }

  @Override
  public void validate(Validator validator) {
    validator.notBlank("driverClassName", driverClassName);
    validator.notBlank("url", url);
    validator.notBlank("user", user);
    validator.notBlank("password", password);
  }
}
