package com.gruelbox.transactionoutbox.acceptance;

import com.gruelbox.transactionoutbox.TransactionManager;
import java.sql.Statement;

class TestUtils {

  @SuppressWarnings("SameParameterValue")
  static void runSql(TransactionManager transactionManager, String sql) {
    transactionManager.inTransaction(
        tx -> {
          try {
            try (Statement statement = tx.connection().createStatement()) {
              statement.execute(sql);
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }
}
