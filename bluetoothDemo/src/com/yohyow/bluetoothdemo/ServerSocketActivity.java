package com.yohyow.bluetoothdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

public class ServerSocketActivity extends ListActivity {
	/* һЩ��������������������� */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";
	private static final String TAG = ServerSocketActivity.class
			.getSimpleName();
	private Handler _handler = new Handler();
	/* ȡ��Ĭ�ϵ����������� */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	/* ���������� */
	private BluetoothServerSocket _serverSocket;
	/* �߳�-�����ͻ��˵����� */
	private Thread _serverWorker = new Thread() {
		public void run() {
			listen();
		};
	};

	private List<String> lines = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		setContentView(R.layout.server_socket);
		if (!_bluetooth.isEnabled()) {
			finish();
			return;
		}
		/* ��ʼ���� */
		_serverWorker.start();
		mAdapter = new ArrayAdapter<String>(
				ServerSocketActivity.this,
				android.R.layout.simple_list_item_1, lines);
		setListAdapter(mAdapter);
	}

	protected void onDestroy() {
		super.onDestroy();
		shutdownServer();
	}

	protected void finalize() throws Throwable {
		super.finalize();
		shutdownServer();
	}

	/* ֹͣ������ */
	private void shutdownServer() {
		new Thread() {
			public void run() {
				_serverWorker.interrupt();
				if (_serverSocket != null) {
					try {
						/* �رշ����� */
						_serverSocket.close();
					} catch (IOException e) {
						Log.e(TAG, "", e);
					}
					_serverSocket = null;
				}
			};
		}.start();
		finish();
	}

	public void onButtonClicked(View view) {
		shutdownServer();
	}

	protected void listen() {
		try {
			/*
			 * ����һ������������ �����ֱ𣺷��������ơ�UUID
			 */
			_serverSocket = _bluetooth.listenUsingRfcommWithServiceRecord(
					PROTOCOL_SCHEME_RFCOMM,
					UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
			// _serverSocket =
			// _bluetooth.listenUsingRfcommWithServiceRecord(_bluetooth.getName(),
			// null);
			/* �ͻ��������б� */
			_handler.post(new Runnable() {
				public void run() {
					lines.clear();
					lines.add("Rfcomm server started...");
					mAdapter.notifyDataSetChanged();
				}
			});
			/**��ѭ����ʽ��ͣ�Ľ��� һ�����socket����*/
			while(true) {
				/* ���ܿͻ��˵��������� */
				BluetoothSocket socket = _serverSocket.accept();
				new handleSocketThread(socket).start();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			while (socket != null) {
////			/* ������������ */
////			if (socket != null) {
//				InputStream inputStream = socket.getInputStream();
//				BufferedReader is = new BufferedReader(new InputStreamReader(inputStream));
////				int read = -1;
////				final byte[] bytes = new byte[2048];
//				final String line = is.readLine();
//				if(line.equals("--close")) {
//					break;
//				}
//				Log.i("======", line);
//				_handler.post(new Runnable() {
//					public void run() {
//						lines.add(line);
//						mAdapter.notifyDataSetChanged();
//					}
//				});
////				PrintWriter os = new PrintWriter(socket.getOutputStream());
////				os.println(line);
////				os.flush();
////				os.close();
////				is.close();
////				for (; (read = inputStream.read(bytes)) > -1;) {
////					final int count = read;
////					_handler.post(new Runnable() {
////						public void run() {
////							StringBuilder b = new StringBuilder();
////							for (int i = 0; i < count; ++i) {
////								if (i > 0) {
////									b.append(' ');
////								}
////								String s = Integer.toHexString(bytes[i] & 0xFF);
////								if (s.length() < 2) {
////
////									b.append('0');
////								}
////								b.append(s);
////							}
////							String s = b.toString();
////							Log.i("===���ܵ���===", s);
////						}
////					});
////				}
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				Log.i("======", "��ȡ���");
//			}
//			if (_serverSocket != null) {
//				try {
//					/* �رշ����� */
//					_serverSocket.close();
//				} catch (IOException e) {
//					Log.e(TAG, "", e);
//				}
//				_serverSocket = null;
//			}
//			listen();
		} catch (IOException e) {
			Log.e(TAG, "", e);
		} finally {

		}
	}
	
	/**
	 * ����ͻ��������߳�
	 * @author HHP
	 *
	 * Version
	 */
	class handleSocketThread extends Thread {
		private BluetoothSocket mSocket = null;
		public handleSocketThread(BluetoothSocket socket) {
			this.mSocket = socket;
		}
		
		@Override
		public void run() {
			InputStream in;
			OutputStream out;
			try {
				in = mSocket.getInputStream();
				out = mSocket.getOutputStream();
				while(true) {
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					final String line = br.readLine();
					if(line.equals("--close")) {
						break;
					}
					Log.i("======", line);
					_handler.post(new Runnable() {
						public void run() {
							lines.add(line);
							mAdapter.notifyDataSetChanged();
						}
					});
					Thread.sleep(100);
				}
			} catch (Exception e) {
			}finally {
				try {
					mSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
}
