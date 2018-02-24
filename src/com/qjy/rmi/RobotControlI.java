package com.qjy.rmi;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.rmi.Remote;
import java.rmi.RemoteException;
public interface RobotControlI extends Remote {
	public BufferedImage createScreenCapture(Rectangle screenRect)
			throws RemoteException;
	// ��ȡԶ��������Ļָ�������������ɫ 
	public Color getPixelColor(int x, int y) throws RemoteException;
	// ִ��Զ����������ָ�������İ��¶���
	public void keyPress(int keycode) throws RemoteException;
	// ִ��Զ����������ָ��������̧����
	public void keyRelease(int keycode) throws RemoteException;
	// ִ��Զ������������ƶ�����
	public void mouseMove(int x, int y) throws RemoteException;
	// ִ��Զ�����������ָ�������İ��¶���
	public void mousePress(int buttons) throws RemoteException;
	// ִ��Զ�����������ָ���������ͷŶ���
	public void mouseRelease(int buttons) throws RemoteException;
	// ִ��Զ�������������ֶ���
	public void mouseWheel(int wheelAmt) throws RemoteException;
}