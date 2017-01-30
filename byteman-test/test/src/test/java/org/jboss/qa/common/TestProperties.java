package org.jboss.qa.common;

public class TestProperties {
  public static final String JBOSS_CONTAINER_MANUAL = "jboss-manual";
  public static final String JBOSS_CONTAINER_AUTO = "jboss-auto";
  
  public static final String JBOSS_ADDRESS = System.getProperty("jboss.address", "localhost");
  
  public static final int BYTEMAN_PORT = Integer.getInteger("byteman.port.number", 9091);
  public static final int BYTEMAN_RMI_REGISTRY_PORT = Integer.getInteger("byteman.rmi.registry.port.number", 1199);
}
