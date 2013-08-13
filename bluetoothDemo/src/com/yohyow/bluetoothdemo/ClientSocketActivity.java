package com.yohyow.bluetoothdemo;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ClientSocketActivity  extends ListActivity
{
	private static final String TAG = ClientSocketActivity.class.getSimpleName();
	private static final int REQUEST_DISCOVERY = 0x1;;
	private Handler _handler = new Handler();
	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	private BluetoothSocket mSocket = null;
	private BluetoothDevice mDevice = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discovery);
//		Intent intent = new Intent(this, DiscoveryActivity.class);
		/* ��ʾѡ��һ��Ҫ���ӵķ����� */
		Toast.makeText(this, "select device to connect", Toast.LENGTH_SHORT).show();
		discoveryDevices();
		/* ��ת�������������豸�б���������ѡ�� */
//		startActivityForResult(intent, REQUEST_DISCOVERY);
	}
	/* ѡ���˷�����֮��������� */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_DISCOVERY) {
			return;
		}
		if (resultCode != RESULT_OK) {
			return;
		}
		final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		new Thread() {
			public void run() {
				/* ���� */
				connect(device);
			};
		}.start();
	}
	
	private PrintWriter mPrintWriter = null;
	protected void connect(BluetoothDevice device) {
		try {
			//����һ��Socket���ӣ�ֻ��Ҫ��������ע��ʱ��UUID��
			// socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.OBEX_OBJECT_PUSH_PROTOCOL_UUID);
			if(null == mSocket) {
				mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
				mSocket.connect();
			}
			if(null == mPrintWriter) {
				mPrintWriter = new PrintWriter(mSocket.getOutputStream());
			}
			mPrintWriter.println(_bluetooth.getName() + ":" + mMsg);
			mPrintWriter.flush();
//			os.close();
		} catch (IOException e) {
			Log.e(TAG, "", e);
		} finally {
//			if (socket != null) {
//				try {
//					socket.close();
//				} catch (IOException e) {
//					Log.e(TAG, "", e);
//				}
//			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			mPrintWriter.println("--close");
			mPrintWriter.flush();			
			mSocket.close();
			mPrintWriter.close();
		} catch (Exception e) {
		}finally {
			mSocket = null;
			mPrintWriter = null;
		}
	}
	
	
	
	/* �����洢�������������豸 */
	private List<BluetoothDevice> _devices = new ArrayList<BluetoothDevice>();
	/* �Ƿ�������� */
	private volatile boolean _discoveryFinished;
	private Runnable _discoveryWorkder = new Runnable() {
		public void run() 
		{
			/* ��ʼ���� */
			_bluetooth.startDiscovery();
			for (;;) 
			{
				if (_discoveryFinished) 
				{
					break;
				}
				try 
				{
					Thread.sleep(100);
				} 
				catch (InterruptedException e){}
			}
		}
	};
	/**
	 * ������
	 * �����������豸���ʱ����
	 */
	private BroadcastReceiver _foundReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			/* ��intent��ȡ������������� */
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			/* �������ӵ��б��� */
			_devices.add(device);
			/* ��ʾ�б� */
			showDevices();
		}
	};
	private BroadcastReceiver _discoveryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			/* ж��ע��Ľ����� */
			unregisterReceiver(_foundReceiver);
			unregisterReceiver(this);
			_discoveryFinished = true;
		}
	};

	/* ��ʾ�б� */
	protected void showDevices()
	{
		List<String> list = new ArrayList<String>();
		for (int i = 0, size = _devices.size(); i < size; ++i)
		{
			StringBuilder b = new StringBuilder();
			BluetoothDevice d = _devices.get(i);
			b.append(d.getAddress());
			b.append('\n');
			b.append(d.getName());
			String s = b.toString();
			list.add(s);
		}

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		_handler.post(new Runnable() {
			public void run()
			{

				setListAdapter(adapter);
			}
		});
	}
	
	private String mMsg = "";
	
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		final int p = position;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("1", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDevice = _devices.get(p);
				mMsg = "�ҵ����1";
				new Thread() {
					public void run() {
						/* ���� */
						connect(mDevice);
					};
				}.start();
			}
		}).setNegativeButton("2", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDevice = _devices.get(p);
				mMsg = "�ҵ����2";
				new Thread() {
					public void run() {
						/* ���� */
						connect(mDevice);
					};
				}.start();
			}
		}).show();
//		Intent result = new Intent();
		
//		setResult(RESULT_OK, result);
//		finish();
	}
	
	private void discoveryDevices() {
		/* �������������û�д򿪣����� */
		if (!_bluetooth.isEnabled())
		{

			finish();
			return;
		}
		/* ע������� */
		IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(_discoveryReceiver, discoveryFilter);
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(_foundReceiver, foundFilter);
		/* ��ʾһ���Ի���,�������������豸 */
		SamplesUtils.indeterminate(ClientSocketActivity.this, _handler, "Scanning...", _discoveryWorkder, new OnDismissListener() {
			public void onDismiss(DialogInterface dialog)
			{

				for (; _bluetooth.isDiscovering();)
				{

					_bluetooth.cancelDiscovery();
				}

				_discoveryFinished = true;
			}
		}, true);
	}
}

