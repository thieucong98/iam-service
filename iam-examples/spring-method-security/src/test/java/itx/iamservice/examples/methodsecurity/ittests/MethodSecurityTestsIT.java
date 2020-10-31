package itx.iamservice.examples.methodsecurity.ittests;


import itx.iamservice.core.dto.CreateRole;
import itx.iamservice.core.dto.CreateUser;
import itx.iamservice.core.dto.PermissionInfo;
import itx.iamservice.core.model.ClientId;
import itx.iamservice.core.model.OrganizationId;
import itx.iamservice.core.model.ProjectId;
import itx.iamservice.core.model.RoleId;
import itx.iamservice.core.model.UserId;
import itx.iamservice.core.model.utils.ModelUtils;
import itx.iamservice.core.services.dto.SetupOrganizationRequest;
import itx.iamservice.core.services.dto.SetupOrganizationResponse;
import itx.iamservice.core.services.dto.TokenResponse;
import itx.iamservice.examples.methodsecurity.dto.ServerData;
import itx.iamservice.examples.methodsecurity.dto.SystemInfo;
import itx.iamservice.serviceclient.IAMAuthorizerClient;
import itx.iamservice.serviceclient.IAMServiceClientBuilder;
import itx.iamservice.serviceclient.IAMServiceManagerClient;
import itx.iamservice.serviceclient.IAMServiceProjectManagerClient;
import itx.iamservice.serviceclient.IAMServiceUserManagerClient;
import itx.iamservice.serviceclient.impl.AuthenticationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MethodSecurityTestsIT {

    private static final Logger LOG = LoggerFactory.getLogger(MethodSecurityTestsIT.class);

    private static TestRestTemplate restTemplate;
    private static int iamServerPort;
    private static int resourceServerPort;
    private static TokenResponse iamAdminTokens;
    private static TokenResponse appAdminTokens;
    private static TokenResponse appReaderTokens;
    private static TokenResponse appWriterTokens;

    private static OrganizationId organizationId;
    private static ProjectId projectId;
    private static UserId appAdminUserId;
    private static ClientId clientId;
    private static RoleId appUserRoleReader;
    private static RoleId appUserRoleWriter;
    private static Set<PermissionInfo> readerPermissions = Set.of(
            PermissionInfo.from("spring-method-security", "secure-data", "read")
    );
    private static Set<PermissionInfo> writerPermissions = Set.of(
            PermissionInfo.from("spring-method-security", "secure-data", "read"),
            PermissionInfo.from("spring-method-security", "secure-data", "write")
    );
    private static UserId appReaderUserId;
    private static UserId appWriterUserId;

    private static IAMServiceManagerClient iamServiceManagerClient;

    @BeforeAll
    public static void init() throws MalformedURLException {
        restTemplate = new TestRestTemplate();
        iamServerPort = 8080;
        resourceServerPort = 8082;
        organizationId = OrganizationId.from("it-testing-001");
        projectId = ProjectId.from("spring-method-security");
        appAdminUserId = UserId.from("user-001");
        clientId = ClientId.from("client-001");
        appUserRoleReader = RoleId.from("role-reader");
        appUserRoleWriter = RoleId.from("role-writer");
        appReaderUserId = UserId.from("bob-reader");
        appWriterUserId = UserId.from("alice-writer");
        URL baseUrl = new URL("http://localhost:" + iamServerPort);
        iamServiceManagerClient = IAMServiceClientBuilder.builder()
                .withBaseUrl(baseUrl)
                .withConnectionTimeout(60L, TimeUnit.SECONDS)
                .build();
    }

    @Test
    @Order(1)
    public void checkIamServerIsAlive() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + iamServerPort + "/actuator/info", String.class);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(2)
    public void checkResourceServerIsAlive() {
        ResponseEntity<SystemInfo> response = restTemplate.getForEntity(
                "http://localhost:" + resourceServerPort + "/services/public/info", SystemInfo.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void getIamAdminAccessTokens() throws AuthenticationException {
        iamAdminTokens = iamServiceManagerClient
                .getIAMAdminAuthorizerClient()
                .getAccessTokensOAuth2UsernamePassword("admin", "secret", ModelUtils.IAM_ADMIN_CLIENT_ID, "top-secret");
        assertNotNull(iamAdminTokens);
    }

    @Test
    @Order(4)
    public void createOrganizationProjectAndAdminUser() throws AuthenticationException {
        SetupOrganizationRequest setupOrganizationRequest = new SetupOrganizationRequest(organizationId.getId(), "IT Testing",
                projectId.getId(),  "Method Security Project",
                clientId.getId(), "top-secret", appAdminUserId.getId(),  "secret", "admin@email.com", Set.of("methodsecurity"));
        SetupOrganizationResponse setupOrganizationResponse = iamServiceManagerClient.setupOrganization(iamAdminTokens.getAccessToken(), setupOrganizationRequest);
        assertNotNull(setupOrganizationResponse);
    }

    @Test
    @Order(5)
    public void getAppAdminUserTokens() throws AuthenticationException {
        IAMAuthorizerClient iamAuthorizerClient = iamServiceManagerClient.getIAMAuthorizerClient(organizationId, projectId);
        appAdminTokens = iamAuthorizerClient.getAccessTokensOAuth2UsernamePassword(appAdminUserId.getId(), "secret", clientId, "top-secret");
        LOG.info("App admin: ACCESS_TOKEN {}", appAdminTokens.getAccessToken());
        assertNotNull(appAdminTokens);
    }

    @Test
    @Order(6)
    public void createOrdinaryAppUsers() throws AuthenticationException {
        IAMServiceProjectManagerClient iamServiceProject = iamServiceManagerClient.getIAMServiceProject(appAdminTokens.getAccessToken(), organizationId, projectId);
        CreateRole createReaderRole = new CreateRole(appUserRoleReader.getId(), "Reader Role", readerPermissions);
        iamServiceProject.createRole(createReaderRole);
        CreateRole createWriterRole = new CreateRole(appUserRoleWriter.getId(), "Writer Role", writerPermissions);
        iamServiceProject.createRole(createWriterRole);
        IAMServiceUserManagerClient iamServiceUserManagerClient = iamServiceManagerClient.getIAMServiceUserManagerClient(appAdminTokens.getAccessToken(), organizationId, projectId);
        CreateUser createReaderUser = new CreateUser(appReaderUserId.getId(), "", 3600L, 3600L, "", "789456");
        iamServiceUserManagerClient.createUser(createReaderUser);
        iamServiceUserManagerClient.addRoleToUser(appReaderUserId, appUserRoleReader);
        CreateUser createWriterUser = new CreateUser(appWriterUserId.getId(), "", 3600L, 3600L, "", "456789");
        iamServiceUserManagerClient.createUser(createWriterUser);
        iamServiceUserManagerClient.addRoleToUser(appWriterUserId, appUserRoleWriter);
    }

    @Test
    @Order(7)
    public void updateIamClientCache() {
        ResponseEntity<ServerData> response = restTemplate.getForEntity(
                "http://localhost:" + resourceServerPort + "/services/public/update-iam-client-cache", ServerData.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void getAppReaderTokens() throws AuthenticationException {
        IAMAuthorizerClient iamAuthorizerClient = iamServiceManagerClient.getIAMAuthorizerClient(organizationId, projectId);
        appReaderTokens = iamAuthorizerClient.getAccessTokensOAuth2UsernamePassword(appReaderUserId.getId(), "789456", clientId, "top-secret");
        LOG.info("App READER: ACCESS_TOKEN {}", appReaderTokens.getAccessToken());
        assertNotNull(appReaderTokens);
    }

    @Test
    @Order(9)
    public void getAppWriterTokens() throws AuthenticationException {
        IAMAuthorizerClient iamAuthorizerClient = iamServiceManagerClient.getIAMAuthorizerClient(organizationId, projectId);
        appWriterTokens = iamAuthorizerClient.getAccessTokensOAuth2UsernamePassword(appWriterUserId.getId(), "456789", clientId, "top-secret");
        LOG.info("App WRITER: ACCESS_TOKEN {}", appWriterTokens.getAccessToken());
        assertNotNull(appWriterTokens);
    }

    @Test
    @Order(10)
    public void testSecureAccessNoAccessTokens() {
        ResponseEntity<ServerData> response = restTemplate.getForEntity(
                "http://localhost:" + resourceServerPort + "/services/secure/data", ServerData.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ServerData serverData = new ServerData("update");
        response = restTemplate.postForEntity(
                "http://localhost:" + resourceServerPort + "/services/secure/data", serverData, ServerData.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(11)
    public void testSecureAccessInvalidIAMAdminTokens() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + iamAdminTokens.getAccessToken()));
        HttpEntity<ServerData> requestEntity = new HttpEntity(headers);
        ResponseEntity<ServerData> response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.GET, requestEntity, ServerData.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ServerData serverData = new ServerData("update");
        requestEntity = new HttpEntity(serverData, headers);
        response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.POST, requestEntity, ServerData.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(12)
    public void testSecureAccessInvalidIAppAdminTokens() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + appAdminTokens.getAccessToken()));
        HttpEntity<ServerData> requestEntity = new HttpEntity(headers);
        ResponseEntity<ServerData> response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.GET, requestEntity, ServerData.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ServerData serverData = new ServerData("update");
        requestEntity = new HttpEntity(serverData, headers);
        response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.POST, requestEntity, ServerData.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Order(13)
    public void testSecureAccessReaderUserTokensReadAccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + appReaderTokens.getAccessToken()));
        HttpEntity<ServerData> requestEntity = new HttpEntity(headers);
        ResponseEntity<ServerData> response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.GET, requestEntity, ServerData.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @Order(14)
    public void testSecureAccessReaderUserTokensWriteAccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + appReaderTokens.getAccessToken()));
        ServerData serverData = new ServerData("update");
        HttpEntity<ServerData> requestEntity = new HttpEntity(serverData, headers);
        ResponseEntity<ServerData> response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.POST, requestEntity, ServerData.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @Order(15)
    public void testSecureAccessWriterUserTokensReadAccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + appWriterTokens.getAccessToken()));
        HttpEntity<ServerData> requestEntity = new HttpEntity(headers);
        ResponseEntity<ServerData> response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.GET, requestEntity, ServerData.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
    }

    @Test
    @Order(16)
    public void testSecureAccessWriterUserTokensWriteAccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", List.of("Bearer " + appWriterTokens.getAccessToken()));
        ServerData serverData = new ServerData("update");
        HttpEntity<ServerData> requestEntity = new HttpEntity(serverData, headers);
        ResponseEntity<ServerData> response = restTemplate.exchange(
                "http://localhost:" + resourceServerPort + "/services/secure/data", HttpMethod.POST, requestEntity, ServerData.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("update", response.getBody().getData());
    }

}
