package org.jboss.qa;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

@Stateless
public class SLSBean implements RemoteBean {
  private static final Logger log = Logger.getLogger(SLSBean.class);
  
  @EJB
  SingletonStorageRemote singletonStorage;
  
  @Override
  public void call() {
    log.info("Calling call()");    
  }

  @Override
  public String call(String param) {
    log.info("Calling call(String)");
    singletonStorage.addToStringStorage(param);
    return param;
  }

  @Override
  public String callAndReturn() {
    log.info("Calling callAndReturn()");
    return "callAndReturn";
  }

}
