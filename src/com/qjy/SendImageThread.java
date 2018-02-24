/**
 * 
 */
package com.qjy;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

import javax.swing.*;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

class SendImageThread implements Runnable {
	/**
	 * 
	 */
	private final MainFrame frame;
	private Socket socket;

	Dimension screenSize;
	private Rectangle rectangle;
	Robot robot;
	private BufferedImage image;

	/**
	 * @param mainFrame
	 * @param socket
	 */
	SendImageThread(MainFrame mainFrame, Socket socket) {
		frame = mainFrame;
		this.socket = socket;
		try {
			socket.setSendBufferSize(1024 * 1024);
			socket.setTcpNoDelay(true);
			socket.setOOBInline(true);
			robot = new Robot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		Toolkit toolkit = Toolkit.getDefaultToolkit(); // ��ȡ�������ʵ������
		screenSize = toolkit.getScreenSize(); // ��ȡ��Ļ��С
		rectangle = new Rectangle(screenSize); // ������Ļ��С�ľ��ζ���
		OutputStream outputStream = null;
		BufferedOutputStream bout = null;
		try {
			outputStream = socket.getOutputStream(); // ��ȡ���������
			bout = new BufferedOutputStream(outputStream);// ��������������������
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// ����JPEG��ʽ�ı�����
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bout);
		
		while (!socket.isClosed()) {
			try {
				Thread.sleep(300); // �߳�����0.1��
				BufferedImage screenImage = robot
						.createScreenCapture(rectangle);
				if (image == null) {
					image = screenImage;
				} else {
					if (ImageEquals(image, screenImage)) {
						continue;
					} else {
						image = screenImage;
					}
				}
				encoder.encode(image); // ��ͼ����뵽ָ�������������
			} catch (Exception e) {
				e.printStackTrace();
				try {
					bout.close(); // �ر������
					outputStream.close();
					socket.close(); // �ر���������
					JOptionPane.showMessageDialog(frame, "Э���쳣��ֹ��");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		frame.linkButton.setText("����");
		frame.linkButton.setSelected(false);
	}

	private boolean ImageEquals(BufferedImage image1, BufferedImage image2) {
		int w1 = image1.getWidth();
		int h1 = image2.getHeight();
		int w2 = image1.getWidth();
		int h2 = image2.getHeight();
		if (w1 != w2 || h1 != h2)
			return false;
		for (int i = 0; i < w1; i += 4) {
			for (int j = 0; j < h1; j += 4) {
				if (image1.getRGB(i, j) != image2.getRGB(i, j))
					return false;
			}
		}
		return true;
	}
}