package se.lnu.siq.s4rdm3x;

/**
 * Created by tohto on 2017-08-21.
 */
public class ConsoleReader implements Runnable {

    Thread m_thread;
    String m_input;

    public ConsoleReader() {
        m_thread = new Thread(this);
        m_thread.start();
    }

    public boolean hasInput() {
        return m_input != null;
    }

    public String popInput() {
        String ret = m_input;
        m_input = null;
        return ret;
    }

    public void run() {

        //Scanner inputReader = new Scanner(Sys.in);
        //Console inputReader = Sys.console();
        m_input = null;

        //if (inputReader == null) {
        //    Sys.out.println("No console...");
        //}

        while(true) {
            if (m_input == null) {
                //m_input = inputReader.nextLine();
                //m_input = inputReader.readLine();
                //m_input = C.io.nextLine();
                //m_input = guic.nextLine();
            }
        }
    }
}
