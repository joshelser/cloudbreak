package com.sequenceiq.it.cloudbreak.dto.database;

import java.util.List;
import java.util.stream.Collectors;

import com.sequenceiq.it.cloudbreak.Prototype;
import com.sequenceiq.it.cloudbreak.RedbeamsClient;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.DeletableRedbeamsTestDto;
import com.sequenceiq.it.cloudbreak.search.Searchable;
import com.sequenceiq.it.cloudbreak.util.ResponseUtil;
import com.sequenceiq.redbeams.api.endpoint.v4.database.DatabaseV4Endpoint;
import com.sequenceiq.redbeams.api.endpoint.v4.database.request.DatabaseV4Request;
import com.sequenceiq.redbeams.api.endpoint.v4.database.responses.DatabaseV4Response;

@Prototype
public class RedbeamsDatabaseTestDto extends DeletableRedbeamsTestDto<DatabaseV4Request, DatabaseV4Response, RedbeamsDatabaseTestDto, DatabaseV4Response>
        implements Searchable {

    public static final String DATABASE = "DATABASE";

    public RedbeamsDatabaseTestDto(TestContext testContext) {
        super(new DatabaseV4Request(), testContext);
    }

    @Override
    public RedbeamsDatabaseTestDto valid() {
        TestContext testContext = getTestContext();
        if (testContext == null) {
            throw new IllegalStateException("Cannot create valid instance, test context is not available");
        }
        String environmentCrn = testContext.getMicroserviceClient(RedbeamsClient.class).getEnvironmentCrn();
        return withName(getResourcePropertyProvider().getName(getCloudPlatform()))
                .withDescription(getResourcePropertyProvider().getDescription("database"))
                .withConnectionUserName("user")
                .withConnectionPassword("password")
                .withConnectionURL("jdbc:postgresql://somedb.com:5432/mydb")
                .withType("HIVE")
                .withEnvironmentCrn(environmentCrn);
    }

    public RedbeamsDatabaseTestDto withRequest(DatabaseV4Request request) {
        setRequest(request);
        return this;
    }

    public RedbeamsDatabaseTestDto withName(String name) {
        getRequest().setName(name);
        setName(name);
        return this;
    }

    public RedbeamsDatabaseTestDto withDescription(String description) {
        getRequest().setDescription(description);
        return this;
    }

    public RedbeamsDatabaseTestDto withConnectionPassword(String password) {
        getRequest().setConnectionPassword(password);
        return this;
    }

    public RedbeamsDatabaseTestDto withConnectionUserName(String username) {
        getRequest().setConnectionUserName(username);
        return this;
    }

    public RedbeamsDatabaseTestDto withConnectionURL(String connectionURL) {
        getRequest().setConnectionURL(connectionURL);
        return this;
    }

    public RedbeamsDatabaseTestDto withType(String type) {
        getRequest().setType(type);
        return this;
    }

    public RedbeamsDatabaseTestDto withEnvironmentCrn(String environmentCrn) {
        getRequest().setEnvironmentCrn(environmentCrn);
        return this;
    }

    @Override
    public List<DatabaseV4Response> getAll(RedbeamsClient client) {
        DatabaseV4Endpoint databaseV4Endpoint = client.getEndpoints().databaseV4Endpoint();
        return databaseV4Endpoint.list(client.getEnvironmentCrn()).getResponses().stream()
                .filter(s -> s.getName() != null)
                .collect(Collectors.toList());
    }

    @Override
    protected String name(DatabaseV4Response entity) {
        return entity.getName();
    }

    @Override
    public void delete(TestContext testContext, DatabaseV4Response entity, RedbeamsClient client) {
        try {
            client.getEndpoints().databaseV4Endpoint().deleteByName(client.getEnvironmentCrn(), entity.getName());
        } catch (Exception e) {
            LOGGER.warn("Something went wrong on {} purge. {}", entity.getName(), ResponseUtil.getErrorMessage(e), e);
        }
    }

    @Override
    public int order() {
        return 500;
    }

    @Override
    public String getSearchId() {
        return getName();
    }
}
