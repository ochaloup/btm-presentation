package org.jboss.qa.auto;

import java.rmi.RemoteException;
import java.util.concurrent.Future;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.byteman.agent.submit.Submit;
import org.jboss.byteman.contrib.dtest.Instrumentor;
import org.jboss.logging.Logger;
import org.jboss.qa.RemoteBean;
import org.jboss.qa.RemoteBeanAsync;
import org.jboss.qa.SLSBean;
import org.jboss.qa.SLSBeanAsync;
import org.jboss.qa.SingletonStorageRemote;
import org.jboss.qa.common.TestProperties;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BytemanTestCase {
  private final Logger log = Logger.getLogger(BytemanTestCase.class);
  private static final String DEPLOYMENT_NAME = "bytemantest";
  
  protected static Instrumentor instrumentor = null;
  protected static Submit instrumentorSubmit = null;
  
  @EJB
  RemoteBeanAsync asyncBean;
  
  @EJB
  RemoteBean syncBean;
  
  @Inject
  SingletonStorageRemote singletonStorage;
  
  @Deployment(name = DEPLOYMENT_NAME)
  public static JavaArchive createDeployment() {
    return ShrinkWrap.create(JavaArchive.class, DEPLOYMENT_NAME + ".jar")
        .addPackage(RemoteBean.class.getPackage())
        .addPackage(TestProperties.class.getPackage());
        // .addAsManifestResource(new StringAsset("Dependencies: org.jboss.jts\n"), "MANIFEST.MF");
  }
  
  @Before
  public void beforeClass() throws RemoteException {
    if(instrumentor == null) {
      instrumentorSubmit = new Submit(TestProperties.JBOSS_ADDRESS, TestProperties.BYTEMAN_PORT);
      instrumentor = new Instrumentor(instrumentorSubmit, TestProperties.BYTEMAN_RMI_REGISTRY_PORT);
    }
    
    // cleaning singleton
    singletonStorage.clearStringStorage();
  }
  
  @After
  public void tearDown() {
    try {
      instrumentor.removeAllInstrumentation();
      instrumentor.removeLocalState();
    } catch (Exception e) {
      log.warn("Instrumentator fails with removing instrumentations", e);
    }
  }
  
  @Test
  public void call() {
    syncBean.call();
    asyncBean.call();
  }
  
  @Test
  public void callException() throws Exception {
    instrumentor.injectFault(SLSBean.class, "call", IllegalArgumentException.class, new Object[]{});
    try {
      syncBean.call();
    } catch (EJBException ejbe) {
      if(ejbe.getCause() instanceof IllegalArgumentException) {
        return;
      }
    }
    Assert.fail("Expected IllegalArgument exception being thrown");
  }
  
  @Test
  public void callInvoke() throws Exception {
    String injectCallString = "hello";
    instrumentor.injectOnCall(SLSBean.class, "call()", "$0.call(\"" + injectCallString + "\")");
    syncBean.call();
    Assert.assertEquals("Expecting that byteman propagated call to storage contains " + injectCallString, injectCallString, singletonStorage.getStringStorage());
    
  }
  
  @Test
  @Ignore
  public void bytemanCrash() throws Exception {
    instrumentor.crashAtMethodEntry(SLSBean.class, "call");
    syncBean.call();
  }

  @Test
  public void waitFor() throws Exception {
    String waitForName = "slsbeanwait";
    String waitScriptString = "RULE wait for call \n" +
        "CLASS " + SLSBeanAsync.class.getName() + "\n" +
        "METHOD callAndReturn \n" +
        // "HELPER org.jboss.byteman.contrib.dtest.BytemanTestHelper \n " +
        // "AT ENTRY \n" +
        "BIND NOTHING \n" +
        "IF TRUE \n" +
        "DO waitFor(\"" + waitForName + "\") \n" +
        "ENDRULE \n";
    instrumentor.installScript("waitForSyncEJBCall", waitScriptString);
    
    String awakeScriptString= "RULE awake sleeping one \n" +
        "CLASS " + SLSBean.class.getName() + "\n" +
        "METHOD call \n" +
        // "HELPER org.jboss.byteman.contrib.dtest.BytemanTestHelper \n " +
        // "AT ENTRY \n" +
        "BIND NOTHING \n" +
        "IF TRUE \n" +
        "DO signalWake(\"" + waitForName + "\", true) \n" +
        "ENDRULE \n";
    instrumentor.installScript("awakeSleepingAsyncBean", awakeScriptString);
    
    Future<String> future = asyncBean.callAndReturn();
    log.info("Waiting for 3 seconds");
    Thread.sleep(3000);
    Assert.assertFalse("Expecting that future object is not done byteman script stopped it", future.isDone());
    
    syncBean.call();
    Thread.sleep(1000);
    Assert.assertTrue("Expecting that future is returned as byteman script for awaken was already called", future.isDone());
  }
  
  @Test
  public void rendezvous() throws Exception {
    String rendezvousName = "slsbean-rendezvous";
    String createRendezvous = "RULE create rendezvous \n" +
        "CLASS " + SLSBeanAsync.class.getName() + "\n" +
        "METHOD <init> \n" +
        "BIND NOTHING \n" +
        "IF createRendezvous(\""+ rendezvousName + "\", 3) \n" +
        "DO traceln(\"Creating rendezvous " + rendezvousName + "\") \n" +
        "ENDRULE \n";
    instrumentor.installScript("createRendezvous", createRendezvous);
    String hitRendezvous = "RULE hit rendezvous \n" +
        "CLASS " + SLSBeanAsync.class.getName() + "\n" +
        "METHOD callSynchronized(java.lang.String) \n" +
        "BIND NOTHING \n" +
        "IF true \n" +
        "DO rendezvous(\"" + rendezvousName + "\") \n" +
        "ENDRULE \n";
    instrumentor.installScript("hitRendezvous", hitRendezvous);
    
    Future<String> future1 = asyncBean.callSynchronized("1");
    Future<String> future2 = asyncBean.callSynchronized("2");
    Assert.assertFalse("First async call should wait on rendezvous", future1.isDone());
    Assert.assertFalse("Second async call should wait on rendezvous", future2.isDone());
    
    Future<String> future3 = asyncBean.callSynchronized("3");
    Thread.sleep(1000);
    Assert.assertTrue("Third async call should be freed from rendezvous", future3.isDone());
    Assert.assertEquals("Rendezvous should be released and all data added in singleton storage "
        + "the length of the saved data is not correct", 3, singletonStorage.getStringStorage().length());
  }
  
  /**
   * Using helper to do some action. In our case just simple one but it shows how the helper is defined
   * in script. 
   */
  @Test
  public void helperUsage() throws Exception {
    log.info("method helperUsage");
      
    String helperUsageScript = "RULE helper_usage_script\n" +
            "CLASS " + SLSBean.class.getName() + "\n" + 
            "METHOD callAndReturn\n" + 
            "HELPER org.jboss.qa.byteman.BytemanTestHelper\n" +
            "AT EXIT\n" + 
            "IF true\n" +
            "DO RETURN helperPleaseSayHelloForMe()\n" +
            "ENDRULE\n";
    instrumentor.installScript("helper_usage_script", helperUsageScript);
    
    String returnetString = syncBean.callAndReturn();
    
    Assert.assertEquals("HELLO", returnetString);
  }
}
