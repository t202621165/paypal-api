package com.iwanol.paypal.vo;

import com.alibaba.fastjson.JSONObject;

public enum Result {
	
	success("成功", true, ""), error("失败", false,"");
	
	private String msg;
	
	private Boolean state;
	
	private Object data;
	
	public JSONObject toJson(){
		return JSONObject.parseObject(JSONObject.toJSONString(this));
	}
	
	public JSONObject getJson(){
		JSONObject json = new JSONObject();
		json.put("msg", this.msg);
		json.put("state", this.state);
		json.put("data", this.data);
		return json;
	}
	
	public JSONObject toJson(String msg){
		this.msg = msg;
		return getJson();
	}
	
	public JSONObject toJson(String msg,Object data){
		this.msg = msg;
		this.data = data;
		return getJson();
	}
	
	private Result(String msg, Boolean state) {
		this.msg = msg;
		this.state = state;
	}

	private Result(String msg, Boolean state, Object data) {
		this.msg = msg;
		this.state = state;
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
