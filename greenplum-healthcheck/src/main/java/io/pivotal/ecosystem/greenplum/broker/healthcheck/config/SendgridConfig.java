package io.pivotal.ecosystem.greenplum.broker.healthcheck.config;

import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendgridConfig {
	private String username;
	private String password;

	public SendgridConfig() {
		JSONObject vcap = new JSONObject(System.getenv("VCAP_SERVICES"));
		JSONObject sendgrid = vcap.getJSONArray("sendgrid").getJSONObject(0);
		JSONObject credentials = sendgrid.getJSONObject("credentials");
		this.username = credentials.getString("username");
		this.password = credentials.getString("password");

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
