package org.jboss.qa;

public class TryFinallyCheck 
{
    public static void main( String[] args )
    {
        try {
            String message = "before method printMessage is called";
            System.out.println(message);
            TryFinallyCheck tcc = new TryFinallyCheck();
            tcc.printMessage();
            System.out.println("after method printMessage is called");
        } finally {
            System.out.println("finally block of main method");
        }
    }

    private void printMessage() {
        System.out.println("private: inside ofthe printMessage method");
    }
}
