package br.com.ntxdev.zup.util;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.FILE_PATH;
import static org.acra.ReportField.INSTALLATION_ID;
import static org.acra.ReportField.IS_SILENT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.util.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class SentrySender implements ReportSender {

	private SentryConfig config;
	public static final ReportField[] SENTRY_TAGS_FIELDS = { APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME, FILE_PATH, PHONE_MODEL,
			BRAND, PRODUCT, ANDROID_VERSION, TOTAL_MEM_SIZE, AVAILABLE_MEM_SIZE, IS_SILENT, USER_APP_START_DATE, USER_CRASH_DATE,
			INSTALLATION_ID };

	public SentrySender(String sentryDSN) {
		if (sentryDSN == null) {
			return;
		}
		config = new SentryConfig(sentryDSN);
	}

	public SentrySender() {
		if (ACRA.getConfig().formKey() == null) {
			return;
		}
		config = new SentryConfig(ACRA.getConfig().formKey());
	}

	@Override
	public void send(CrashReportData errorContent) throws ReportSenderException {

		if (config == null) {
			return;
		}

		final HttpRequest request = new HttpRequest();
		request.setConnectionTimeOut(ACRA.getConfig().connectionTimeout());
		request.setSocketTimeOut(ACRA.getConfig().socketTimeout());
		request.setMaxNrRetries(ACRA.getConfig().maxNumberOfRequestRetries());

		Hashtable<String, String> headers = new Hashtable<String, String>();
		headers.put("X-Sentry-Auth", buildAuthHeader());
		request.setHeaders(headers);

		try {
			request.send(config.getSentryURL(), Method.POST, buildJSON(errorContent), org.acra.sender.HttpSender.Type.JSON);
		} catch (MalformedURLException e) {
			throw new ReportSenderException("Error while sending report to Sentry.", e);
		} catch (IOException e) {
			throw new ReportSenderException("Error while sending report to Sentry.", e);
		} catch (JSONException e) {
			throw new ReportSenderException("Error while sending report to Sentry.", e);
		}
	}

	protected String buildAuthHeader() {
		StringBuilder header = new StringBuilder();
		header.append("Sentry sentry_version=3");
		header.append(",sentry_client=ACRA");
		header.append(",sentry_timestamp=");
		header.append(new Date().getTime());
		header.append(",sentry_key=");
		header.append(config.getPublicKey());
		header.append(",sentry_secret=");
		header.append(config.getSecretKey());

		return header.toString();
	}

	public String getTimestampString() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US);
		return df.format(new Date());
	}

	private String buildJSON(CrashReportData report) throws JSONException {
		JSONObject obj = new JSONObject();

		String message = report.getProperty(ReportField.STACK_TRACE);
		obj.put("event_id", report.getProperty(ReportField.REPORT_ID));

		obj.put("culprit", message);

		obj.put("level", "error");
		obj.put("timestamp", getTimestampString());

		obj.put("message", message);

		obj.put("logger", "org.acra");
		obj.put("platform", "android");
		obj.put("tags", remap(report, SENTRY_TAGS_FIELDS));
		if (ACRA.getConfig().customReportContent().length > 0) {
			obj.put("extra", remap(report, ACRA.getConfig().customReportContent()));
		}

		if (ACRA.DEV_LOGGING) {
			ACRA.log.d(ACRA.LOG_TAG, obj.toString());
		}

		return obj.toString();
	}

	private JSONObject remap(CrashReportData report, ReportField[] fields) throws JSONException {

		final JSONObject result = new JSONObject();
		for (ReportField originalKey : fields) {
			result.put(originalKey.toString(), report.getProperty(originalKey));
			ACRA.log.d(ACRA.LOG_TAG, originalKey.toString() + ": " + report.getProperty(originalKey));
		}
		return result;
	}

	private class SentryConfig {

		private String host, protocol, publicKey, secretKey, path;
		private int port;

		public SentryConfig(String sentryDSN) {

			try {
				URL url = new URL(sentryDSN);
				this.host = url.getHost();
				this.protocol = url.getProtocol();
				String urlPath = url.getPath();

				int lastSlash = urlPath.lastIndexOf("/");
				this.path = urlPath.substring(0, lastSlash);

				String userInfo = url.getUserInfo();
				String[] userParts = userInfo.split(":");

				this.secretKey = userParts[1];
				this.publicKey = userParts[0];

				this.port = url.getPort();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		}

		public URL getSentryURL() throws MalformedURLException {
			StringBuilder serverUrl = new StringBuilder();
			serverUrl.append(getProtocol());
			serverUrl.append("://");
			serverUrl.append(getHost());
			if ((getPort() != 0) && (getPort() != 80) && getPort() != -1) {
				serverUrl.append(":").append(getPort());
			}
			serverUrl.append(getPath());
			serverUrl.append("/api/store/");
			return new URL(serverUrl.toString());
		}

		public String getHost() {
			return host;
		}

		public String getProtocol() {
			return protocol;
		}

		public String getPublicKey() {
			return publicKey;
		}

		public String getSecretKey() {
			return secretKey;
		}

		public String getPath() {
			return path;
		}

		public int getPort() {
			return port;
		}
	}
}
