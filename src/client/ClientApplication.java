package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.DataOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

import olive.ui.LiveApplication;

public class ClientApplication extends LiveApplication{

	private final static int QUIT = 0, KEY_PRESS = 1, KEY_RELEASE = 2, MOUSE_PRESS = 3, MOUSE_RELEASE = 4, MOUSE_MOVE = 5, MOUSE_WHEEL = 6;
	
	private final static long MOVE_LIMIT = 16; //only allow ~60 move or drag inputs a second so the server isn't hammered (1000/60 = 16.6666...)
	
	private Socket socket = null;
	private DataOutputStream out = null;
	
	private long moveLimiter = 0;
	private boolean altDown = false;
	
	private ArrayList<Integer>heldKeys = new ArrayList<Integer>();
	
	public ClientApplication(String host, int port) {
		try {
			System.out.println("Attempting to connect with server " + host + " on port " + port + "...");
			socket = new Socket(host, port);
			out = new DataOutputStream(socket.getOutputStream());
		} catch(Exception e) {
			System.out.println("Error opening socket on " + port + " with " + host);
			System.exit(0);
		}
		System.out.println("Success!");
		System.out.println("Use the transparent window to feed input to the server");
		System.out.println("Press Alt+End to terminate the connection");
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
	}
	
	public void paint(Graphics g) {
		Rectangle rect = g.getClipBounds();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, rect.width, rect.height);
	}
	
	@Override
	public void run() {
		
	}
	
	public void post(int event, int key) {
		try {
			out.writeInt(event);
			out.writeInt(key);
			out.flush();
		} catch(Exception e) {
			System.out.println("Error on event " + event + " " + (char)key);
		}
	}
	
	public void post(int event, int x, int y) {
		try {
			out.writeInt(event);
			out.writeInt(x);
			out.writeInt(y);
			out.flush();
		} catch(Exception e) {
			System.out.println("Error on event " + event + " " + x + ", " + y);
			System.exit(0);
		}
	}
	
	public void quit() {
		try{
			//Tell the server to release any keys we're currently holding
			for(int i : heldKeys) {
				out.writeInt(KEY_RELEASE);
				out.writeInt(i);
			}
			out.writeInt(QUIT);
			out.flush();
			out.close();
		} catch(Exception e) {}
		System.exit(0);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		heldKeys.add(e.getKeyCode());
		if(e.getKeyCode() == KeyEvent.VK_ALT)
			altDown = true;
		else if(altDown && e.getKeyCode() == KeyEvent.VK_END)
			quit();
		else
			post(KEY_PRESS, e.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		heldKeys.remove((Integer)e.getKeyCode());
		if(e.getKeyCode() == KeyEvent.VK_ALT)
			altDown = false;
		post(KEY_RELEASE, e.getKeyCode());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		post(MOUSE_PRESS, MouseEvent.getMaskForButton(e.getButton()));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		post(MOUSE_RELEASE, MouseEvent.getMaskForButton(e.getButton()));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		long time = System.currentTimeMillis();
		if(time - moveLimiter > MOVE_LIMIT) {
			post(MOUSE_MOVE, e.getX(), e.getY());
			moveLimiter = time;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		long time = System.currentTimeMillis();
		if(time - moveLimiter > MOVE_LIMIT) {
			post(MOUSE_MOVE, e.getX(), e.getY());
			moveLimiter = time;
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		post(MOUSE_MOVE, e.getX(), e.getY());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		post(MOUSE_WHEEL, e.getWheelRotation());
	}
	
	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Exception e) {}
	}

	@Override
	public void keyTyped(KeyEvent arg0) { }

	@Override
	public void mouseExited(MouseEvent arg0) { }

	@Override
	public void mouseClicked(MouseEvent e) { }


}
