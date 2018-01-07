import olive.ui.ApplicationWindow;
import client.ClientApplication;

public class EntryPoint {

	public static void main(String[] args) {
		ClientApplication c = null;
		try {
			if(args.length >= 1) {
				String host = args[0];
				int port = 3023;
				if(args.length >= 2)
					try {
						port = Integer.parseInt(args[1]);
					} catch(NumberFormatException e) {
						System.out.println("Port argument must be an integer");
						return;
					}
				c = new ClientApplication(args[0], port);
			}
		} catch(NumberFormatException e) {
			System.out.println("Port argument must be an integer");
			return;
		} finally {
			if(c == null)
				c = new ClientApplication("localhost", 3023);
		}
		ApplicationWindow w = new ApplicationWindow(c);
		w.setOpacity(0.25f);
		w.init();
	}
	
}
