package io.github.lbarnkow.jarb.rocketchat.sharedmodel;

import java.util.List;

import io.github.lbarnkow.jarb.misc.Common;

public class RawMessage extends Common {
	private String _id;
	private String t;
	private String rid;
	private String msg;
	private String ts;
	private RawUser u;
	private List<RawAttachment> attachments;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public RawUser getU() {
		return u;
	}

	public void setU(RawUser u) {
		this.u = u;
	}

	public List<RawAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<RawAttachment> attachments) {
		this.attachments = attachments;
	}
}