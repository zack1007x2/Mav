package com.paku.mavlinkhub.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.MAVLink.Messages.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.ardupilotmega.msg_rc_channels_override;
import com.paku.mavlinkhub.R;
import com.paku.mavlinkhub.enums.MSG_SOURCE;
import com.paku.mavlinkhub.queue.items.ItemMavLinkMsg;
import com.paku.mavlinkhub.view.CusJoystickView;
import com.zerokol.views.JoystickView;

public class FragmentJoystickControl extends HUBFragment {

	private JoystickView joystick_right;
	private CusJoystickView joystick_left;
	private short cur_lift, cur_rotate, ch1, ch2;
	private static final int DEFAULT_PACKET = 1;
	private static final int DIRECTION_PACKET = 2;
	private static final int PRINT_PARAM = 3;
	private static final int BOOM = 4;
	private Thread defaultLoop;
	private boolean keeploopong, isTouching_left, isTouching_right;
	private TextView tvRL, tvFB, tvPower, tvRotate;
	private ToggleButton tbRC;
	private int boom;
	private boolean startUp;

	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DEFAULT_PACKET:
				msg_rc_channels_override cmd = new msg_rc_channels_override();
				cmd.chan1_raw = ch1;
				cmd.chan2_raw = ch2;
				cmd.chan3_raw = cur_lift;
				cmd.chan4_raw = cur_rotate;
				cmd.target_component = -66;
				cmd.target_system = 1;
				Log.d("Zack", "前後 = " + cmd.chan2_raw + " 左右 = " + cmd.chan1_raw + " 升力 = " + cmd.chan3_raw + " 自轉 = " + cmd.chan4_raw);
				ItemMavLinkMsg mav_msg = new ItemMavLinkMsg(cmd.pack(), MSG_SOURCE.FROM_GS, 1);
				hub.queue.addHubQueueItem(mav_msg);
				this.sendEmptyMessage(PRINT_PARAM);
				break;
			case DIRECTION_PACKET:
				msg_rc_channels_override cmd_active = new msg_rc_channels_override();
				cmd_active.chan1_raw = ch1;
				cmd_active.chan2_raw = ch2;
				cmd_active.chan3_raw = cur_lift;
				cmd_active.chan4_raw = cur_rotate;
				cmd_active.target_component = -66;
				cmd_active.target_system = 1;
				Log.d("Zack", "前後 = " + cmd_active.chan2_raw + " 左右 = " + cmd_active.chan1_raw + " 升力 = " + cmd_active.chan3_raw + " 自轉 = " + cmd_active.chan4_raw);
				ItemMavLinkMsg msg_active = new ItemMavLinkMsg(cmd_active.pack(), MSG_SOURCE.FROM_GS, 1);
				hub.queue.addHubQueueItem(msg_active);
				this.sendEmptyMessage(PRINT_PARAM);
				break;
			case PRINT_PARAM:
				tvRL.setText("Roll\n" + String.valueOf(ch1));
				tvFB.setText("Pitch\n" + String.valueOf(ch2));
				tvPower.setText("Throttle\n" + String.valueOf(cur_lift));
				tvRotate.setText("Yaw\n" + String.valueOf(cur_rotate));
				break;
			case BOOM:
				msg_rc_channels_override cmd_start = new msg_rc_channels_override();
				cmd_start.chan1_raw = ch1;
				cmd_start.chan2_raw = ch2;
				cmd_start.chan3_raw = cur_lift;
				cmd_start.chan4_raw = cur_rotate;
				cmd_start.target_component = -66;
				cmd_start.target_system = 1;
				Log.d("Zack", "前後 = " + cmd_start.chan2_raw + " 左右 = " + cmd_start.chan1_raw + " 升力 = " + cmd_start.chan3_raw + " 自轉 = " + cmd_start.chan4_raw);
				ItemMavLinkMsg mav__start_up_msg = new ItemMavLinkMsg(cmd_start.pack(), MSG_SOURCE.FROM_GS, 1);
				hub.queue.addHubQueueItem(mav__start_up_msg);
				this.sendEmptyMessage(PRINT_PARAM);
				cur_lift = 1000;
				startUp = false;
				break;
			}
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_joystick_control, container, false);

		joystick_left = (CusJoystickView) (rootView.findViewById(R.id.joystick_left));
		joystick_right = (JoystickView) (rootView.findViewById(R.id.joystick_right));

		tvRL = (TextView) (rootView.findViewById(R.id.tvRL));
		tvFB = (TextView) (rootView.findViewById(R.id.tvFB));
		tvPower = (TextView) (rootView.findViewById(R.id.tvPower));
		tvRotate = (TextView) (rootView.findViewById(R.id.tvRotate));
		tbRC = (ToggleButton) (rootView.findViewById(R.id.tbRC));

		joystick_left.setOnJoystickMoveListener(new com.paku.mavlinkhub.view.CusJoystickView.OnJoystickMoveListener() {

			@Override
			public void onValueChanged(int angle, int power, int direction) {
				isTouching_left = true;
				if (power >= 99) {
					power = 100;
				}
				Log.d("Zack", "direction = " + direction);
				switch (direction) {
				case JoystickView.FRONT:
					cur_lift = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					cur_rotate = 1500;
					break;
				case JoystickView.FRONT_RIGHT:
					cur_lift = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					cur_rotate = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.RIGHT:
					cur_lift = 1500;
					cur_rotate = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.RIGHT_BOTTOM:
					cur_lift = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					cur_rotate = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.BOTTOM:
					cur_lift = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					cur_rotate = 1500;
					break;
				case JoystickView.BOTTOM_LEFT:
					cur_lift = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					cur_rotate = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.LEFT:
					cur_lift = 1500;
					cur_rotate = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.LEFT_FRONT:
					cur_lift = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					cur_rotate = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					break;
				default:
				}
				if (!isTouching_right) {
					ch1 = 1500;
					ch2 = 1500;
				}
				if (tbRC.isChecked()) mHandler.sendEmptyMessage(DIRECTION_PACKET);
			}

			@Override
			public void onNotTouch() {
				Log.d("Zack", "isTouching_left = false;");
				isTouching_left = false;
			}

		}, JoystickView.DEFAULT_LOOP_INTERVAL);

		joystick_right.setOnJoystickMoveListener(new com.zerokol.views.JoystickView.OnJoystickMoveListener() {

			@Override
			public void onValueChanged(int angle, int power, int direction) {
				isTouching_right = true;
				if (power >= 99) {
					power = 100;
				}
				switch (direction) {
				case JoystickView.FRONT:
					ch1 = 1500;
					ch2 = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.FRONT_RIGHT:
					ch1 = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					ch2 = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.RIGHT:
					ch1 = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					ch2 = 1500;
					break;
				case JoystickView.RIGHT_BOTTOM:
					ch1 = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					ch2 = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.BOTTOM:
					ch1 = 1500;
					ch2 = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.BOTTOM_LEFT:
					ch1 = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					ch2 = (short) (1500 + (Integer.valueOf(5 * power).shortValue()));
					break;
				case JoystickView.LEFT:
					ch1 = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					ch2 = 1500;
					break;
				case JoystickView.LEFT_FRONT:
					ch1 = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					ch2 = (short) (1500 - (Integer.valueOf(5 * power).shortValue()));
					break;
				default:
					ch1 = 1500;
					ch2 = 1500;
				}

				if (!isTouching_left) {
					cur_rotate = 1500;
				}
				if (tbRC.isChecked()) mHandler.sendEmptyMessage(DIRECTION_PACKET);
			}

			@Override
			public void onNotTouch() {
				Log.d("Zack", "isTouching_right = false;");
				isTouching_right = false;
			}
		}, JoystickView.DEFAULT_LOOP_INTERVAL);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public void onPause() {
		super.onPause();
		keeploopong = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		keeploopong = true;
		startUp = true;
		boom = 0;
		defaultLoop = new Thread(new Runnable() {
			public void run() {
				while (keeploopong) {
					try {
						Thread.sleep(500);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!isTouching_left && !isTouching_right && tbRC.isChecked()) {
						ch1 = 1500;
						ch2 = 1500;
						cur_rotate = 1500;

						if (startUp) {
							cur_lift = 1000;
						}

						Log.d("Zack", "cur_lift = " + cur_lift);
						if (boom == 3 || boom == 5) {
							cur_lift = 1050;
							mHandler.sendEmptyMessage(BOOM);

						}
						else {
							mHandler.sendEmptyMessage(DEFAULT_PACKET);
						}
						if (!(boom > 5)) {
							boom++;
						}

					}
				}

			}
		});
		defaultLoop.start();
	}
}
