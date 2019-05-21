package com.dnake.v700;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;

import com.dnake.misc.SysTalk;
import com.dnake.misc.sCaller;
import com.dnake.misc.tsl;
import com.dnake.panel.FaceLabel;
import com.dnake.panel.SDTLabel;
import com.dnake.panel.TalkLabel;
import com.dnake.panel.WakeTask;

public class devent {
	private static List<devent> elist = null;
	public static Boolean boot = false;

	public String url;

	public devent(String url) {
		this.url = url;
	}

	public void process(String xml) {
	}

	public static void event(String url, String xml) {
		Boolean err = true;
		if (boot && elist != null) {
			devent e;

			Iterator<devent> it = elist.iterator();
			while (it.hasNext()) {
				e = it.next();
				if (url.equals(e.url)) {
					e.process(xml);
					err = false;
					break;
				}
			}
		}
		if (err)
			dmsg.ack(480, null);
	}

	public static void setup() {
		elist = new LinkedList<devent>();

		devent de;
		de = new devent("/ui/run") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
			}
		};
		elist.add(de);

		de = new devent("/ui/version") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				String v = String.valueOf(sys.version_major) + "." + sys.version_minor + "." + sys.version_minor2;
				v = v + " " + sys.version_date + " " + sys.version_ex;
				p.setText("/params/version", v);
				p.setInt("/params/proxy", sys.qResult.sip.proxy);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/start") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				sCaller.reset();
				sCaller.running = sCaller.RINGING;
				sCaller.mId = p.getText("/params/host");
				sCaller.refresh();

				TalkLabel.mMode = TalkLabel.IN;
				SysTalk.start();
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/stop") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				sCaller.bStop = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/play") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				SysTalk.play();
				sCaller.running = sCaller.TALK;
				sCaller.refresh();
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/ringing") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				sCaller.mId = p.getText("/params/host");
				sCaller.running = sCaller.RINGING;
				sCaller.refresh();

				TalkLabel.mMode = TalkLabel.OUT;
				SysTalk.start();
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/register") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				sys.qResult.sip.proxy = p.getInt("/params/register", 0);
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/query") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				if (sys.qResult.sip.url == null) {
					dxml p = new dxml();
					p.parse(body);
					sys.qResult.sip.url = new String(p.getText("/params/url"));
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/device/query") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				if (sys.qResult.d600.ip == null) {
					dxml p = new dxml();
					p.parse(body);
					sys.qResult.d600.host = p.getText("/params/name");
					sys.qResult.d600.ip = p.getText("/params/ip");
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/result") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				sys.qResult.result = p.getInt("/params/result", 0);
			}
		};
		elist.add(de);

		de = new devent("/ui/broadcast/data") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				//dxml p = new dxml();
				//p.parse(body);
			}
		};
		elist.add(de);

		de = new devent("/ui/ipwatchd") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				int result = p.getInt("/params/result", 0);
				String ip = p.getText("/params/ip");
				String mac = p.getText("/params/mac");
				SysTalk.ipMacErr(result, ip, mac);
			}
		};
		elist.add(de);

		de = new devent("/ui/touch/event") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				WakeTask.acquire();
				login.refresh();
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/key") {
			private long ts = 0;

			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				String key = p.getText("/params/data");
				if (key.charAt(0) == '*') {
					if (Math.abs(System.currentTimeMillis() - ts) > 3 * 1000) {
						dmsg req = new dmsg();
						dxml p2 = new dxml();
						p2.setInt("/params/key", 4);
						req.to("/settings/key", p2.toString());
					}
					ts = System.currentTimeMillis();
				}
				SysTalk.Keys.offer(key);
				SysTalk.touch(key);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/keyCall") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				if (p.getInt("/params/data", 0) == 0) { // 按下
					WakeTask.acquire();
					SysTalk.Keys.offer("X");
					SysTalk.touch("X");
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/unlock") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dmsg req = new dmsg();
				req.to("/face/unlock", body);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/elev/appoint") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dmsg req = new dmsg();
				req.to("/control/elev/appoint", body);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/elev/permit") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dmsg req = new dmsg();
				req.to("/control/elev/permit", body);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/elev/join") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
			}
		};
		elist.add(de);

		de = new devent("/ui/sdt/detect") { // 身份证读取
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				WakeTask.acquire();

				if (SDTLabel.mSdtThread == null) {
					SDTLabel.mSdtThread = new SDTLabel.SdtThread();
					SDTLabel.mSdtThread.body = body;
					Thread t = new Thread(SDTLabel.mSdtThread);
					t.start();
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/sdt/result") { // 人证比对结果
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				if (SDTLabel.mContext != null) {
					dxml p = new dxml();
					p.parse(body);
					String id = p.getText("/params/id");
					int data = p.getInt("/params/data", 0);
					String url = p.getText("/params/url");
					SDTLabel.mContext.setResult(id, data, url);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/face/reboot") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				FaceLabel.mStartVo = true;
				SDTLabel.mStartVo = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/face/detect") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				WakeTask.acquire();
				FaceLabel.mTs = System.currentTimeMillis();
				if (FaceLabel.mContext == null) {
					Intent it = new Intent(SysTalk.mContext, FaceLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SysTalk.mContext.startActivity(it);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/face/result") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dxml p = new dxml();
				p.parse(body);
				FaceLabel.mFaceUid = p.getInt("/params/id", -1);
				FaceLabel.mFaceSim = p.getInt("/params/sim", -1);
				FaceLabel.mFaceBlack = p.getInt("/params/black", 0); // 0:正常 1:黑名单
				FaceLabel.mFaceUrl = p.getText("/params/url");
				FaceLabel.mFaceTs = new Date();
				FaceLabel.mFaceCms = false;
				FaceLabel.mFaceHave = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/cms/result") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				FaceLabel.mFaceUid = p.getInt("/params/id", 0);
				FaceLabel.mFaceSim = p.getInt("/params/data", 0);
				FaceLabel.mFaceUrl = p.getText("/params/url");
				FaceLabel.mFaceTs = new Date();
				FaceLabel.mFaceCms = true;
				FaceLabel.mFaceHave = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/web/tsl/read") {
			@Override
			public void process(String body) {
				tsl.load();
				dxml p = new dxml();
				p.setText("/params/ProductKey", tsl.mProductKey);
				p.setText("/params/License", tsl.mLicense);
				p.setText("/params/MQTT", tsl.mMQTT);
				p.setText("/params/BaseUrl", tsl.mBaseUrl);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/web/tsl/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dxml p = new dxml(body);
				tsl.mProductKey = p.getText("/params/ProductKey", tsl.mProductKey);
				tsl.mLicense = p.getText("/params/License", tsl.mLicense);
				tsl.mMQTT = p.getText("/params/MQTT", tsl.mMQTT);
				tsl.mBaseUrl = p.getText("/params/BaseUrl", tsl.mBaseUrl);
				tsl.save();
			}
		};
		elist.add(de);
	}
}
