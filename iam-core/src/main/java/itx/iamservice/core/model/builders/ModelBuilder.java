package itx.iamservice.core.model.builders;

import itx.iamservice.core.model.Model;
import itx.iamservice.core.model.ModelId;
import itx.iamservice.core.model.ModelImpl;
import itx.iamservice.core.model.Organization;
import itx.iamservice.core.model.OrganizationId;
import itx.iamservice.core.model.OrganizationImpl;
import itx.iamservice.core.model.PKIException;

import java.util.UUID;

public final class ModelBuilder {

    private final Model model;

    public ModelBuilder(String name) {
        ModelId id = ModelId.from(UUID.randomUUID().toString());
        this.model = new ModelImpl(id, name);
    }

    public OrganizationBuilder addOrganization(String name) throws PKIException {
        OrganizationId id = OrganizationId.from(UUID.randomUUID().toString());
        Organization organization = new OrganizationImpl(id, name);
        this.model.add(organization);
        return new OrganizationBuilder(this, organization);
    }

    public Model build() {
        return model;
    }

    public static ModelBuilder builder(String name) {
        return new ModelBuilder(name);
    }

}
