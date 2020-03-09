package itx.iamservice.core.tests;

import itx.iamservice.core.model.ClientId;
import itx.iamservice.core.model.Model;
import itx.iamservice.core.model.ModelUtils;
import itx.iamservice.core.model.OrganizationId;
import itx.iamservice.core.model.ProjectId;
import itx.iamservice.core.model.TokenCache;
import itx.iamservice.core.model.TokenCacheImpl;
import itx.iamservice.core.model.TokenUtils;
import itx.iamservice.core.services.dto.JWToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokenCacheTests {

    private static final OrganizationId ORGANIZATION_ID = OrganizationId.from("unique-organization-id");
    private static final ProjectId PROJECT_ID = ProjectId.from("unique-project-id");
    private static final ClientId CLIENT_ID = ClientId.from("unique-client-id");
    private static final Set<String> ROLES = Set.of("role-a", "role-b", "role-c");
    private static final Long DURATION = 3L;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private static KeyPair keyPair;
    private static Model model;
    private static TokenCache tokenCache;
    private static JWToken jwToken;

    @BeforeAll
    private static void init() throws NoSuchAlgorithmException {
        keyPair = TokenUtils.generateKeyPair();
        model = ModelUtils.createDefaultModel("top-secret");
        tokenCache = new TokenCacheImpl(model);
        jwToken = TokenUtils.issueToken(ORGANIZATION_ID, PROJECT_ID, CLIENT_ID, DURATION, TIME_UNIT, ROLES, keyPair);
    }

    @Test
    @Order(1)
    public void afterInitializationTest() {
        int size = tokenCache.size();
        assertTrue(size == 0);
        assertFalse(tokenCache.isRevoked(jwToken));
    }

    @Test
    @Order(2)
    public void addRevokedTokenTest() {
        assertFalse(tokenCache.isRevoked(jwToken));
        tokenCache.addRevokedToken(jwToken);
        assertTrue(tokenCache.size() == 1);
        assertTrue(tokenCache.isRevoked(jwToken));
    }

    @Test
    @Order(3)
    public void waitForTokensToExpireTest() throws InterruptedException {
        while(tokenCache.purgeRevokedTokens() > 0) {
            Thread.sleep(1000);
        };
        int size = tokenCache.size();
        assertTrue(size == 0);
    }

    @Test
    @Order(4)
    public void afterCachePurgeTest() {
        assertFalse(tokenCache.isRevoked(jwToken));
    }

}
