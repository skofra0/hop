package org.apache.hop.core;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import no.deem.core.metadata.app.Application;
import no.deem.core.metadata.app.WrappedPath;
import no.deem.core.resources.FileLocatorService;
import no.deem.core.utils.Slf4j;
import no.deem.core.version.SemanticVersion;
import no.deem.core.version.Versions;

public final class LogbackInitializer {

  private static final String HOME_FOLDER = "HOP_CLIENT";
  private static final Level ROOT_LEVEL = Level.ERROR;
  private static final String LOG_PATTERN = "%date{yyyy-MM-dd HH:mm:ss} - %-5level - %msg%n%rEx";
  private static final String CONSOLE_PATTERN = "%date{yyyy-MM-dd HH:mm:ss} - %-5level - %msg%n%rEx";

  private Map<String, String> loggerProperties = Map.of();
  private WrappedPath logPath;
  private boolean debug = false;

  public LogbackInitializer() {
    SemanticVersion version = Versions.getSemanticVersion("deem-integrator");
    WrappedPath basePath = FileLocatorService.getApplicationRoot();
    Application.consoleOut("Starting " + version);
    if (basePath.toString().endsWith("assemblies\\static\\src\\main\\resources")) {
      debug = true;
      basePath = WrappedPath.ofNullable(System.getenv(HOME_FOLDER));
    }
    logPath = basePath.getPath("logs");
    loggerProperties = new HashMap<>();
    loggerProperties.put("version", String.valueOf(version.getVersionAsOneNumber()));
  }

  public void init() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    Map<String, String> existingProperties = context.getCopyOfPropertyMap();
    context.reset();

    for (Map.Entry<String, String> entry : existingProperties.entrySet()) {
      context.putProperty(entry.getKey(), entry.getValue());
    }
    for (Map.Entry<String, String> entry : loggerProperties.entrySet()) {
      context.putProperty(entry.getKey(), entry.getValue());
    }
    ContextInitializer ci = new ContextInitializer(context);
    try {
      ci.autoConfig();
    } catch (JoranException e) {
      throw new LogbackInitializerException("Unable to re-configure logback", e);
    }

    addAppenders(context);
    setRootLogLevel(context, ROOT_LEVEL);
    context.getLogger("com.microsoft.sqlserver.jdbc.internals.TDS.TOKEN").setLevel(Level.ERROR);
    Slf4j.setPerformedConfig();

    org.slf4j.Logger logger = Slf4j.logger();
    logger.info("Logging configured - {} {}", debug ? " debug" : "", logPath);
  }

  public static class LogbackInitializerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public LogbackInitializerException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

  private void addAppenders(LoggerContext logCtx) {
    addConsoleAppender(logCtx, CONSOLE_PATTERN, Level.INFO);
    addLogAppender(logCtx, "error", Level.ERROR, LOG_PATTERN, 1, 7, 2, true, Level.ERROR);
    // addLogAppender(logCtx, "integrator", Level.DEBUG, LOG_PATTERN, 1, 7, 10, true, Level.INFO)
  }

  private void setRootLogLevel(LoggerContext logCtx, final Level newLevel) {
    var rootLogger = logCtx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    if (rootLogger != null) {
      rootLogger.setLevel(newLevel);
    }
  }

  private void addLogAppender(LoggerContext logCtx, String id, Level info, String pattern, int minIndex, int maxIndex, int maxMB, boolean addToRoot, Level filter) {
    if (logCtx.exists(id) == null) {
      String path = logPath.getPath(id).toString();

      PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
      logEncoder.setContext(logCtx);
      logEncoder.setPattern(pattern);
      logEncoder.start();

      RollingFileAppender<ILoggingEvent> logFileAppender = new RollingFileAppender<>();
      logFileAppender.setContext(logCtx);
      logFileAppender.setName(id);
      logFileAppender.setEncoder(logEncoder);
      logFileAppender.setAppend(true);
      logFileAppender.setPrudent(true);
      logFileAppender.setFile(path + "_.log");

      TimeBasedRollingPolicy<?> logFilePolicy = new TimeBasedRollingPolicy<>();
      logFilePolicy.setContext(logCtx);
      logFilePolicy.setParent(logFileAppender);
      logFilePolicy.setFileNamePattern(path + "_%d{yyyy-MM-dd}.log");
      logFilePolicy.setMaxHistory(maxIndex);
      logFilePolicy.setTotalSizeCap(new FileSize(maxMB * FileSize.MB_COEFFICIENT * maxIndex));
      logFilePolicy.setCleanHistoryOnStart(true);
      logFilePolicy.start();
      logFileAppender.setRollingPolicy(logFilePolicy);

      if (filter != null) {
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel(filter.toString());
        thresholdFilter.start();
        logFileAppender.addFilter(thresholdFilter);
      }

      logFileAppender.start();

      if (addToRoot) {
        Logger log = logCtx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        log.addAppender(logFileAppender);
      } else {
        Logger log = logCtx.getLogger(id);
        log.setAdditive(false);
        log.setLevel(info);
        log.addAppender(logFileAppender);
      }
    }
  }

  private void addConsoleAppender(LoggerContext logCtx, String pattern, Level filter) {
    String id = "STDOUT";
    if (logCtx.exists(id) == null) {
      PatternLayoutEncoder logEncoder = new PatternLayoutEncoder();
      logEncoder.setContext(logCtx);
      logEncoder.setPattern(pattern);
      logEncoder.start();

      ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
      consoleAppender.setContext(logCtx);
      consoleAppender.setName(id);
      consoleAppender.setEncoder(logEncoder);

      if (filter != null) {
        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel(filter.toString());
        thresholdFilter.start();
        consoleAppender.addFilter(thresholdFilter);
      }
      consoleAppender.start();

      Logger log = logCtx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
      log.setLevel(ROOT_LEVEL);
      log.addAppender(consoleAppender);
    }
  }  
}
