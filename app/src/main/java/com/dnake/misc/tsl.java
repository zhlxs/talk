package com.dnake.misc;

import com.dnake.v700.dxml;

public class tsl {
	public static String mProductKey = "c3ccf9d78f9eaf8b887177192a7cf63d";
	public static String mLicense = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvfFCet7XT+pUDqe8Xo1w/4h2gBD0fnlNu83IqReWzUmL64vrejIPu12UklW+BWtryWy7wJLSk24WLQVC3pYNcVNwvsP3nHjImFMSVeVLw1DftE/2T9RyPhXPx0vwnLHQbNcYrskJKmHg+3kQYJiaWzDItylkSpqmwjgjJaFXqDQIDAQAB";
	public static String mMQTT = "tcp://testmqtt-iot.tslsmart.com:1883";
	public static String mBaseUrl = "http://testapi.tslsmart.com";

	public static void load() {
		dxml p = new dxml();
		p.load("/dnake/cfg/gateway_config.xml");
		mProductKey = p.getText("/Root/ProductKey", mProductKey);
		mLicense = p.getText("/Root/License", mLicense);
		mMQTT = p.getText("/Root/MQTT", mMQTT);
		mBaseUrl = p.getText("/Root/BaseUrl", mBaseUrl);
	}

	public static void save() {
		dxml p = new dxml();
		mProductKey = p.getText("/Root/ProductKey", mProductKey);
		mLicense = p.getText("/Root/License", mLicense);
		mMQTT = p.getText("/Root/MQTT", mMQTT);
		mBaseUrl = p.getText("/Root/BaseUrl", mBaseUrl);
		p.save("/dnake/cfg/gateway_config.xml");
	}
}
