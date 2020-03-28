package itx.iamservice.core;

import itx.iamservice.core.model.Model;
import itx.iamservice.core.model.PKIException;
import itx.iamservice.core.model.utils.ModelUtils;
import itx.iamservice.core.services.ClientService;
import itx.iamservice.core.services.ResourceServerService;
import itx.iamservice.core.services.admin.ClientManagementService;
import itx.iamservice.core.services.admin.OrganizationManagerService;
import itx.iamservice.core.services.admin.ProjectManagerService;
import itx.iamservice.core.services.admin.UserManagerService;
import itx.iamservice.core.services.caches.AuthorizationCodeCache;
import itx.iamservice.core.services.caches.CacheCleanupScheduler;
import itx.iamservice.core.services.caches.TokenCache;
import itx.iamservice.core.services.impl.ClientServiceImpl;
import itx.iamservice.core.services.impl.ResourceServerServiceImpl;
import itx.iamservice.core.services.impl.admin.ClientManagementServiceImpl;
import itx.iamservice.core.services.impl.admin.OrganizationManagerServiceImpl;
import itx.iamservice.core.services.impl.admin.ProjectManagerServiceImpl;
import itx.iamservice.core.services.impl.admin.UserManagerServiceImpl;
import itx.iamservice.core.services.impl.caches.AuthorizationCodeCacheImpl;
import itx.iamservice.core.services.impl.caches.CacheCleanupSchedulerImpl;
import itx.iamservice.core.services.impl.caches.TokenCacheImpl;
import itx.iamservice.core.services.impl.persistence.InMemoryPersistenceServiceImpl;
import itx.iamservice.core.services.persistence.PersistenceService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.concurrent.TimeUnit;

public class IAMCoreBuilder {

    private Model model;
    private PersistenceService persistenceService;
    private CacheCleanupScheduler cacheCleanupScheduler;
    private AuthorizationCodeCache authorizationCodeCache;
    private TokenCache tokenCache;
    private ClientService clientService;
    private ResourceServerService resourceServerService;
    private ClientManagementService clientManagementService;
    private OrganizationManagerService organizationManagerService;
    private ProjectManagerService projectManagerService;
    private UserManagerService userManagerService;

    public IAMCoreBuilder withBCProvider() {
        Security.addProvider(new BouncyCastleProvider());
        return this;
    }

    public IAMCoreBuilder withModel(Model model) {
        this.model = model;
        return this;
    }

    public IAMCoreBuilder withDefaultModel(String iamAdminPassword) throws PKIException {
        this.model = ModelUtils.createDefaultModel(iamAdminPassword);
        return this;
    }

    public IAMCoreBuilder withPersistentService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        return this;
    }

    public IAMCoreBuilder withDefaultPersistentService() {
        this.persistenceService = new InMemoryPersistenceServiceImpl();
        return this;
    }

    public IAMCoreBuilder withAuthorizationCodeCache(AuthorizationCodeCache authorizationCodeCache) {
        this.authorizationCodeCache = authorizationCodeCache;
        return this;
    }

    public IAMCoreBuilder withDefaultAuthorizationCodeCache(Long maxDuration, TimeUnit timeUnit) {
        this.authorizationCodeCache = new AuthorizationCodeCacheImpl(maxDuration, timeUnit);
        return this;
    }

    public IAMCoreBuilder withTokenCache(TokenCache tokenCache) {
        this.tokenCache = tokenCache;
        return this;
    }

    public IAMCoreBuilder withDefaultTokenCache() {
        this.tokenCache = new TokenCacheImpl(model);
        return this;
    }

    public IAMCore build() {
        if (model == null) {
            throw new UnsupportedOperationException("Model not defined ! Initialize model first by IAMCoreBuilder.withDefaultModel()");
        }
        if (authorizationCodeCache == null) {
            authorizationCodeCache = new AuthorizationCodeCacheImpl(20L, TimeUnit.MINUTES);
        }
        if (tokenCache == null) {
            tokenCache = new TokenCacheImpl(model);
        }
        if (persistenceService == null) {
            persistenceService = new InMemoryPersistenceServiceImpl();
        }
        cacheCleanupScheduler = new CacheCleanupSchedulerImpl(10L, TimeUnit.MINUTES, authorizationCodeCache, tokenCache);
        cacheCleanupScheduler.start();
        clientService = new ClientServiceImpl(model, tokenCache, authorizationCodeCache);
        resourceServerService = new ResourceServerServiceImpl(model, tokenCache);
        clientManagementService = new ClientManagementServiceImpl(model);
        organizationManagerService = new OrganizationManagerServiceImpl(model);
        projectManagerService = new ProjectManagerServiceImpl(model);
        userManagerService = new UserManagerServiceImpl(model);
        return new IAMCore();
    }

    public static IAMCoreBuilder builder() {
        return new IAMCoreBuilder();
    }

    /**
     * Central interface to get IAM-core essential services.
     */
    public class IAMCore implements AutoCloseable {

        public Model getModel() {
            return model;
        }

        public PersistenceService getPersistenceService() {
            return persistenceService;
        }

        public ClientService getClientService() {
            return clientService;
        }

        public ResourceServerService getResourceServerService() {
            return resourceServerService;
        }

        public ClientManagementService getClientManagementService() {
            return clientManagementService;
        }

        public OrganizationManagerService getOrganizationManagerService() {
            return organizationManagerService;
        }

        public ProjectManagerService getProjectManagerService() {
            return projectManagerService;
        }

        public UserManagerService getUserManagerService() {
            return userManagerService;
        }

        @Override
        public void close() throws Exception {
            cacheCleanupScheduler.close();
        }
    }

}
