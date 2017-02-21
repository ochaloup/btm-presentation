Test for byteman and dtest library
==================================

Testing project of byteman dtest library. This is an example how the dtest library could be used.

Check the pom.xml, arquillian.xml and the sources of the test. 

For checking the test you can run

`export JBOSS_HOME=/path/to/wildfly`<br/>
`mvn test`

If you do want to run particular test then use something like

`mvn test -Dtest=BytemanScriptTestCase#helperUsage -Dno.auto.arq`

Important thing is to have defined no.auto.arq property for not run the test with profile where auto lauch of container is used.
That's because the test expects to run container on its own.

TODO: check https://github.com/cyron/byteman-framework and https://github.com/cyron/byteman-framework-extension
