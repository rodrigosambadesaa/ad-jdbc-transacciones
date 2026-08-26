package dev.rodrigosambade.transactions;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferServiceTest {

    @Test
    void commitsValidTransferAndRollsBackInvalidTransfer() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:tx;DB_CLOSE_DELAY=-1",
                "sa",
                "")) {
            TransferService service = new TransferService();
            service.init(connection);

            service.transfer(
                    connection,
                    1,
                    2,
                    new BigDecimal("100"));
            assertEquals(new BigDecimal("900.00"), service.balance(connection, 1));

            assertThrows(
                    SQLException.class,
                    () -> service.transfer(
                            connection,
                            1,
                            2,
                            new BigDecimal("9999")));

            assertEquals(new BigDecimal("900.00"), service.balance(connection, 1));
        }
    }
}
