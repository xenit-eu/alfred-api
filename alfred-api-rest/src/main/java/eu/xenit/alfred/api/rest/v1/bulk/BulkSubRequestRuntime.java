package eu.xenit.alfred.api.rest.v1.bulk;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

/**
 * Runs one bulk sub-request through the web script runtime, as Alfresco's web script servlet runs an HTTP request,
 * but as the user who sent the bulk request and reporting unexpected exceptions to the bulk web script instead of
 * rendering them.
 */
final class BulkSubRequestRuntime extends WebScriptServletRuntime {

    private final String userName;
    private Throwable failure;

    BulkSubRequestRuntime(RuntimeContainer container, HttpServletRequest req, HttpServletResponse res,
            String userName) {
        super(container, null, req, res, null);
        this.userName = userName;
    }

    @Override
    protected Authenticator createAuthenticator() {
        return new Authenticator() {
            @Override
            public boolean authenticate(RequiredAuthentication required, boolean isGuest) {
                // The container may have cleared the security context before authenticating (multi-tenant setups).
                if (!userName.equals(AuthenticationUtil.getFullyAuthenticatedUser())) {
                    AuthenticationUtil.setFullyAuthenticatedUser(userName);
                }
                return true;
            }

            @Override
            public boolean emptyCredentials() {
                return false;
            }
        };
    }

    @Override
    protected boolean beforeProcessError(Match match, Throwable e) {
        if (e instanceof WebScriptException) {
            // An HTTP error status: rendered as for a direct request.
            return true;
        }
        failure = e;
        return false;
    }

    /** The exception the sub-request failed with, or {@code null}. */
    Throwable getFailure() {
        return failure;
    }
}
