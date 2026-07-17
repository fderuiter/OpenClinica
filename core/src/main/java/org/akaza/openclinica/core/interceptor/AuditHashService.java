package org.akaza.openclinica.core.interceptor;

import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.hibernate.query.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

@Service
public class AuditHashService {

    @Autowired
    private SessionFactory sessionFactory;

    public String computeHash(AuditLogEvent event, String previousHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            sb.append(previousHash == null ? "ROOT" : previousHash);
            sb.append("|").append(event.getAuditTable() != null ? event.getAuditTable() : "");
            sb.append("|").append(event.getEntityId() != null ? event.getEntityId() : "");
            sb.append("|").append(event.getEntityName() != null ? event.getEntityName() : "");
            sb.append("|").append(event.getReasonForChange() != null ? event.getReasonForChange() : "");
            sb.append("|").append(event.getOldValue() != null ? event.getOldValue() : "");
            sb.append("|").append(event.getNewValue() != null ? event.getNewValue() : "");
            
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public void validateChain() {
        StatelessSession session = sessionFactory.openStatelessSession();
        try {
            Query<AuditLogEvent> query = session.createQuery("FROM AuditLogEvent ORDER BY auditId ASC", AuditLogEvent.class);
            query.setFetchSize(1000);
            ScrollableResults<AuditLogEvent> results = query.scroll(ScrollMode.FORWARD_ONLY);
            String currentHash = null;

            while (results.next()) {
                AuditLogEvent event = results.get();
                String expectedHash = computeHash(event, currentHash);
                
                if (event.getChainHash() == null || !event.getChainHash().equals(expectedHash)) {
                    throw new DataIntegrityException("Audit log chain mismatch detected at auditId: " + event.getAuditId());
                }
                currentHash = expectedHash;
            }
        } finally {
            session.close();
        }
    }
}
