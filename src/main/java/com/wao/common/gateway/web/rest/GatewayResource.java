package com.wao.common.gateway.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.wao.common.gateway.web.rest.dto.RouteDTO;

/**
 * REST controller for managing Gateway configuration.
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayResource {

	private final Logger log = LoggerFactory.getLogger(GatewayResource.class);

	@Inject
	private RouteLocator routeLocator;

	@Inject
	private DiscoveryClient discoveryClient;

	/**
	 * GET /routes : get the active routes.
	 *
	 * @return the ResponseEntity with status 200 (OK) and with body the list of
	 *         routes
	 */
	@RequestMapping(value = "/routes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<List<RouteDTO>> activeRoutes() {
		List<Route> routes = routeLocator.getRoutes();
		List<RouteDTO> routeDTOs = new ArrayList<>();
		routes.forEach(route -> {
			RouteDTO routeDTO = new RouteDTO();
			routeDTO.setPath(route.getFullPath());
			routeDTO.setServiceId(route.getId());
			routeDTO.setServiceInstances(discoveryClient.getInstances(route.getId()));
			routeDTOs.add(routeDTO);
		});
		return new ResponseEntity<>(routeDTOs, HttpStatus.OK);
	}
}
