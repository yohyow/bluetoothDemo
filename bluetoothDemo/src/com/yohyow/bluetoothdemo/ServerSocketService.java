/**
 * ServerSocketService.java Aug 12, 2013
 */
package com.yohyow.bluetoothdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * @author HHP
 * 尝试以服务形式启动socket监听
 * Version 
 */
public class ServerSocketService extends Service {
	
	/* 一些常量，代表服务器的名称 */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";
	private static final String TAG = ServerSocketActivity.class
			.getSimpleName();
	private Handler _handler = new Handler();
	/* 取得默认的蓝牙适配器 */
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	/* 蓝牙服务器 */
	private BluetoothServerSocket _serverSocket;
	/* 线程-监听客户端的链接 */
	private Thread _serverWorker = new Thread() {
		public void run() {
			listen();
		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (_bluetooth.isEnabled()) {
			/* 开始监听 */
			_serverWorker.start();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		shutdownServer();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdownServer();
	}
	
	/* 停止服务器 */
	private void shutdownServer() {
		new Thread() {
			public void run() {
				_serverWorker.interrupt();
				if (_serverSocket != null) {
					try {
						/* 关闭服务器 */
						_serverSocket.close();
					} catch (IOException e) {
						Log.e(TAG, "", e);
					}
					_serverSocket = null;
				}
			};
		}.start();
	}
	
	protected void listen() {
		try {
			/*
			 * 创建一个蓝牙服务器 参数分别：服务器名称、UUID
			 */
			_serverSocket = _bluetooth.listenUsingRfcommWithServiceRecord(
					PROTOCOL_SCHEME_RFCOMM,
					UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
			// _serverSocket =
			// _bluetooth.listenUsingRfcommWithServiceRecord(_bluetooth.getName(),
			// null);
			/* 客户端连线列表 */
//			_handler.post(new Runnable() {
//				public void run() {
//					lines.clear();
//					lines.add("Rfcomm server started...");
//					mAdapter.notifyDataSetChanged();
//				}
//			});
			/**以循环方式不停的接收 一至多个socket连接*/
			while(true) {
				/* 接受客户端的连接请求 */
				BluetoothSocket socket = _serverSocket.accept();
				new handleSocketThread(socket).start();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			while (socket != null) {
////			/* 处理请求内容 */
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
////							Log.i("===接受到了===", s);
////						}
////					});
////				}
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				Log.i("======", "读取完毕");
//			}
//			if (_serverSocket != null) {
//				try {
//					/* 关闭服务器 */
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
	 * 处理客户端数据线程
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
//							mSocket.getRemoteDevice().getAddress();
							// TODO 把address和内容 通过广播发送出去
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
