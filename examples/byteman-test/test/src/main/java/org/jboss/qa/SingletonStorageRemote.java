package org.jboss.qa;

public interface SingletonStorageRemote {
  String getStringStorage();
  void addToStringStorage(String addition);
  void clearStringStorage();
}
