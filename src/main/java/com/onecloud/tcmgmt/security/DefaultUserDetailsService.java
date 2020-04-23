
package com.onecloud.tcmgmt.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.onecloud.tcmgmt.dao.UserDao;
import com.onecloud.tcmgmt.domain.appdb.User;

public class DefaultUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUserDetailsService.class);

    private final UserDao dao;

    @Autowired
    public DefaultUserDetailsService(UserDao dao) {
        this.dao = dao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException,
            DataAccessException {
        logger.debug("Loading user details for '{}'", email);
        try {
            User user = this.dao.getByEmail(email);
            if (user == null) {
                logger.debug("No user found by email '{}'. Nothing to load.", email);
                throw new UsernameNotFoundException(email);
            }
            user.setTotalLogins(new Long(user.getTotalLogins().longValue()+1));
            dao.save(user);
            user = this.dao.getByEmail(email);             
            return new DefaultUserDetails(user);
        } catch (DataAccessException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to load user by email '" + email
                        + "' due to a data access error", e);
            }
            throw e;
        }
    }
}
