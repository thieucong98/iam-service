package itx.iamservice.core.services.impl.admin;

import itx.iamservice.core.model.Organization;
import itx.iamservice.core.model.OrganizationId;
import itx.iamservice.core.model.PKIException;
import itx.iamservice.core.model.Permission;
import itx.iamservice.core.model.PermissionId;
import itx.iamservice.core.model.Project;
import itx.iamservice.core.model.ProjectId;
import itx.iamservice.core.model.ProjectImpl;
import itx.iamservice.core.model.Role;
import itx.iamservice.core.model.RoleId;
import itx.iamservice.core.model.RoleImpl;
import itx.iamservice.core.model.User;
import itx.iamservice.core.services.admin.ProjectManagerService;
import itx.iamservice.core.services.caches.ModelCache;
import itx.iamservice.core.services.dto.CreateProjectRequest;
import itx.iamservice.core.services.dto.CreateRoleRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class ProjectManagerServiceImpl implements ProjectManagerService {

    private final ModelCache modelCache;

    public ProjectManagerServiceImpl(ModelCache modelCache) {
        this.modelCache = modelCache;
    }

    @Override
    public Optional<ProjectId> create(OrganizationId id, CreateProjectRequest request) throws PKIException {
        Optional<Organization> organizationOptional = modelCache.getOrganization(id);
        Optional<Project> projectOptional  = modelCache.getProject(id, request.getId());
        if (organizationOptional.isPresent() && projectOptional.isEmpty()) {
            Project project = new ProjectImpl(request.getId(),
                    request.getName(), id, organizationOptional.get().getPrivateKey(), request.getAudience());
            modelCache.add(id, project);
            return Optional.of(request.getId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Project> getAll(OrganizationId id) {
        return modelCache.getProjects(id);
    }

    @Override
    public Collection<User> getUsers(OrganizationId id, ProjectId projectId) {
        return modelCache.getUsers(id, projectId);
    }

    @Override
    public Optional<Project> get(OrganizationId id, ProjectId projectId) {
        return getProject(id, projectId);
    }

    @Override
    public boolean remove(OrganizationId id, ProjectId projectId) {
        return modelCache.remove(id, projectId);
    }

    @Override
    public boolean addRole(OrganizationId id, ProjectId projectId, Role role) {
        return modelCache.add(id, projectId, role);
    }

    @Override
    public Optional<RoleId> addRole(OrganizationId id, ProjectId projectId, CreateRoleRequest request) {
        Optional<Role> roleOptional = modelCache.getRole(id, projectId, request.getId());
        if (roleOptional.isEmpty()) {
            Role role = new RoleImpl(request.getId(), request.getName());
            if (modelCache.add(id, projectId, role)) {
                return Optional.of(request.getId());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeRole(OrganizationId id, ProjectId projectId, RoleId roleId) {
        return modelCache.remove(id,  projectId, roleId);
    }

    @Override
    public Collection<Role> getRoles(OrganizationId id, ProjectId projectId) {
        return modelCache.getRoles(id, projectId);
    }

    @Override
    public void addPermission(OrganizationId id, ProjectId projectId, Permission permission) {
        Optional<Project> project = getProject(id, projectId);
        if (project.isPresent()) {
            project.get().addPermission(permission);
        }
    }

    @Override
    public Collection<Permission> getPermissions(OrganizationId id, ProjectId projectId) {
        Optional<Project> project = getProject(id, projectId);
        if (project.isPresent()) {
            return project.get().getPermissions();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean removePermission(OrganizationId id, ProjectId projectId, PermissionId permissionId) {
        Optional<Project> project = getProject(id, projectId);
        if (project.isPresent()) {
            return project.get().removePermission(permissionId);
        }
        return false;
    }

    @Override
    public boolean addPermissionToRole(OrganizationId id, ProjectId projectId, RoleId roleId, PermissionId permissionId) {
        return modelCache.addPermissionToRole(id, projectId, roleId, permissionId);
    }

    @Override
    public boolean removePermissionFromRole(OrganizationId id, ProjectId projectId, RoleId roleId, PermissionId permissionId) {
        return modelCache.removePermissionFromRole(id, projectId, roleId, permissionId);
    }

    private Optional<Project> getProject(OrganizationId id, ProjectId projectId) {
        return modelCache.getProject(id, projectId);
    }

}
