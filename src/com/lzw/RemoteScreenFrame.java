package com.qjy;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;

import javax.swing.*;

import com.lzw.rmi.*;
import com.sun.image.codec.jpeg.*;

public class RemoteScreenFrame extends JPanel implements Runnable,
		MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	private BufferedImage image;
	private Socket socket;
	public Socket getSocket() {
		return socket;
	}
	private RobotControlI robot;
	private MainFrame frame;
	private JScrollPane panel;

	public RemoteScreenFrame(MainFrame frame, JScrollPane scrollPane,
			Socket socketArg) {
		super();
		this.frame = frame;
		socket = socketArg;
		this.panel = scrollPane;
		try {
			// ��ȡ�Է�����IP��ַ
			String address = socket.getInetAddress().getHostAddress();
			// ��ȡע���б�
			Registry registry = LocateRegistry.getRegistry(address, 1098);
			// ��ȡRMIʵ����
			robot = (RobotControlI) registry.lookup("robot");
		} catch (Exception e) {
			e.printStackTrace();
		}
		initComponents();
	}

	private void initComponents() {
		addMouseListener(this);			// ���Ӹ�������¼�������
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);			// ���Ӽ��̰����¼�������
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);						// ִ�и���Ļ��Ʒ���
		g.drawImage(image, 0, 0, this);	// ����ȡ����Ļͼ����Ƶ������
	}

	@Override
	public synchronized void run() {
		try {
			InputStream is = socket.getInputStream();	// ��ȡ����������
			BufferedInputStream bis = new BufferedInputStream(is);
			socket.setTcpNoDelay(true);
			socket.setReceiveBufferSize(1024*1024);
			while (!socket.isClosed()) {
				Thread.sleep(100);	// �߳�����0.1��
				socket.sendUrgentData(0XFF);
				// ����JPEG������
				JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(bis);
				image = decoder.decodeAsBufferedImage();	// ������������JPEGͼƬ
				if (!frame.isShowing()
						|| frame.getExtendedState() == JFrame.ICONIFIED)
					continue;
				Dimension preferredSize = new Dimension(image.getWidth(), image
						.getHeight());		// ����ͼƬ��С���������С
				setPreferredSize(preferredSize);
				revalidate();
				repaint();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		JOptionPane.showMessageDialog(this, "Զ��Э����ֹͣ��");
		panel.setVisible(false); // ����Զ����Ļ����
		JPanel linkPanel = frame.getLinkPanel();
		linkPanel.setVisible(true); // ��ʾ�������
		setPreferredSize(new Dimension(linkPanel.getWidth(), linkPanel
				.getHeight()));
		revalidate();
	}
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		requestFocus();
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * ��갴ť�����¼��Ĵ�������
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Point point = e.getPoint();				// ��ȡ�������λ��
		int button = -1;
		switch (e.getButton()) {				// ��ȡ��갴��
		case MouseEvent.BUTTON1:				// ���������Button1
			button = InputEvent.BUTTON1_MASK;	// ���÷��͵�Զ�̵İ�������
			break;
		case MouseEvent.BUTTON2:				// ������I��Button2
			button = InputEvent.BUTTON2_MASK;	// ���÷��͵İ�������
			break;
		case MouseEvent.BUTTON3:				// ���������Button3
			button = InputEvent.BUTTON3_MASK;	// ���÷��͵İ�������
			break;
		default:
			return;
		}
		try {
			robot.mouseMove(point.x, point.y);	// ��Զ������������ƶ���point��λ��
			robot.mousePress(button);			// ִ�а�������ָ���İ�ť�����¼�
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}
	/**
	 * ��갴��̧����¼���������
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		Point point = e.getPoint();
		int button = -1;
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			button = InputEvent.BUTTON1_MASK;
			break;
		case MouseEvent.BUTTON2:
			button = InputEvent.BUTTON2_MASK;
			break;
		case MouseEvent.BUTTON3:
			button = InputEvent.BUTTON3_MASK;
			break;
		default:
			return;
		}
		try {
			robot.mouseMove(point.x, point.y);
			robot.mouseRelease(button);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		Point point = e.getPoint();
		try {
			robot.mouseMove(point.x, point.y);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * ����ƶ��¼��Ĵ�������
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Point point = e.getPoint();
		try {
			robot.mouseMove(point.x, point.y);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	/** 
	 * �������¼��Ĵ�������
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		try {
			robot.mouseWheel(e.getWheelRotation());
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	/** 
	 * ���̰����İ����¼���������
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		try {
			robot.keyPress(e.getKeyCode());
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * ���̰������ͷ��¼���������
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		try {
			robot.keyRelease(e.getKeyCode());
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}