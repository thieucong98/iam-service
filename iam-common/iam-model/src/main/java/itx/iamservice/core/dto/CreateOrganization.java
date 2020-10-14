package itx.iamservice.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateOrganization {

    private final String id;
    private final String name;

    @JsonCreator
    public CreateOrganization(@JsonProperty("id") String id,
                              @JsonProperty("name") String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
