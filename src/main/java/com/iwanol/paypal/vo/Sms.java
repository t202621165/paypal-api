package com.iwanol.paypal.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="sms对象",discriminator="接收sms消息的对象")
public class Sms {
	@ApiModelProperty(name="appId",value="腾讯云appId",example = "1400026980")
	private Integer appId;
	@ApiModelProperty(name="appKey",value="腾讯云密钥",example = "413ef0456665971b62fb82479849715d")
	private String appKey; //密钥
	@ApiModelProperty(name="paramOne",value="参数一(验证码)",example = "512685")
	private String paramOne;
	@ApiModelProperty(name="paramTwo",value="参数二(有效时间)",example = "5")
	private String paramTwo;
	@ApiModelProperty(name="telPhone",value="手机号码",example = "13165985421")
	private String telPhone;
	@ApiModelProperty(name="templateId",value="业务模板Id",example="10012")
	private Integer templateId;
	@ApiModelProperty(name="type",value="发送类型",example="yzm or txyzm")
	private String type;
	
	public Sms() {
		super();
	}
	


	public Integer getAppId() {
		return appId;
	}



	public void setAppId(Integer appId) {
		this.appId = appId;
	}




	public String getAppKey() {
		return appKey;
	}


	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}


	public String getParamOne() {
		return paramOne;
	}


	public void setParamOne(String paramOne) {
		this.paramOne = paramOne;
	}


	public String getParamTwo() {
		return paramTwo;
	}


	public void setParamTwo(String paramTwo) {
		this.paramTwo = paramTwo;
	}


	public String getTelPhone() {
		return telPhone;
	}


	public void setTelPhone(String telPhone) {
		this.telPhone = telPhone;
	}	


	public Integer getTemplateId() {
		return templateId;
	}


	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	
}
