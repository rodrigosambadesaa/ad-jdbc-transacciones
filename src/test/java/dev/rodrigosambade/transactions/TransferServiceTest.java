package dev.rodrigosambade.transactions;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;
class TransferServiceTest{
    @Test void commitsAndRollsBack()throws Exception{
        try(var c=DriverManager.getConnection("jdbc:h2:mem:tx;DB_CLOSE_DELAY=-1","sa","")){
            var s=new TransferService();
            s.init(c);
            s.transfer(c,1,2,new BigDecimal("100"));
            assertEquals(new BigDecimal("900.00"),s.balance(c,1));
            assertThrows(java.sql.SQLException.class,()->s.transfer(c,1,2,new BigDecimal("9999")));
            assertEquals(new BigDecimal("900.00"),s.balance(c,1));
        }
    }
}
