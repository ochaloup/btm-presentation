package org.jboss.qa.byteman;

import org.jboss.byteman.rule.Rule;

/**
 * Own byteman helper class.
 * 
 * E.g. see here:
 * http://www.mastertheboss.com/byteman/byteman-advanced-tutorial
 */
public class BytemanTestHelper extends org.jboss.byteman.contrib.dtest.BytemanTestHelper {

  public BytemanTestHelper(Rule rule) throws Exception {
    super(rule);
  }

  public String helperPleaseSayHelloForMe() {
    String hello = "HELLO";
    debug(BytemanTestHelper.class.getName() + ": " + hello);
    return hello;
  }
}
