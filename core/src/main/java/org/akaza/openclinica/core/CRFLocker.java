/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.core;

import java.io.Serializable;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Data structure used to keep track of CRFs locked by users. The synchronization of access to the locks is implemented
 * internally, so clients of this class don't have to deal with it.
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class CRFLocker implements Serializable {

    private static final long serialVersionUID = -541015729642748245L;

    private transient JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Locks a CRF for an user.
     *
     * @param crfId ID of the CFR to be locked.
     * @param userId ID of the user who owns the lock.
     * @return true if the lock was acquired or already held by the user, false if held by another user.
     */
    public boolean lock(int crfId, int userId) {
        if (jdbcTemplate != null) {
            try {
                Integer ownerId = getLockOwner(crfId);
                if (ownerId != null) {
                    return ownerId.equals(userId);
                }
                jdbcTemplate.update("INSERT INTO crf_lock_registry (crf_id, user_id) VALUES (?, ?)", crfId, userId);
                return true;
            } catch (DuplicateKeyException e) {
                return false;
            } catch (DataAccessException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Unlocks a CRF.
     *
     * @param crfId The ID of the CRF to be unlocked
     */
    public void unlock(int crfId) {
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("DELETE FROM crf_lock_registry WHERE crf_id = ?", crfId);
            } catch (DataAccessException e) {
                // Ignore
            }
        }
    }

    /**
     * Release all the locks owned by a user.
     *
     * @param userId ID of the user.
     */
    public void unlockAllForUser(int userId) {
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("DELETE FROM crf_lock_registry WHERE user_id = ?", userId);
            } catch (DataAccessException e) {
                // Ignore
            }
        }
    }

    /**
     * If the CRF is locked by a user.
     *
     * @param crfId ID of the CRF.
     * @return
     */
    public boolean isLocked(int crfId) {
        if (jdbcTemplate != null) {
            try {
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crf_lock_registry WHERE crf_id = ?", Integer.class, crfId);
                return count != null && count > 0;
            } catch (DataAccessException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Identifies the owner of a CRF lock.
     *
     * @param crfId ID of the CRF.
     * @return ID of the user who owns the lock, <code>null</code> if the CRF is not locked.
     */
    public Integer getLockOwner(int crfId) {
        if (jdbcTemplate != null) {
            try {
                return jdbcTemplate.queryForObject("SELECT user_id FROM crf_lock_registry WHERE crf_id = ?", Integer.class, crfId);
            } catch (DataAccessException e) {
                return null;
            }
        }
        return null;
    }

}
