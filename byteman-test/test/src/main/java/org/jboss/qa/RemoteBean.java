package org.jboss.qa;

import javax.ejb.Remote;

@Remote
public interface RemoteBean {
  void call();
  String call(String param);
  String callAndReturn();
}
