package org.jboss.qa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Hello world!
 *
 */
public class AppWaiting 
{
    public static void main( String[] args )
    {
    	try(BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
    		String readValue = "";
    		while(!readValue.equals("exit")) {
    			AppWaiting.write("Hello world!");
    			readValue = br.readLine();
    		}
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }
    
    private static void write(String message) {
    	System.out.println( message );
    }
}
