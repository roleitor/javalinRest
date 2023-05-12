package com.riadsoft.rest;

import com.riadsoft.rest.user.UserController;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.BearerAuth;
import io.javalin.openapi.SecurityScheme;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.SecurityConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import org.jetbrains.annotations.NotNull;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String VALID_TOKEN = "your_valid_token";

    public static void main(String[] args) {

        var app = Javalin.create(config -> {

            SecurityConfiguration security = new SecurityConfiguration();
            security.withSecurityScheme("bearerAuth",new BearerAuth());

            OpenApiConfiguration openApiConfiguration = new OpenApiConfiguration();
            openApiConfiguration.getInfo().setTitle("Javalin OpenAPI example");
            openApiConfiguration.setSecurity(security);
            config.plugins.register(new OpenApiPlugin(openApiConfiguration));
            config.plugins.register(new SwaggerPlugin(new SwaggerConfiguration()));
            config.plugins.register(new ReDocPlugin(new ReDocConfiguration()));
        });

        app.before("api/rest/*",Main::tokenAuthFilter);
        app.routes(() -> {
            path("api/rest", () -> {
                get("users",UserController::getAll);
                post(UserController::create);
                path("{userId}", () -> {
                    get(UserController::getOne);
                    patch(UserController::update);
                    delete(UserController::delete);
                });
            });
        }).start(7002);

        System.out.println("Check out ReDoc docs at http://localhost:7002/redoc");
        System.out.println("Check out Swagger UI docs at http://localhost:7002/swagger-ui");
    }
    private static void tokenAuthFilter(Context ctx) throws Exception {
        String authorizationHeader = ctx.header(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            throw new UnauthorizedResponse("Unauthorized");
        }

        String token = authorizationHeader.substring(TOKEN_PREFIX.length());
        if (!isValidToken(token)) {
            throw new UnauthorizedResponse("Unauthorized");
        }
    }

    private static boolean isValidToken(String token) {
        return token.equals(VALID_TOKEN);
    }

}
