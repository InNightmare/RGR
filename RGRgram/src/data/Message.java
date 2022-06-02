package data;

import java.util.Calendar;
import java.util.Date;

public class Message {
	private Date datetime;
	public final String text;
	private String author;

	public Message(String text,String author) {
		this.text = text;
		datetime = new Date();
		this.author = author;
	}

	Message(String text, Date datetime,String author) {
		this.text = text;
		this.datetime = datetime;
		this.author = author;
	}
	
	public String getAuthor() {
		return author;
		
	}

	public String getTimestamp() {
		Calendar c = Calendar.getInstance();
		c.setTime(datetime);
		String month = c.get(Calendar.MONTH) + 1 + "";
		if (month.length() < 2) {
			month = "0" + month;
		}
		String date = c.get(Calendar.DAY_OF_MONTH) + "";
		if (date.length() < 2) {
			date = "0" + date;
		}
		String hour = c.get(Calendar.HOUR) + "";
		if (hour.length() < 2) {
			hour = "0" + hour;
		}
		String minute = c.get(Calendar.MINUTE) + "";
		if (minute.length() < 2) {
			minute = "0" + minute;
		}
		String second = c.get(Calendar.SECOND) + "";
		if (second.length() < 2) {
			second = "0" + second;
		}
		return c.get(Calendar.YEAR) + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
	}

}
