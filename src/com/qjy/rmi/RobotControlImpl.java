package com.qjy.rmi;
import java.awt.AWTException;
import java.awt.Robot;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
/**
 * ʵ��Զ�̽ӿڵķ�������
 */
public class RobotControlImpl extends Robot implements RobotControlI {
	public RobotControlImpl() throws RemoteException, AWTException {
		UnicastRemoteObject.exportObject(this, 0);
	}
}