package com.wao.common.gateway.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import net.logstash.logback.appender.LogstashSocketAppender;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;

@Configuration
public class LoggingConfiguration {

	private final Logger log = LoggerFactory.getLogger(LoggingConfiguration.class);

	private LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

	@Value("${spring.application.name}")
	private String appName;

	@Value("${server.port}")
	private String serverPort;

	@Value("${eureka.instance.instanceId}")
	private String instanceId;

	@Inject
	private GatewayProperties gatewayProperties;

	@PostConstruct
	private void init() {
		if (gatewayProperties.getLogging().getLogstash().isEnabled()) {
			addLogstashAppender();
		}
	}

	public void addLogstashAppender() {
		log.info("Initializing Logstash logging");

		LogstashSocketAppender logstashAppender = new LogstashSocketAppender();
		logstashAppender.setName("LOGSTASH");
		logstashAppender.setContext(context);
		String customFields = "{\"app_name\":\"" + appName + "\",\"app_port\":\"" + serverPort + "\","
				+ "\"instance_id\":\"" + instanceId + "\"}";

		// Set the Logstash appender config from gateway properties
		logstashAppender.setSyslogHost(gatewayProperties.getLogging().getLogstash().getHost());
		logstashAppender.setPort(gatewayProperties.getLogging().getLogstash().getPort());
		logstashAppender.setCustomFields(customFields);

		// Limit the maximum length of the forwarded stacktrace so that it won't
		// exceed the 8KB UDP limit of logstash
		ShortenedThrowableConverter throwableConverter = new ShortenedThrowableConverter();
		throwableConverter.setMaxLength(7500);
		throwableConverter.setRootCauseFirst(true);
		logstashAppender.setThrowableConverter(throwableConverter);

		logstashAppender.start();

		// Wrap the appender in an Async appender for performance
		AsyncAppender asyncLogstashAppender = new AsyncAppender();
		asyncLogstashAppender.setContext(context);
		asyncLogstashAppender.setName("ASYNC_LOGSTASH");
		asyncLogstashAppender.setQueueSize(gatewayProperties.getLogging().getLogstash().getQueueSize());
		asyncLogstashAppender.addAppender(logstashAppender);
		asyncLogstashAppender.start();

		context.getLogger("ROOT").addAppender(asyncLogstashAppender);
	}
}
