package org.jboss.qa;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class SingletonStorage implements SingletonStorageRemote {
  private String stringStorage = "";

  public String getStringStorage() {
    return stringStorage;
  }
  
  @Lock(LockType.WRITE)
  public void addToStringStorage(String addition) {
    stringStorage += addition;
  }

  public void clearStringStorage() {
    stringStorage = "";
  }
  
  
}
