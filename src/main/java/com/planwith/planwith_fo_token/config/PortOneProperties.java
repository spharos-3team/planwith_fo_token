package com.planwith.planwith_fo_token.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portone")
public class PortOneProperties {

	private boolean stubEnabled = true;
	private String storeId = "";
	private String channelKey = "";
	private String apiSecret = "";
	private String apiBaseUrl = "https://api.portone.io";

	public boolean isStubEnabled() {
		return stubEnabled;
	}

	public void setStubEnabled(boolean stubEnabled) {
		this.stubEnabled = stubEnabled;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getChannelKey() {
		return channelKey;
	}

	public void setChannelKey(String channelKey) {
		this.channelKey = channelKey;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public String getApiBaseUrl() {
		return apiBaseUrl;
	}

	public void setApiBaseUrl(String apiBaseUrl) {
		this.apiBaseUrl = apiBaseUrl;
	}

	public boolean isConfiguredForLiveApi() {
		return !stubEnabled
				&& storeId != null && !storeId.isBlank()
				&& channelKey != null && !channelKey.isBlank()
				&& apiSecret != null && !apiSecret.isBlank();
	}
}
