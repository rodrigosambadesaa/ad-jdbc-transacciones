package dev.rodrigosambade.transactions;
import java.math.BigDecimal;
import java.sql.*;
public final class TransferService {
    public void init(Connection c)throws SQLException{
        try(var s=c.createStatement()){
            s.execute("create table if not exists cuenta(id int primary key,saldo decimal(14,2) not null check(saldo>=0))");
            s.executeUpdate("merge into cuenta key(id) values(1,1000),(2,500)");
        }
    }
    public void transfer(Connection c,int from,int to,BigDecimal amount)throws SQLException{
        if(amount==null||amount.signum()<=0)throw new IllegalArgumentException("Importe positivo obligatorio");
        boolean oldAuto=c.getAutoCommit();
        int oldIso=c.getTransactionIsolation();
        try{
            c.setAutoCommit(false);
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            BigDecimal balance;
            try(var ps=c.prepareStatement("select saldo from cuenta where id=? for update")){
                ps.setInt(1,from);
                try(var rs=ps.executeQuery()){
                    if(!rs.next())throw new SQLException("Cuenta origen inexistente");
                    balance=rs.getBigDecimal(1);
                }
            }
            if(balance.compareTo(amount)<0)throw new SQLException("Saldo insuficiente");
            update(c,from,amount.negate());
            update(c,to,amount);
            c.commit();
        } catch(Exception e){
            c.rollback();
            if(e instanceof SQLException s)throw s;
            throw e;
        } finally{
            c.setTransactionIsolation(oldIso);
            c.setAutoCommit(oldAuto);
        }
    }
    private void update(Connection c,int id,BigDecimal delta)throws SQLException{
        try(var ps=c.prepareStatement("update cuenta set saldo=saldo+? where id=?")){
            ps.setBigDecimal(1,delta);
            ps.setInt(2,id);
            if(ps.executeUpdate()!=1)throw new SQLException("Cuenta inexistente: "+id);
        }
    }
    public BigDecimal balance(Connection c,int id)throws SQLException{
        try(var ps=c.prepareStatement("select saldo from cuenta where id=?")){
            ps.setInt(1,id);
            try(var rs=ps.executeQuery()){
                if(!rs.next())throw new SQLException("Cuenta inexistente");
                return rs.getBigDecimal(1);
            }
        }
    }
}
