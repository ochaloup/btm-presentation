package org.jboss.qa;

public class TryCatchCheck 
{
    public static void main( String[] args )
    {
        try {
            String message = "before: printMessage";
            System.out.println(message);
            TryCatchCheck tcc = new TryCatchCheck();
            tcc.printMessage();
            System.out.println("after: printMessage");
        } catch (Throwable t) {
            System.out.println("catch block: " + t);
        }
    }

    private void printMessage() {
        System.out.println("private: printMessage");
    }
}
