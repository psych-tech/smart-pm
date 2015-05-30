package com.emolance.service.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

import com.emolance.domain.Report;

public class ParsePushUtil {

	private static final String APPLICATION_ID = "LLqu5BZybPgJ0ntI1aS5Ra3Z47VgMjaX8GG1lbxU";
	private static final String REST_API_KEY = "P5me26nQPEVb21uLioVFe9sE6PsLGU9o2m0DVsHU";
	private static final String PARSE_ENDPOINT = "https://api.parse.com/1";

	public static RestAdapter restAdapter = new RestAdapter.Builder()
	  .setEndpoint(PARSE_ENDPOINT)
	  .build();

	public static ParseAPI parseAPI = restAdapter.create(ParseAPI.class);

	public interface ParseAPI {
		@POST("/push")
		@Headers({
		    "X-Parse-Application-Id: " + APPLICATION_ID,
		    "X-Parse-REST-API-Key: " + REST_API_KEY,
		    "Content-Type: application/json"
		})
		public Response sendPushNotification(@Body ParsePushData data);


	}

	public static class ParsePushData {

		private List<String> channels;
		private Map<String, Object> data;

		public List<String> getChannels() {
			return channels;
		}
		public void setChannels(List<String> channels) {
			this.channels = channels;
		}
		public Map<String, Object> getData() {
			return data;
		}
		public void setData(Map<String, Object> data) {
			this.data = data;
		}
	}

//	public static void main(String[] args) {
//		String[] channels = new String[]{""};
//		String type = "android";
//
//		Report report = new Report();
//		report.setTimestamp(new DateTime());
//		report.setValue(new BigDecimal(123));
//		Map<String, Object> data = new HashMap<String, Object>();
//		data.put("alert", "test");
//		data.put("value", report.getValue().toString());
//		data.put("timestamp", report.getTimestamp().toString());
//
//
//		ParsePushData push = new ParsePushData();
//		push.setChannels(Arrays.asList(channels));
//		push.setData(data);
//
//		try {
//			Response r = parseAPI.sendPushNotification(push);
//			System.out.println(r.getStatus());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public static void sendPushNotification(String login, Report report) {
		String[] channels = new String[]{"user_" + login};
		//String type = "android";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("alert", "Here is your latest pressure value!");
		data.put("value", report.getValue().toString());
		data.put("timestamp", report.getTimestamp().toString());

		ParsePushData push = new ParsePushData();
		push.setChannels(Arrays.asList(channels));
		push.setData(data);

		try {
			Response r = parseAPI.sendPushNotification(push);
			System.out.println(r.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void sendPost(String[] channels, String type, Map<String, String> data) throws Exception {
//		JSONObject jo = new JSONObject();
//		jo.put("channels", channels);
//		if(type != null) {
//			//??type?????android?ios???
//			jo.put("type", type);
//		}
//		jo.put("data", data);
//
//		this.pushData(jo.toString());
//	}

//	private void pushData(String postData) throws Exception {
//		DefaultHttpClient httpclient = new DefaultHttpClient();
//		HttpResponse response = null;
//		HttpEntity entity = null;
//		String responseString = null;
//		HttpPost httpost = new HttpPost(PUSH_URL);
//		httpost.addHeader("X-Parse-Application-Id", APPLICATION_ID);
//		httpost.addHeader("X-Parse-REST-API-Key", REST_API_KEY);
//		httpost.addHeader("Content-Type", "application/json");
//		StringEntity reqEntity = new StringEntity(postData);
//		httpost.setEntity(reqEntity);
//		response = httpclient.execute(httpost);
//		entity = response.getEntity();
//		if (entity != null) {
//			responseString = EntityUtils.toString(response.getEntity());
//		}
//
//		System.out.println(responseString);
//	}
}
