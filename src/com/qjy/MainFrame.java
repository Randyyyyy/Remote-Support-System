package com.qjy;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.lzw.dialog.LinkInfoDialog;
import com.lzw.rmi.RobotControlImpl;

public class MainFrame extends JFrame {

	private JTextField hostField;
	private JTextField nameField;
	private JList linkList;
	ServerSocket server = null;
	JToggleButton linkButton;
	private JPanel linkPanel;
	private DefaultListModel listModel;
	private SystemTray systemTray;
	private RemoteScreenFrame remoteScreenFrame;
	private JScrollPane panel = new JScrollPane();
	private ImageIcon icon = new ImageIcon(getClass().getResource(
			"/res/trayIcon.png"));

	public JPanel getLinkPanel() {
		return linkPanel;
	}

	/**
	 * Create the frame
	 */
	public MainFrame() {
		super();
		// 创建存储联系人模型数据的文件对象
		File listModelFile = new File("./data/listModel.data");
		if (listModelFile.exists()) {// 如果数据模型文件存在
			try {
				// 获取文件的输入流
				FileInputStream fis = new FileInputStream(listModelFile);
				// 将文件输入流转换为对象输入流
				ObjectInputStream ois = new ObjectInputStream(fis);
				// 从文件中读取列表组件的数据模型对象
				listModel = (DefaultListModel) ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {// 否则
			listModel = new DefaultListModel();// 创建新的数据模型对象
		}
		initComponents();// 执行界面初始化
		linkList.setModel(listModel);// 设置列表组件使用的数据模型
		initSystemTray();//初始化系统托盘
		try {
			// 创建远程调用的存根（也就是服务器的代理对象，用于执行本地方法）
			RobotControlImpl remoteRobot_Server = new RobotControlImpl();
			// 启动RMI服务器（默认端口是1099）
			Registry registry = LocateRegistry.createRegistry(1098);
			// 注册RMI实现类
			registry.rebind("robot", remoteRobot_Server);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setLocationRelativeTo(null);
		startServer();
	}

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 启动服务套接字
	 */
	public void startServer() {
		try {
			server = new ServerSocket(9527); // 创建服务器套接字
			new Thread(new Runnable() {
				public void run() {
					panel
							.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					panel
							.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
					while (!server.isClosed()) {
						try {
							// 接受远程计算机的连接请求
							final Socket socket = server.accept();
							if (socket.isConnected()) {
								// 获取对方计算机的主机名称
								String name = socket.getInetAddress()
										.getHostName();
								// 显示提示信息
								int i = JOptionPane.showConfirmDialog(
										MainFrame.this, "收到" + name
												+ "的求助请求，是否开始协助？");
								if (i == JOptionPane.NO_OPTION) {
									socket.close();
									continue;
								}
								setExtendedState(JFrame.NORMAL);
								// 创建远程屏幕监控面板
								remoteScreenFrame = new RemoteScreenFrame(
										MainFrame.this, panel, socket);
								panel.setViewportView(remoteScreenFrame);
								new Thread(remoteScreenFrame).start();
								// 创建右下角的X断开按钮
								JButton button = new JButton("X");
								button.setMargin(new Insets(0, 0, 0, 0));
								button.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										try {
											socket.close();
											panel.setVisible(false);
										} catch (IOException e1) {
											e1.printStackTrace();
										}
									}
								});
								panel.setCorner(
										ScrollPaneConstants.LOWER_RIGHT_CORNER,
										button);
								// 隐藏远程连接面板
								linkPanel.setVisible(false);
								// 显示远程监控面板
								panel.setVisible(true);
								add(panel, BorderLayout.CENTER);
								validate();
							}
						} catch (IOException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.this, e);

						}
					}
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.this, e);
			JOptionPane.showMessageDialog(MainFrame.this, "本软件已经启动。");
			System.exit(0);
		}
	}

	/**
	 * 初始化系统托盘的方法
	 */
	private void initSystemTray() {
		if (SystemTray.isSupported())
			systemTray = SystemTray.getSystemTray();
		TrayIcon trayIcon = new TrayIcon(icon.getImage());
		trayIcon.setImageAutoSize(true);
		URL url = getClass().getResource("/res/1.jpg");
		PopupMenu popupMenu = new PopupMenu("托盘菜单");

		// 创建显示主窗体菜单项
		MenuItem showMenuItem = new MenuItem("显示主窗体");
		showMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.setExtendedState(JFrame.NORMAL);
				MainFrame.this.setVisible(true);
			}
		});

		// 创建退出菜单项
		MenuItem exitMenuItem = new MenuItem("退出");
		exitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		popupMenu.add(showMenuItem);
		popupMenu.addSeparator();
		popupMenu.add(exitMenuItem);
		trayIcon.setPopupMenu(popupMenu);
		try {
			systemTray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化界面的方法
	 */
	private void initComponents() {
		setIconImage(icon.getImage());
		getContentPane().setLayout(new BorderLayout());
		setTitle("远程协助系统");
		addWindowListener(new ThisWindowListener());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setSize(476, 301);

		final JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(Color.GREEN, 1, false));
		panel.setLayout(new BorderLayout());
		panel.setPreferredSize(new Dimension(180, 0));
		getContentPane().add(panel, BorderLayout.WEST);

		final JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);

		linkList = new JList();
		linkList.addListSelectionListener(new LinkListListSelectionListener());
		linkList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		scrollPane.setViewportView(linkList);

		final JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.PAGE_AXIS));
		panel.add(panel_2, BorderLayout.EAST);

		final JButton addButton = new JButton();
		addButton.addActionListener(new AddButtonActionListener());
		addButton.setMargin(new Insets(0, 5, 0, 5));
		addButton.setText("添加");
		panel_2.add(addButton);

		final JButton modiButton = new JButton();
		modiButton.addActionListener(new ModiButtonActionListener());
		modiButton.setMargin(new Insets(0, 5, 0, 5));
		modiButton.setText("修改");
		panel_2.add(modiButton);

		final JButton delButton = new JButton();
		delButton.addActionListener(new DelButtonActionListener());
		delButton.setMargin(new Insets(0, 5, 0, 5));
		delButton.setText("删除");
		panel_2.add(delButton);

		linkPanel = new JPanel();
		linkPanel.addComponentListener(new LinkPanelComponentListener());
		final GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[] { 0, 0, 7 };
		linkPanel.setLayout(gridBagLayout_1);
		linkPanel.setSize(338, 187);
		getContentPane().add(linkPanel, BorderLayout.CENTER);

		JLabel label_1;
		label_1 = new JLabel();
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(48, 32, 3, 0);
		linkPanel.add(label_1, gridBagConstraints);
		label_1.setText("对方姓名：");

		nameField = new JTextField();
		nameField.addKeyListener(new NameFieldKeyListener());
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.gridx = 1;
		gridBagConstraints_2.gridy = 0;
		gridBagConstraints_2.gridwidth = 2;
		gridBagConstraints_2.ipadx = 203;
		gridBagConstraints_2.insets = new Insets(45, 4, 0, 33);
		linkPanel.add(nameField, gridBagConstraints_2);

		linkButton = new JToggleButton();
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.gridx = 1;
		gridBagConstraints_4.gridy = 2;
		gridBagConstraints_4.insets = new Insets(30, 4, 40, 0);
		linkPanel.add(linkButton, gridBagConstraints_4);
		linkButton.addActionListener(new LinkButtonActionListener());
		linkButton.setText("求助");

		JButton button_1;
		button_1 = new JButton();
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridx = 2;
		gridBagConstraints_5.gridy = 2;
		gridBagConstraints_5.insets = new Insets(30, 43, 40, 85);
		linkPanel.add(button_1, gridBagConstraints_5);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				System.exit(0);
			}
		});
		button_1.setText("关闭");

		JLabel label_1_1;
		label_1_1 = new JLabel();
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridx = 0;
		gridBagConstraints_1.gridy = 1;
		gridBagConstraints_1.ipady = 3;
		gridBagConstraints_1.insets = new Insets(8, 32, 2, 0);
		linkPanel.add(label_1_1, gridBagConstraints_1);
		label_1_1.setText("对方主机：");

		hostField = new JTextField();
		hostField.addKeyListener(new HostFieldKeyListener());
		hostField.setText("192.168.1.128");
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridx = 1;
		gridBagConstraints_3.gridy = 1;
		gridBagConstraints_3.gridwidth = 2;
		gridBagConstraints_3.ipadx = 203;
		gridBagConstraints_3.ipady = 1;
		gridBagConstraints_3.insets = new Insets(6, 4, 0, 33);
		linkPanel.add(hostField, gridBagConstraints_3);

		setVisible(true);

		final JPanel panel_3 = new JPanel();
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 7 };
		panel_3.setLayout(gridBagLayout);
		getContentPane().add(panel_3, BorderLayout.NORTH);

		final JLabel label_2 = new JLabel();
		label_2.setIcon(new ImageIcon(getClass().getResource("/res/1.jpg")));
		final GridBagConstraints gridBagConstraints_7 = new GridBagConstraints();
		gridBagConstraints_7.ipadx = -10;
		gridBagConstraints_7.anchor = GridBagConstraints.WEST;
		panel_3.add(label_2, gridBagConstraints_7);

		final JLabel label = new JLabel();
		label.setIcon(new ImageIcon(getClass().getResource("/res/2.jpg")));
		final GridBagConstraints gridBagConstraints_6 = new GridBagConstraints();
		gridBagConstraints_6.anchor = GridBagConstraints.WEST;
		gridBagConstraints_6.weightx = 1.0;
		gridBagConstraints_6.ipadx = 40;
		gridBagConstraints_6.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints_6.gridy = 0;
		gridBagConstraints_6.gridx = 1;
		panel_3.add(label, gridBagConstraints_6);
	}//主界面

	private void storeListModel() {
		File listModelFile = new File("data/listModel.data");// 创建存储数据的文件对象
		try {
			if (!listModelFile.exists()) { // 如果文件不存在
				listModelFile.getParentFile().mkdirs(); // 创建文件夹
				listModelFile.createNewFile(); // 和文件
			}
			// 获取文件输出流
			FileOutputStream fout = new FileOutputStream(listModelFile);
			// 创建对象输出流
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(listModel); // 序列化对象到文件中
		} catch (Exception e) {
			e.printStackTrace();
		}
	}//存储联系人信息

	/**
	 * @author lzwJava 连接按钮的事件监听器
	 */
	private class LinkButtonActionListener implements ActionListener {
		private Socket socket;

		public void actionPerformed(final ActionEvent e) {
			if (linkButton.isSelected()) {
				String host = hostField.getText(); // 获取主机地址
				InetAddress address = null;
				try {
					address = InetAddress.getByName(host); // 将字符串转换成地址对象
				} catch (IOException e1) {
					// 如果IP非法，提示用户
					JOptionPane.showMessageDialog(MainFrame.this, "对方主机地址不正确");
					return;
				}
				if (address == null)
					return;
				try {
					socket = new Socket(address, 9527); // 创建连接对方主机的Socket
					JOptionPane.showMessageDialog(MainFrame.this,
							"连接成功,对方将控制您的计算机。");
					Runnable runnable = new SendImageThread(MainFrame.this,
							socket); // 创建发送屏幕图像的线程
					new Thread(runnable).start(); // 启动线程
				} catch (ConnectException e1) {
					// 如果无法连接对方主机，提示本地用户
					JOptionPane.showMessageDialog(MainFrame.this,
							"无法连接对方主机，必须确认主机地址的正确性，" + "\n并且对方已经启动本软件");
					JOptionPane.showMessageDialog(MainFrame.this, e1.toString()
							.replaceAll("\n", "\t"));// 显示错误信息
					linkButton.setText("求助");
					linkButton.setSelected(false);
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				linkButton.setText("中断求助"); // 更改按钮文本
			} else {// 再次单击按钮时
				try {
					socket.close();// 端口网络连接
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				linkButton.setText("求助");
			}
		}
	}//求助

	private class AddButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			// 获取用户输入的姓名字符串
			String name = JOptionPane.showInputDialog(MainFrame.this, "请输入姓名");
			// 获取用户输入的主机或IP字符串
			String host = JOptionPane.showInputDialog(MainFrame.this,
					"请输入对方主机名称或IP");
			
			if (name != null && host != null && !name.isEmpty()
					&& !host.isEmpty()) {
				
				LinkItem item = new LinkItem(); // 创建联系人JavaBean
				item.setName(name); // 初始化该JavaBean
				item.setHostName(host);
				DefaultListModel model = (DefaultListModel) linkList.getModel();
				model.addElement(item); // 添加联系人到列表组件模板
				storeListModel(); // 存储列表数据
			}
		}
	}//添加用户

	private class DelButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			int[] is = linkList.getSelectedIndices(); // 获取联系人列表的多个选择项
			int index = linkList.getSelectedIndex(); // 获取列表选项的索引
			
			if (is.length > 0) { // 如果选择书名大于零
				int button = JOptionPane.showConfirmDialog(MainFrame.this,
						"是否确定删除用户信息"); // 显示确认对话框
				if (button != JOptionPane.YES_OPTION) // 如果不同意删除
					return; // 结束该方法
			}
			listModel.remove(index);// 根据索引删除联系人信息
			storeListModel(); // 存储列表数据模型
		}
	}//删除用户

	private class ModiButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			int index = linkList.getSelectedIndex(); // 获取列表选项的索引
			LinkItem item = (LinkItem) listModel.getElementAt(index);// 获取选择的联系人选项
			LinkInfoDialog dialog = new LinkInfoDialog(item, MainFrame.this,
					true); // 创建修改联系人的对话框
			dialog.setVisible(true); // 显示对话框
			linkList.repaint();
			storeListModel(); // 存储列表数据
		}
	}//修改用户

	/**
	 * 联系人列表的选择事件监听器
	 */
	private class LinkListListSelectionListener implements
			ListSelectionListener {
		public void valueChanged(final ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) { // 如果选项调整完毕
				// 获取选择的列表项，并转换成联系人实体对象
				LinkItem item = (LinkItem) linkList.getSelectedValue();
				if (item != null) {
					// 使用实体对象填充远程连接面板的数据
					nameField.setText(item.getName());
					hostField.setText(item.getHostName());
				}
			}
		}
	}//选择联系人

	private class ThisWindowListener extends WindowAdapter {
		public void windowClosing(final WindowEvent e) {
			setExtendedState(JFrame.ICONIFIED);
			setVisible(false);
		}
	}

	private class LinkPanelComponentListener extends ComponentAdapter {
		public void componentShown(final ComponentEvent e) {
			if (remoteScreenFrame.getSocket().isClosed()) {
				linkButton.setSelected(false);
				linkButton.setText("求助");
			} else {
				linkButton.setSelected(true);
				linkButton.setText("中断求助");
			}
		}
	}//中断求助

	/**
	 *对方姓名文本框的按键事件监听器
	 */
	private class NameFieldKeyListener extends KeyAdapter {
		public void keyPressed(final KeyEvent e) {
			if (e.getKeyChar() == '\n') { // 如果按的是回车键
				hostField.requestFocus();// 将输入焦点转给对方主机文本框
			}
		}
	}

	/**
	 *对方主机文本框的按键事件监听器
	 */
	private class HostFieldKeyListener extends KeyAdapter {
		public void keyPressed(final KeyEvent e) {
			if (e.getKeyChar() == '\n') { // 如果按的是回车键
				linkButton.doClick();// 执行求助按钮的鼠标单击事件
			}
		}
	}
	
}
