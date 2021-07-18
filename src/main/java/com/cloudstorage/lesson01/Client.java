package com.cloudstorage.lesson01;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
	private final Socket socket;
	private final DataOutputStream out;
	private final DataInputStream in;

	public Client() throws IOException {
		socket = new Socket("localhost", 5678);
		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());

		setSize(300, 300);
		JPanel panel = new JPanel(new GridLayout(2, 1));

		JButton btnSend = new JButton("SEND");
		JTextField textField = new JTextField();

		btnSend.addActionListener(a -> {
			// upload 1.txt
			// download img.png
			String[] cmd = textField.getText().split(" ");
			mainActions(cmd);
		});

		panel.add(textField);
		panel.add(btnSend);

		add(panel);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				sendMessage("exit");
			}
		});
		setVisible(true);
	}

	private void mainActions(String[] cmd) {
		if ("upload".equals(cmd[0])) {
			sendFile(cmd[1]);
		} else if ("download".equals(cmd[0])) {
			getFile(cmd[1]);
		}
	}

	private void getFile(String filename) {
		try {
			out.writeUTF("download");
			out.writeUTF(filename);

			File file = new File("client" + File.separator + filename);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(file);
			long size = in.readLong();
			byte[] buffer = new byte[8 * 1024];

			for (int i = 0; i < (size + (buffer.length - 1)) / buffer.length; i++) {
				int read = in.read(buffer);
				fos.write(buffer, 0, read);
			}

			fos.close();

			out.writeUTF("OK");
		} catch (IOException e) {
			try {
				out.writeUTF("WRONG");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.err.println("Downloading error");
			e.printStackTrace();
		}

	}

	private void sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (!file.exists()) {
				throw  new FileNotFoundException();
			}

			long fileLength = file.length();
			FileInputStream fis = new FileInputStream(file);

			out.writeUTF("upload");
			out.writeUTF(filename);
			out.writeLong(fileLength);

			int read = 0;
			byte[] buffer = new byte[8 * 1024];
			while ((read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}

			out.flush();

			String status = in.readUTF();
			System.out.println("sending status: " + status);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(String message) {
		try {
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}