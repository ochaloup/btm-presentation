package org.jboss.qa;

import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

@Stateless
@Asynchronous
public class SLSBeanAsync implements RemoteBeanAsync {
  private static final Logger log = Logger.getLogger(SLSBeanAsync.class);
  
  @EJB
  SingletonStorageRemote singletonStorage;
  
  @Override
  public void call() {
    log.info("Calling call()");    
  }

  @Override
  public Future<String> call(String param) {
    log.info("Calling call(String)");
    singletonStorage.addToStringStorage(param);
    return new AsyncResult<String>(param);
  }
  
  @Override
  public Future<String> callSynchronized(String param) {
    singletonStorage.addToStringStorage(param);
    log.info("Calling call(String)");
    return new AsyncResult<String>(param);
  }

  @Override
  public Future<String> callAndReturn() {
    log.info("Calling callAndReturn()");
    return new AsyncResult<String>("callAndReturn");
  }

}
