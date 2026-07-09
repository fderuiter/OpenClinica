package org.akaza.openclinica.ws;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class SpringPlainTextPasswordValidationCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    private AuthenticationManager authenticationManager;
    private boolean ignoreFailure = false;

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    @Override
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof WSPasswordCallback) {
            WSPasswordCallback pc = (WSPasswordCallback) callback;
            try {
                Authentication authResult =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(pc.getIdentifier(), pc.getPassword()));
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication success: " + authResult.toString());
                }
                SecurityContextHolder.getContext().setAuthentication(authResult);
            } catch (AuthenticationException failed) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication request for user '" + pc.getIdentifier() + "' failed: " + failed.toString());
                }
                SecurityContextHolder.clearContext();
                if (!ignoreFailure) {
                    throw new IOException("Authentication failed", failed);
                }
            }
        } else if (callback instanceof CleanupCallback) {
            SecurityContextHolder.clearContext();
            return;
        } else {
            throw new UnsupportedCallbackException(callback);
        }
    }
}
