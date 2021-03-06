package com.wao.common.gateway.config.apidoc;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.wao.common.gateway.config.Constants;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * Retrieves all registered microservices Swagger resources.
 */
@Component
@Primary
@Profile("!" + Constants.SPRING_PROFILE_NO_SWAGGER)
@ConditionalOnProperty(value = "gateway.swagger.enabled")
public class GatewaySwaggerResourcesProvider implements SwaggerResourcesProvider {

	private final Logger log = LoggerFactory.getLogger(GatewaySwaggerResourcesProvider.class);

	@Inject
	private RouteLocator routeLocator;

	@Inject
	private DiscoveryClient discoveryClient;

	@Override
	public List<SwaggerResource> get() {
		List<SwaggerResource> resources = new ArrayList<>();

		// Add the default swagger resource that correspond to the gateway's own
		// swagger doc
		resources.add(swaggerResource("default", "/v2/api-docs"));

		// Add the registered microservices swagger docs as additional swagger
		// resources
		List<Route> routes = routeLocator.getRoutes();
		routes.forEach(route -> {
			resources.add(swaggerResource(route.getId(), route.getFullPath().replace("**", "v2/api-docs")));
		});

		return resources;
	}

	private SwaggerResource swaggerResource(String name, String location) {
		SwaggerResource swaggerResource = new SwaggerResource();
		swaggerResource.setName(name);
		swaggerResource.setLocation(location);
		swaggerResource.setSwaggerVersion("2.0");
		return swaggerResource;
	}
}
