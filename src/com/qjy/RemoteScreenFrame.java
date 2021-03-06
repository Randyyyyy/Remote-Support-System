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
			// 获取对方主机IP地址
			String address = socket.getInetAddress().getHostAddress();
			// 获取注册列表
			Registry registry = LocateRegistry.getRegistry(address, 1098);
			// 获取RMI实现类
			robot = (RobotControlI) registry.lookup("robot");
		} catch (Exception e) {
			e.printStackTrace();
		}
		initComponents();
	}

	private void initComponents() {
		addMouseListener(this);			// 添加各种鼠标事件监听器
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);			// 添加键盘按键事件监听器
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);						// 执行父类的绘制方法
		g.drawImage(image, 0, 0, this);	// 将获取的屏幕图像绘制到组件上
	}

	@Override
	public synchronized void run() {
		try {
			InputStream is = socket.getInputStream();	// 获取网络输入流
			BufferedInputStream bis = new BufferedInputStream(is);
			socket.setTcpNoDelay(true);
			socket.setReceiveBufferSize(1024*1024);
			while (!socket.isClosed()) {
				Thread.sleep(100);	// 线程休眠0.1秒
				socket.sendUrgentData(0XFF);
				// 创建JPEG解码器
				JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(bis);
				image = decoder.decodeAsBufferedImage();	// 从输入流解码JPEG图片
				if (!frame.isShowing()
						|| frame.getExtendedState() == JFrame.ICONIFIED)
					continue;
				Dimension preferredSize = new Dimension(image.getWidth(), image
						.getHeight());		// 根据图片大小设置组件大小
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
		JOptionPane.showMessageDialog(this, "远程协助以停止。");
		panel.setVisible(false); // 隐藏远程屏幕窗体
		JPanel linkPanel = frame.getLinkPanel();
		linkPanel.setVisible(true); // 显示连接面板
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
	 * 鼠标按钮按下事件的处理方法
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Point point = e.getPoint();				// 获取鼠标坐标位置
		int button = -1;
		switch (e.getButton()) {				// 获取鼠标按键
		case MouseEvent.BUTTON1:				// 如果按键是Button1
			button = InputEvent.BUTTON1_MASK;	// 设置发送到远程的按键编码
			break;
		case MouseEvent.BUTTON2:				// 如果按鍵是Button2
			button = InputEvent.BUTTON2_MASK;	// 设置发送的按键编码
			break;
		case MouseEvent.BUTTON3:				// 如果按键是Button3
			button = InputEvent.BUTTON3_MASK;	// 设置发送的按键编码
			break;
		default:
			return;
		}
		try {
			robot.mouseMove(point.x, point.y);	// 将远程主机的鼠标移动到point的位置
			robot.mousePress(button);			// 执行按键编码指定的按钮按下事件
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}
	/**
	 * 鼠标按键抬起的事件处理方法
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
	 * 鼠标移动事件的处理方法
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
	 * 鼠标滚轮事件的处理方法
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
	 * 键盘按键的按下事件处理方法
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
	 * 键盘按键的释放事件处理方法
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
