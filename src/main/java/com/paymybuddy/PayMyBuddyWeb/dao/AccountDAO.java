package com.paymybuddy.paymybuddyweb.dao;

import com.paymybuddy.paymybuddyweb.interfaces.DatabaseConfigurationInterface;
import com.paymybuddy.paymybuddyweb.interfaces.dao.AccountDAOInterface;
import com.paymybuddy.paymybuddyweb.models.Account;
import com.paymybuddy.paymybuddyweb.models.Currency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author MorganCpn
 */
@Singleton
public class AccountDAO implements AccountDAOInterface {
    /**
     * Logger log4j2
     */
    private static final Logger logger = LogManager.getLogger("UserDAO");

    /**
     * Database configuration
     */
    private DatabaseConfigurationInterface databaseConfiguration;

    /**
     * Constructor
     * @param databaseConfiguration
     */
    public AccountDAO(DatabaseConfigurationInterface databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    /**
     * @see AccountDAOInterface {@link #getAccount(Integer)}
     */
    @Override
    public Account getAccount(Integer userId){
        Account result = null;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatement ps = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT amount, currency, balance_date");
        sql.append(" FROM account");
        sql.append(" WHERE user_id = ?");
        sql.append(" AND balance_date = (SELECT MAX(balance_date) FROM account WHERE user_id = ?)");

        try {
            con = databaseConfiguration.getConnection();
            ps = con.prepareStatement(sql.toString());
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            if(rs.next()){
                result = new Account(
                        userId,
                        rs.getDouble("amount"),
                        new Currency(rs.getString("currency")),
                        rs.getDate("balance_date").toLocalDate());
            }
            logger.info("AccountDAO.getAccount() -> Account get for user : {0}", userId);
        } catch (Exception e){
            logger.error("AccountDAO.getAccount() -> Error fetching user account", e);
        } finally {
            databaseConfiguration.closeSQLTransaction(con, ps, rs);

        }
        return result;
    }

    /**
     * @see AccountDAOInterface {@link #updateAccount(Account)}
     */
    @Override
    public void updateAccount(Account account){
        Connection con = null;
        PreparedStatement ps = null;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE account");
        sql.append(" SET amount = ?, currency = ?, balance_date = ?");
        sql.append(" WHERE user_id = ?");

        try {
            con = databaseConfiguration.getConnection();
            ps = con.prepareStatement(sql.toString());
            ps.setDouble(1, account.getAmount());
            ps.setString(2, account.getCurrency().getCode());
            ps.setDate(3, Date.valueOf(account.getDate()));
            ps.setInt(4, account.getUserId());
            ps.execute();
            logger.info("AccountDAO.updateAccount() -> Account updated for user : {0}", account.getUserId());
        } catch (Exception ex){
            logger.error("AccountDAO.updateAccount() -> Error update user account", ex);
        } finally {
            databaseConfiguration.closeSQLTransaction(con, ps, null);
        }
    }

    /**
     * @see AccountDAOInterface {@link #updateAccount(Account)}
     */
    @Override
    public void createAccount(Integer userId, String currency){
        Connection con = null;
        PreparedStatement ps = null;

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO account (user_id, amount, currency, balance_date)");
        sql.append(" VALUES (?, 0, ?, NOW())");

        try {
            con = databaseConfiguration.getConnection();
            ps = con.prepareStatement(sql.toString());
            ps.setInt(1, userId);
            ps.setString(2, currency);
            ps.execute();
            logger.info("AccountDAO.createAccount() -> Account created for user : {0}", userId);
        } catch (Exception ex){
            logger.error("AccountDAO.createAccount() -> Error create user account", ex);
        } finally {
            databaseConfiguration.closeSQLTransaction(con, ps, null);
        }
    }
}
