package com.qjy.rmi;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RobotControlI extends Remote {
	public BufferedImage createScreenCapture(Rectangle screenRect)
			throws RemoteException;
	// 获取远程主机屏幕指定坐标的像素颜色 
	public Color getPixelColor(int x, int y) throws RemoteException;
	// 执行远程主机键盘指定按键的按下动作
	public void keyPress(int keycode) throws RemoteException;
	// 执行远程主机键盘指定按键的抬起动作
	public void keyRelease(int keycode) throws RemoteException;
	// 执行远程主机的鼠标移动方法
	public void mouseMove(int x, int y) throws RemoteException;
	// 执行远程主机的鼠标指定按键的按下动作
	public void mousePress(int buttons) throws RemoteException;
	// 执行远程主机的鼠标指定按键的释放动作
	public void mouseRelease(int buttons) throws RemoteException;
	// 执行远程主机的鼠标滚轮动作
	public void mouseWheel(int wheelAmt) throws RemoteException;
}