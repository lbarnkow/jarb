package bot.rocketchat.messages;

import com.google.gson.Gson;

public class Base {
	private static final Gson gson = new Gson();

	private String msg;
	
	protected Base() {
		this(null);
	}
	
	protected Base(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public String toString() {
		return gson.toJson(this);
	}
	
	protected static <T> T parse(String json, Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}
	
	public static Base parse(String json) {
		return parse(json, Base.class);
	}
}
