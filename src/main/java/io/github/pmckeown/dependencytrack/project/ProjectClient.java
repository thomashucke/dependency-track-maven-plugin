package io.github.pmckeown.dependencytrack.project;

import io.github.pmckeown.dependencytrack.CommonConfig;
import io.github.pmckeown.dependencytrack.Response;
import kong.unirest.GenericType;
import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

import static io.github.pmckeown.dependencytrack.ResourceConstants.V1_PROJECT_UUID;
import static io.github.pmckeown.dependencytrack.ResourceConstants.V1_PROJECT_LOOKUP;
import static kong.unirest.Unirest.*;

/**
 * Client for getting Project details from Dependency Track
 *
 * @author Paul McKeown
 */
@Singleton
public class ProjectClient {

    private static final String X_API_KEY = "X-Api-Key";

    private CommonConfig commonConfig;

    @Inject
    public ProjectClient(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public Response<Project> getProject(String projectName, String projectVersion) {
        HttpResponse<Project> httpResponse = get(commonConfig.getDependencyTrackBaseUrl() + V1_PROJECT_LOOKUP)
            .queryString("name", projectName)
            .queryString("version", projectVersion)
            .header(X_API_KEY, commonConfig.getApiKey())
            .asObject(new GenericType<Project>() {
            });
        Optional<Project> body;
        if (httpResponse.isSuccess()) {
            body = Optional.of(httpResponse.getBody());
        } else {
            body = Optional.empty();
        }
        return new Response<>(httpResponse.getStatus(), httpResponse.getStatusText(), httpResponse.isSuccess(), body);
    }

    Response<Void> deleteProject(Project project) {
        HttpResponse<?> httpResponse = delete(commonConfig.getDependencyTrackBaseUrl() + V1_PROJECT_UUID)
                .routeParam("uuid", project.getUuid())
                .header(X_API_KEY, commonConfig.getApiKey())
                .asEmpty();

        return new Response<>(httpResponse.getStatus(), httpResponse.getStatusText(), httpResponse.isSuccess());
    }

    public Response<Void> patchProject(String uuid, ProjectInfo info) {
        HttpResponse<?> httpResponse = patch(commonConfig.getDependencyTrackBaseUrl() + V1_PROJECT_UUID)
                .routeParam("uuid", uuid)
                .header(X_API_KEY, commonConfig.getApiKey())
                .contentType("application/json")
                .body(info)
                .asEmpty();

        boolean isSuccess = httpResponse.isSuccess() || httpResponse.getStatus() == HttpStatus.NOT_MODIFIED;
        return new Response<>(httpResponse.getStatus(), httpResponse.getStatusText(), isSuccess);
    }
}
