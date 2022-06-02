package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Storage {

	static boolean MYSQL = true;

	private Connection connection;

	public Storage() {
		registerDriverManager();

		try {
			String password = "postgres";
			String login;
			int port;
			String drivername;
			String basename;
			String ip = "127.0.0.1";
			if (MYSQL) {
				login = "root";
				port = 3306;
				drivername = "mysql";
				basename = "rgr";
			} else {
				login = "postgres";
				port = 5432;
				drivername = "postgresql";
				basename = "postgres";
			}
			connection = DriverManager.getConnection("jdbc:" + drivername + "://" + ip + ":" + port + "/" + basename,
					login, password);
			System.out.println("Соединение установлено.");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void registerDriverManager() {
		String driverName = MYSQL ? "com.mysql.cj.jdbc.Driver" : "org.postgresql.Driver";
		try {
			Class.forName(driverName).newInstance();
			System.out.println("драйвер " + driverName + " установлен");
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Boolean isBannedByAdmin(int userId, int chatId) {
		try {
			Statement s = connection.createStatement();
			String where = " where userInChat=" + userId + " and chatId=" + chatId;
			ResultSet r = s.executeQuery("select * from membership" + where);
			if (r.next()) {
				return r.getBoolean("isBanned");
			} else {
				return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void ban(int userId, int chatId,boolean admin) {
		try {
			Statement s = connection.createStatement();
			String where = " where userInChat=" + userId + " and chatId=" + chatId;
			ResultSet r = s.executeQuery("select * from membership" + where);
			if (r.next()) {
				s.executeUpdate("update membership set isBanned=" + admin + where);

			} else {
				s.executeUpdate("insert into membership (chatId,userInChat, isBanned) values (" + chatId + "," + userId
						+ "," + admin + ")");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void unban(int userId, int chatId) {
		try {
			Statement s = connection.createStatement();
			String where = " where userInChat=" + userId + " and chatId=" + chatId;
			s.executeUpdate("delete from membership"+where);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int newId(String table, Statement s) throws SQLException {
		ResultSet r1 = s.executeQuery("select * from " + table);
		int id = -1;
		while (r1.next()) {
			int newId = r1.getInt("id");
			if (newId > id) {
				id = newId;
			}
		}
		return id + 1;
	}

	public int addUser(String mail, String login, String dateOfBirth, String password) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from users where login='" + login + "' or mail='" + mail + "'");
			System.out.println("Add");
			if (!r.next()) {
				int id = newId("users", s);
				System.out.println("insert into users (id,mail,login,dateOfBirth,password) values (" + id + ",'" + mail
						+ "','" + login + "','" + dateOfBirth + "','" + password + "')");
				s.executeUpdate("insert into users (id,mail,login,dateOfBirth,password) values (" + id + ",'" + mail
						+ "','" + login + "','" + dateOfBirth + "','" + password + "')");
				return id;
			}
			return -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}

	}

	public HashMap<Integer, String> users() {
		HashMap<Integer, String> result = new HashMap<>();
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from users");
			while (r.next()) {
				result.put(r.getInt("id"), r.getString("login"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public Integer findUser(String mail, String password) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s
					.executeQuery("select * from users where mail='" + mail + "' and password='" + password + "'");
			if (r.next()) {
				return r.getInt("id");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean saveProfile(int id, String login, String status) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from users where login='" + login + "'");

			if (r.next() && r.getInt("id") != id) {
				return false;
			} else {
				s.execute("update users set login='" + login + "', status='" + status + "' where id=" + id);
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getLogin(int id) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from users where id=" + id);
			if (r.next()) {
				return r.getString("login");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPassword(String mail) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from users where mail='" + mail+"'");
			if (r.next()) {
				return r.getString("password");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getGroup(HashSet<Integer> group) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from chat where name='" + name(group) + "'");
			if (r.next()) {
				return r.getInt("id");
			}
			return -1;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	private int max(HashSet<Integer> members) {
		int res = 0;
		for (Integer i : members) {
			if (i > res) {
				res = i;
			}
		}
		return res;
	}

	// айдишники участников группы по возрастанию
	private String name(HashSet<Integer> members1) {
		HashSet<Integer> members = new HashSet<>();
		for (int i : members1) {
			members.add(i);
		}
		String res = "";
		while (members.size() > 0) {
			int m = max(members);
			members.remove(m);
			if (res.length() > 0) {
				res = " " + res;
			}
			res = m + res;
		}
		return res;
	}

	public int createGroup(HashSet<Integer> members, int admin) {
		String name = name(members);
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from chat where name='" + name + "'");
			if (r.next()) {
				return r.getInt("id");
			}
			int id = newId("chat", s);
			s.executeUpdate("insert into chat (id,admin,name) values (" + id + "," + admin + ",'" + name + "')");
			return id;

		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public int getAdmin(int chatId) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from chat where id=" + chatId);
			if (r.next()) {
				return r.getInt("admin");
			}
			return -1;

		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public LinkedList<Message> getMessages(int group) {
		LinkedList<Message> res = new LinkedList<>();
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from message where chatId=" + group);
			while (r.next()) {
				Message message = new Message(r.getString("textOfMessage"), r.getTimestamp("date"),getLogin(r.getInt("who")));
				res.add(message);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void createMessage(String text, int group, int author) {
		try {
			Statement s = connection.createStatement();
			Message message = new Message(text,getLogin(author));
			s.executeUpdate("insert into message (textOfMessage,date,additionalContent,chatId,who) values ('" + text
					+ "','" + message.getTimestamp() + "',''," + group + "," + author + ")");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getStatus(int id) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from users where id=" + id);
			if (r.next()) {
				return r.getString("status");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getMail(int id) {
		try {
			Statement s = connection.createStatement();
			ResultSet r = s.executeQuery("select * from users where id=" + id);
			if (r.next()) {
				return r.getString("mail");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getStatus() {
		String status = "HELLO";
		return status;
	}

}
