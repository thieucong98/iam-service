package itx.iamservice.core.services;

import itx.iamservice.core.model.Organization;
import itx.iamservice.core.model.User;
import itx.iamservice.core.model.UserId;
import itx.iamservice.core.model.OrganizationId;
import itx.iamservice.core.model.Project;
import itx.iamservice.core.model.ProjectId;
import itx.iamservice.core.dto.IntrospectRequest;
import itx.iamservice.core.dto.IntrospectResponse;
import itx.iamservice.core.services.dto.UserInfo;
import itx.iamservice.core.model.JWToken;
import itx.iamservice.core.services.dto.ProjectInfo;

import java.security.cert.CertificateEncodingException;
import java.util.Optional;

/**
 *  Service providing verification of {@link JWToken} instances.
 *  This service is intended to be used mainly by OAuth2 "resource server" roles.
 *  @see <a href="https://tools.ietf.org/html/rfc6749#section-1.1">OAuth2 roles</a>
 */
public interface ResourceServerService {

    /**
     * Verify if JWT token is valid. Time stamps and signature is verified.
     * @param organizationId {@link OrganizationId} unique organization ID.
     * @param projectId {@link ProjectId} unique project ID.
     * @param request {@link IntrospectRequest} to verify.
     * @return {@link IntrospectResponse} active=true if provided {@link JWToken} is valid, active=false otherwise.
     */
    IntrospectResponse introspect(OrganizationId organizationId, ProjectId projectId, IntrospectRequest request);

    /**
     * Get public {@link Project} related information. This information contains X509 certificates of organization and project.
     * @param organizationId {@link OrganizationId} - unique id of the organization.
     * @param projectId {@link ProjectId} - unique id of the project.
     * @return Optional of {@link ProjectInfo} instance if project and organization exists, empty otherwise.
     * @throws CertificateEncodingException in case certificate serialization to base64 string fails.
     */
    Optional<ProjectInfo> getProjectInfo(OrganizationId organizationId, ProjectId projectId) throws CertificateEncodingException;

    /**
     * Get public {@link User} related information. This information contains X509 of user.
     * @param organizationId {@link OrganizationId} - unique id of the organization.
     * @param projectId {@link ProjectId} - unique id of the project.
     * @param userId {@link UserId} - unique id of the user.
     * @return Optional of {@link UserInfo} instance if project, organization and user exists, empty otherwise.
     * @throws CertificateEncodingException in case certificate serialization to base64 string fails.
     */
    Optional<UserInfo> getUserInfo(OrganizationId organizationId, ProjectId projectId, UserId userId) throws CertificateEncodingException;

    /**
     * Get {@link Project} by {@link OrganizationId} and {@link ProjectId}.
     * @param organizationId - unique id of the organization.
     * @param projectId - unique id of the project.
     * @return Optional of {@link Project} instance if project and organization exists, empty otherwise.
     */
    Optional<Project> getProject(OrganizationId organizationId, ProjectId projectId);

    /**
     * Get {@link User} by {@link OrganizationId}, {@link ProjectId} and {@link UserId}.
     * @param organizationId - unique id of the organization.
     * @param projectId - unique id of the project.
     * @param userId - unique id of the user.
     * @return Optional of {@link User} instance if project, organization and user exists, empty otherwise.
     */
    Optional<User> getUser(OrganizationId organizationId, ProjectId projectId, UserId userId);

    /**
     * Get {@link Organization} by {@link OrganizationId}.
     * @param organizationId - unique id of the organization.
     * @return Optional of {@link Organization} instance if organization exists, empty otherwise.
     */
    Optional<Organization> getOrganization(OrganizationId organizationId);

}
