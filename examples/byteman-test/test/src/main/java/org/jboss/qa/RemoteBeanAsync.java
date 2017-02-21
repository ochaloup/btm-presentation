package org.jboss.qa;

import java.util.concurrent.Future;

import javax.ejb.Remote;

@Remote
public interface RemoteBeanAsync {
  void call();
  Future<String> call(String param);
  Future<String> callSynchronized(String param);
  Future<String> callAndReturn();
}
