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
		Toolkit toolkit = Toolkit.getDefaultToolkit(); // 获取工具类的实例对象
		screenSize = toolkit.getScreenSize(); // 获取屏幕大小
		rectangle = new Rectangle(screenSize); // 创建屏幕大小的矩形对象
		OutputStream outputStream = null;
		BufferedOutputStream bout = null;
		try {
			outputStream = socket.getOutputStream(); // 获取网络输出流
			bout = new BufferedOutputStream(outputStream);// 创建带缓冲的网络输出流
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// 创建JPEG格式的编码器
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bout);
		
		while (!socket.isClosed()) {
			try {
				Thread.sleep(300); // 线程休眠0.1秒
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
				encoder.encode(image); // 将图像编码到指定的网络输出流
			} catch (Exception e) {
				e.printStackTrace();
				try {
					bout.close(); // 关闭输出流
					outputStream.close();
					socket.close(); // 关闭网络连接
					JOptionPane.showMessageDialog(frame, "协助异常终止。");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		frame.linkButton.setText("求助");
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