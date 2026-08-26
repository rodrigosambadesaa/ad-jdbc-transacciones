package dev.rodrigosambade.transactions;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class TransferService {

    public void init(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS cuenta (
                        id INT PRIMARY KEY,
                        saldo DECIMAL(14,2) NOT NULL CHECK (saldo >= 0)
                    )
                    """);
            statement.executeUpdate(
                    "MERGE INTO cuenta KEY(id) VALUES (1, 1000), (2, 500)");
        }
    }

    public void transfer(
            Connection connection,
            int sourceAccount,
            int destinationAccount,
            BigDecimal amount) throws SQLException {
        validateAmount(amount);

        boolean previousAutoCommit = connection.getAutoCommit();
        int previousIsolation = connection.getTransactionIsolation();

        try {
            beginSerializableTransaction(connection);
            BigDecimal sourceBalance = lockAndReadBalance(connection, sourceAccount);
            ensureSufficientFunds(sourceBalance, amount);

            addToBalance(connection, sourceAccount, amount.negate());
            addToBalance(connection, destinationAccount, amount);
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            rollback(connection, exception);
            throw exception;
        } finally {
            restoreConnectionState(
                    connection,
                    previousIsolation,
                    previousAutoCommit);
        }
    }

    public BigDecimal balance(Connection connection, int accountId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT saldo FROM cuenta WHERE id = ?")) {
            statement.setInt(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Cuenta inexistente: " + accountId);
                }
                return resultSet.getBigDecimal("saldo");
            }
        }
    }

    private static void beginSerializableTransaction(Connection connection)
            throws SQLException {
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }

    private static BigDecimal lockAndReadBalance(
            Connection connection,
            int accountId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT saldo FROM cuenta WHERE id = ? FOR UPDATE")) {
            statement.setInt(1, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Cuenta origen inexistente: " + accountId);
                }
                return resultSet.getBigDecimal("saldo");
            }
        }
    }

    private static void ensureSufficientFunds(
            BigDecimal balance,
            BigDecimal amount) throws SQLException {
        if (balance.compareTo(amount) < 0) {
            throw new SQLException("Saldo insuficiente");
        }
    }

    private static void addToBalance(
            Connection connection,
            int accountId,
            BigDecimal delta) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE cuenta SET saldo = saldo + ? WHERE id = ?")) {
            statement.setBigDecimal(1, delta);
            statement.setInt(2, accountId);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Cuenta inexistente: " + accountId);
            }
        }
    }

    private static void rollback(
            Connection connection,
            Exception originalException) throws SQLException {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            rollbackException.addSuppressed(originalException);
            throw rollbackException;
        }
    }

    private static void restoreConnectionState(
            Connection connection,
            int isolation,
            boolean autoCommit) throws SQLException {
        connection.setTransactionIsolation(isolation);
        connection.setAutoCommit(autoCommit);
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Importe positivo obligatorio");
        }
    }
}
