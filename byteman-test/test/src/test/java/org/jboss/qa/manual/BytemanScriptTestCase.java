package org.jboss.qa.manual;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.byteman.agent.submit.Submit;
import org.jboss.byteman.contrib.dtest.InstrumentedClass;
import org.jboss.byteman.contrib.dtest.Instrumentor;
import org.jboss.logging.Logger;
import org.jboss.qa.RemoteBean;
import org.jboss.qa.SLSBean;
import org.jboss.qa.common.TestProperties;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class BytemanScriptTestCase {
  private static final Logger log = Logger.getLogger(BytemanScriptTestCase.class);
  
  private static final String DEPLOYMENT_NAME = "byteman-script";

  @ArquillianResource
  protected ContainerController controller;
  @ArquillianResource
  protected Deployer deployer;

  // testable has to be set to false to have the deployment named by deployment name
  // managed is left to be true to arquillian do the deployment for us on its own
  @Deployment(name = DEPLOYMENT_NAME, testable = false)
  /* as default container is put being jboss-auto for this deployment would be connected
   to the jboss-manual we need to specify it explicitly */
  @TargetsContainer(TestProperties.JBOSS_CONTAINER_MANUAL)
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class, DEPLOYMENT_NAME + ".jar")
        .addPackage(RemoteBean.class.getPackage())
        .addPackage(TestProperties.class.getPackage());
  }
  
  public void startContainer(File scriptFile) throws IOException {
    String serverBytemanArgs = System.getProperty("jvm.args.byteman").trim();
    if (scriptFile != null) {
      // adding script at the start of the container
      serverBytemanArgs += ",script:" + scriptFile.getCanonicalPath();
    }
    // in case that someone would like debug the server
    String serverJvmDebugArguments = System.getProperty("jvm.args.debug", "").trim();
    // and let's create configuration
    Map<String, String> config = new Config().add("javaVmArguments", serverJvmDebugArguments + " " + serverBytemanArgs).map();
    // which is addad as parameter to start method
    controller.start(TestProperties.JBOSS_CONTAINER_MANUAL, config);
    log.infof("=== appserver %s started ===", TestProperties.JBOSS_CONTAINER_MANUAL);
  }
  
  @After
  public void stopContainer() {
    if(controller.isStarted(TestProperties.JBOSS_CONTAINER_MANUAL)) {
      controller.stop(TestProperties.JBOSS_CONTAINER_MANUAL);
    }
  }
  
  /**
   * Showing how to redirect the byteman script from instrumentator to a file where
   * the file is then used as btm script at the manual start of the container.
   */
  @Test
  public void redirectRulesToFile() throws Exception {
    log.info("method redirectRulesToFile");
    // creating instrumentator
    Submit instrumentorSubmit = new Submit(TestProperties.JBOSS_ADDRESS, TestProperties.BYTEMAN_PORT);
    Instrumentor instrumentor = new Instrumentor(instrumentorSubmit, TestProperties.BYTEMAN_RMI_REGISTRY_PORT);
    instrumentor.removeLocalState();
    // file where script will be put to
    File rulesFile = File.createTempFile("temporarybytemanscript.btm", "");
    rulesFile.deleteOnExit();
    instrumentor.setRedirectedSubmissionsFile(rulesFile);
    // instrument the class to be able to check the call order
    InstrumentedClass instrumentedClass = instrumentor.instrumentClass(SLSBean.class);
    
    startContainer(rulesFile);
    
    RemoteBean bb = lookupBean();
    bb.call();
    
    instrumentedClass.assertMethodCalled("call");
  }
  
  private RemoteBean lookupBean() throws NamingException {
    // Properties to creating the intial context is taken from jboss-ejb-client.properties
    final Properties jndiProperties = new Properties();
    jndiProperties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
    final Context ctx = new InitialContext(jndiProperties);
    String slsbeanLookupString = "ejb:/" + DEPLOYMENT_NAME + "//" + SLSBean.class.getSimpleName() + "!" + RemoteBean.class.getName();
    RemoteBean bb = (RemoteBean) ctx.lookup(slsbeanLookupString);
    return bb;
  }
}
