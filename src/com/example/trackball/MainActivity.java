package com.example.trackball;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private TextView tv;
	private Button connectButton;
	private Button syncButton;
	private String serverEndpoint = "http://thirdeye1.azurewebsites.net/api/commands";
	private static final int REQUEST_ENABLE_BT = 1107;
	private String bluetoothPairedDeviceName = "RN42-665A";
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothDevice arduinoBluetoothDevice = null;
	private BluetoothSocket arduinoBluetoothSocket = null;
	private String bluetoothFoundText = "Bluetooth Device Found.";
	private String bluetoothNotFoundText = "Bluetooth Device Not Found.";
	private String bluetoothDisabledText = "Bluetooth Disabled";
	private String bluetoothEnabledText = "Bluetooth Enabled.";
	private volatile Boolean inSync = false;
	private ImageButton trackBall;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);
		tv = (TextView) findViewById(R.id.Console);
		connectButton = (Button) findViewById(R.id.arduConnect);
		syncButton = (Button) findViewById(R.id.sync);
		trackBall = (ImageButton) findViewById(R.id.TrackBall);
		trackBall.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				// TODO Auto-generated method stub

				final float centerX = ((View) v.getParent()).getWidth() / 2;
				final float centerY = ((View) v.getParent()).getHeight() / 2;
				TankMoveStrategy ms = new TankMoveStrategy(centerX, centerY);
				ArrayList<Move> moves = ms.getMoves(event.getRawX(),
						event.getRawY());
				String text = "";
				methodSendMessage(moves);
				for (int i = 0; i < moves.size(); i++) {
					text += moves.get(i).toString();
				}
				final String text1 = text.replace(" ", "");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tv.setText(text1);
						trackBall.setX(event.getRawX() - trackBall.getWidth()
								/ 2);
						trackBall.setY(event.getRawY() - trackBall.getHeight()
								/ 2);
					}
				});
				return false;
			}
		});
	}

	protected synchronized void onActivityResult(int requestCode,
			int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				tv.setText(this.bluetoothEnabledText);
			} else {
				tv.setText(this.bluetoothDisabledText);
			}
		}
	}

	public synchronized void onActivateBt(View v) {
		Thread btActivatorThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mBluetoothAdapter = BluetoothAdapter
										.getDefaultAdapter();
								if (mBluetoothAdapter != null) {
									tv.setText(bluetoothFoundText);
									if (!mBluetoothAdapter.isEnabled()) {
										Intent enableBtIntent = new Intent(
												BluetoothAdapter.ACTION_REQUEST_ENABLE);
										startActivityForResult(enableBtIntent,
												REQUEST_ENABLE_BT);
									} else {
										tv.setText(bluetoothEnabledText);
									}
								} else {
									tv.setText(bluetoothNotFoundText);
								}
							}
						});
					}
				} catch (Exception e) {
				}
			}
		};
		btActivatorThread.start();
	}

	private synchronized void methodSendMessage(ArrayList<Move> moves) {
		String message = "A";
		Move first = moves.get(0);
		Move second = moves.get(1);
		String firstCommand = first.getName().substring(0, 1);
		String secondCommand = second.getName().substring(0, 1);

		int firstValue = first.getValue();
		int secondValue = second.getValue();

		int finalVal1 = firstValue;
		int finalVal2 = secondValue;
		if (firstValue == secondValue && firstValue == 0) {

		} else {
			if (secondValue == 0) {
				if (firstCommand.equals("F"))
					secondCommand = "L";
				else
					secondCommand = "R";
				finalVal2 = finalVal1;
			}
			if (firstValue == 0) {
				if (secondCommand.equals("L")) {
					secondCommand = "R";
				} else {
					secondCommand = "L";
				}
				if (secondCommand.equals("R"))
					firstCommand = "F";
				else
					firstCommand = "B";
				finalVal1 = finalVal2;
			}
			if (firstValue > 0 && secondValue > 0) {
				if (firstCommand.equals("F")) {
					if (secondCommand.equals("R")) {
						secondCommand = "L";
						if (secondValue % 2 != 0)
							return;
						if (firstValue > secondValue) {
						} else {
							firstCommand = "B";
						}
						finalVal1 = firstValue
								- ((int) Math.floor(secondValue / 2));
						finalVal2 = firstValue;
					} else {
						if (secondValue % 2 != 0)
							return;
						if (firstValue > secondValue) {
						} else {
							secondCommand = "R";
						}
						finalVal2 = firstValue
								- ((int) Math.floor(secondValue / 2));
						finalVal1 = firstValue;
					}
				}else{
					if (secondCommand.equals("R")) {
						if (secondValue % 2 != 0)
							return;
						if (firstValue > secondValue) {
						} else {
							firstCommand = "F";
						}
						finalVal1 = firstValue
								- ((int) Math.floor(secondValue / 2));
						finalVal2 = firstValue;
					} else {
						secondCommand = "R";
						if (secondValue % 2 != 0)
							return;
						if (firstValue > secondValue) {
						} else {
							secondCommand = "L";
						}
						finalVal2 = firstValue
								- ((int) Math.floor(secondValue / 2));
						finalVal1 = firstValue;
					}
				}
			}
		}
		/*
		 * finalVal2 = firstValue - secondValue; if (secondValue == 0 &&
		 * firstCommand.equals("F")) { secondCommand = "L"; } else { if
		 * (secondValue == 0 && firstCommand.equals("B")) { secondCommand = "R";
		 * } } if(firstValue==0){ finalVal1=secondValue; finalVal2=secondValue;
		 * secondCommand=(secondCommand.equals("R") ? "L":"R");
		 * if(secondCommand.equals("R")){ firstCommand="F"; } else{
		 * firstCommand="B"; } }
		 */

		message += firstCommand + Math.abs(finalVal1) + secondCommand
				+ Math.abs(finalVal2) + "Z";

		final String messageCpy = message.replace(" ", "");

		// messageCpy.getBytes("ASCII");

		try {
			if (arduinoBluetoothSocket != null) {
				final String mess = message;
				Thread sendToArduinoThread = new Thread() {
					@Override
					public void run() {
						try {
							synchronized (this) {
								if (arduinoBluetoothSocket != null) {
									OutputStream mmOutputStream = arduinoBluetoothSocket
											.getOutputStream();
									mmOutputStream.write(messageCpy
											.getBytes("ASCII"));
								}
								try {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
										}
									});
								} catch (Exception e) {
									// TODO: handle exception
								}
							}
						} catch (Exception e) {
						}
					}
				};
				sendToArduinoThread.start();
			}
		} catch (Exception e) {
		}
	}

	public synchronized void onSync(View v) {
		Thread internetSyncThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						if (inSync == false) {
							inSync = true;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									syncButton.setText("Stop sync");
								}
							});
							while (inSync) {
								// start run
								HttpClient httpclient = new DefaultHttpClient();
								HttpResponse response;
								try {
									response = httpclient.execute(new HttpGet(
											serverEndpoint));
									StatusLine statusLine = response
											.getStatusLine();
									if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
										ByteArrayOutputStream out = new ByteArrayOutputStream();
										response.getEntity().writeTo(out);
										out.close();
										String c = out.toString();
										int cmdIndex = c.indexOf("Value") + 8;
										String command = c.substring(cmdIndex,
												cmdIndex + 1);
										final String responseString = command;
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												tv.setText(responseString);
											}
										});
										try {
											if (responseString.length() > 0) {
												if (arduinoBluetoothSocket != null) {
													OutputStream mmOutputStream = arduinoBluetoothSocket
															.getOutputStream();
													mmOutputStream
															.write(responseString
																	.substring(
																			0,
																			1)
																	.getBytes(
																			"UTF-8"));
													// mmOutputStream.close();
												}
											}
										} catch (Exception e) {
										}
									}
								} catch (Exception e) {
								}
								Thread.sleep(200);
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									syncButton.setText("Sync");
								}
							});

						} else {
							inSync = false;
						}
					}
				} catch (Exception e) {
				}
			}
		};
		internetSyncThread.start();

	}

	public void onSendGoFront(View v) {
		// methodSendMessage("F");
	}

	public void onSendGoBack(View v) {
		// methodSendMessage("B");
	}

	public void onSendGoLeft(View v) {

		// methodSendMessage("L");
	}

	public void onSendGoRight(View v) {

		// methodSendMessage("R");
	}

	public synchronized void onConnectToArduino(View v) {
		Thread connectToArduinoThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						try {

							if (arduinoBluetoothSocket != null) {
								arduinoBluetoothSocket.close();
								arduinoBluetoothSocket = null;
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										connectButton.setText("ArduConnect");
										tv.setText("Arduino Disconnected");
									}
								});
							} else {
								String btDeviceFoundTextCpy = "Arduino Not Found.";
								Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
										.getBondedDevices();
								if (pairedDevices.size() > 0) {
									for (BluetoothDevice device : pairedDevices) {
										String s = device.getName();
										if (device.getName().equals(
												bluetoothPairedDeviceName)) {
											btDeviceFoundTextCpy = "Arduino Found.";
											arduinoBluetoothDevice = device;
											break;
										}
									}
								}
								final String btDeviceFoundText = btDeviceFoundTextCpy;
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										tv.setText(btDeviceFoundText);
									}
								});
								if (arduinoBluetoothDevice != null) {
									try {

										int tries = 4;
										int triesTaken = 0;
										boolean connected = false;
										while (connected == false
												&& triesTaken <= tries) {
											try {
												mBluetoothAdapter
														.cancelDiscovery();
												Method m = arduinoBluetoothDevice
														.getClass()
														.getMethod(
																"createRfcommSocket",
																new Class[] { int.class });
												arduinoBluetoothSocket = (BluetoothSocket) m
														.invoke(arduinoBluetoothDevice,
																1);
												triesTaken++;
												arduinoBluetoothSocket
														.connect();
												connected = true;
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														tv.setText("Arduino Connected");
														connectButton
																.setText("Ardu-connect");
													}
												});
											} catch (Exception e) {
												final int triesTakenDisplay = triesTaken;
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														tv.setText("Retrying to connect"
																+ triesTakenDisplay);
													}
												});
											}
										}

									} catch (final Exception e) {
										arduinoBluetoothDevice = null;
										arduinoBluetoothSocket = null;
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												tv.setText(e.getMessage());
											}
										});
									}
								}
							}
						} catch (final Exception e) {
							arduinoBluetoothDevice = null;
							arduinoBluetoothSocket = null;
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									tv.setText(e.getMessage());
								}
							});
						}
					}

				} catch (Exception e) {
				}
			}
		};
		connectToArduinoThread.start();
	}

	public void onConfigurationChanged(Configuration c) {

	}

	@Override
	protected void onDestroy() {
		if (arduinoBluetoothSocket != null) {
			try {
				arduinoBluetoothSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			arduinoBluetoothSocket = null;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					connectButton.setText("ArduConnect");
					tv.setText("Arduino Disconnected");
				}
			});
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);

		return true;
	}

}
