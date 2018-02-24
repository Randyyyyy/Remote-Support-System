package com.lzw.dialog;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.lzw.LinkItem;
import com.lzw.MainFrame;

public class LinkInfoDialog extends JDialog {

	private JTextField hostTextField;
	private JTextField nameTextField;
	private LinkItem item;
	/**
	 * Create the dialog
	 * @param item 
	 * @param b 
	 * @param mainFrame 
	 */
	public LinkInfoDialog(LinkItem item, MainFrame mainFrame, boolean b) {
		super(mainFrame,b);					// 执行父类构造方法
		this.item=item;						
		setBounds(100, 100, 377, 142);
		initComponents();					// 使用item初始化程序界面数据 
	}
	
	private void initComponents() {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0,7,7};
		gridBagLayout.columnWidths = new int[] {0,7,7};
		getContentPane().setLayout(gridBagLayout);
		setTitle("修改联系人");

		final JLabel label = new JLabel();
		label.setText("姓名：");
		final GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridx = 0;
		getContentPane().add(label, gridBagConstraints);

		nameTextField = new JTextField(item.getName());
		final GridBagConstraints gridBagConstraints_1 = new GridBagConstraints();
		gridBagConstraints_1.gridwidth = 2;
		gridBagConstraints_1.insets = new Insets(0, 0, 5, 10);
		gridBagConstraints_1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_1.weightx = 1.0;
		gridBagConstraints_1.gridy = 0;
		gridBagConstraints_1.gridx = 1;
		getContentPane().add(nameTextField, gridBagConstraints_1);

		final JLabel label_1 = new JLabel();
		label_1.setText("主机：");
		final GridBagConstraints gridBagConstraints_2 = new GridBagConstraints();
		gridBagConstraints_2.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints_2.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_2.ipadx = -5;
		gridBagConstraints_2.gridy = 1;
		gridBagConstraints_2.gridx = 0;
		getContentPane().add(label_1, gridBagConstraints_2);

		hostTextField = new JTextField(item.getHostName());
		final GridBagConstraints gridBagConstraints_3 = new GridBagConstraints();
		gridBagConstraints_3.gridwidth = 2;
		gridBagConstraints_3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints_3.insets = new Insets(0, 0, 5, 10);
		gridBagConstraints_3.gridy = 1;
		gridBagConstraints_3.gridx = 1;
		getContentPane().add(hostTextField, gridBagConstraints_3);

		final JButton okButton = new JButton();
		okButton.addActionListener(new OkButtonActionListener());
		okButton.setText("确定");
		final GridBagConstraints gridBagConstraints_4 = new GridBagConstraints();
		gridBagConstraints_4.insets = new Insets(0, 45, 0, 0);
		gridBagConstraints_4.gridy = 2;
		gridBagConstraints_4.gridx = 1;
		getContentPane().add(okButton, gridBagConstraints_4);

		final JButton closeButton = new JButton();
		closeButton.addActionListener(new CloseButtonActionListener());
		closeButton.setText("关闭");
		final GridBagConstraints gridBagConstraints_5 = new GridBagConstraints();
		gridBagConstraints_5.gridy = 2;
		gridBagConstraints_5.gridx = 2;
		getContentPane().add(closeButton, gridBagConstraints_5);
	}
	private class CloseButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			dispose();
		}
	}
	private class OkButtonActionListener implements ActionListener {
		public void actionPerformed(final ActionEvent e) {
			String name = nameTextField.getText();	// 获取姓名信息
			String host = hostTextField.getText();	// 获取主机或IP信息
			item.setName(name);						// 修改item对象的数据
			item.setHostName(host);
			dispose();								// 销毁对话框
		}
	}
}
