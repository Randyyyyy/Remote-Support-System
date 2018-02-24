package com.qjy.rmi;
import java.awt.AWTException;
import java.awt.Robot;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
/**
 * 实现远程接口的服务器类
 */
public class RobotControlImpl extends Robot implements RobotControlI {
	public RobotControlImpl() throws RemoteException, AWTException {
		UnicastRemoteObject.exportObject(this, 0);
	}
}