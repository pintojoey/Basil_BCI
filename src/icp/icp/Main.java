package icp;

import icp.application.classification.test.OptimizeMLP;
import icp.online.gui.MainFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Hlavni spousteci trida aplikace
 */
public class Main {

    private static void runGui() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
        new MainFrame();
    }

    private static void runOpt() {

    }

    private static void printHelp(PrintStream out) {
        out.println("Guess the number - A demo BCI application." );
        out.println("Call with parameters:" );

        out.println("-h   - print help," );
        out.println("-gui - run graphical user interface (GUI)," );
        out.println("-opt - run classifier optimization process," );
        out.println("<no parameters>  - print help and run GUI." );
    }


    public static void main(String[] args) {

        if (args == null || args.length != 1 ) {
            printHelp(System.out);
            runGui();
        } else if ("-h".equals(args[0])) {
            printHelp(System.out);
        } else if ("-gui".equals(args[0])) {
            runGui();
        } else if ("-opt".equals(args[0])) {
            try {
                OptimizeMLP.main(null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    }
}
