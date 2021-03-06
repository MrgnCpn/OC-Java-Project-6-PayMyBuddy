package com.paymybuddy.paymybuddyweb.interfaces.dao;

import com.paymybuddy.paymybuddyweb.models.User;

import java.util.List;

/**
 * @author MorganCpn
 */
public interface UserDAOInterface {
    /**
     * Get user with id
     * @param id
     * @return User
     */
    User getUserById(Integer id);

    /**
     * Get user with username
     * @param username
     * @return User
     */
    User getUserByUsername(String username);

    /**
     * Update user
     * @param user
     */
    void updateUser(User user);

    /**
     * Create new user
     * @param user
     * @param currency
     * @param password
     */
    void createUser(User user, String currency, String password);

    /**
     * Get list of users with search
     * @param userID
     * @param search
     * @return
     */
    List<User> searchUsers(Integer userID, String search);
}