import javax.swing.SwingUtilities;

public class Z80EmulatorMain {
		
    public static void main(String[] args) {
		MainFrame mf = new MainFrame();
		
		mf.init();
		mf.createMenuBar();
		
		mf.setVisible(true);
		
		/*SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mf.setVisible(true);
            }
        });*/
    }
}