package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import data.Message;
import data.Storage;

public class Main {

	private static int PORT = 8081;

	private static String ROOT = "d:/www";

	private static void sendFile(String filename, OutputStream output) throws IOException {
		InputStream is = new FileInputStream(ROOT + "/" + filename);
		do {// выводим весь файл
			int b = is.read();
			if (b == -1) {
				break;
			}
			output.write(b);
		} while (true);
		output.flush();
	}

	private static String cleaning(String json) {// заменяет %22 на ковычку
		while (true) {
			int position = json.indexOf("%22");
			if (position == -1) {
				break;
			}
			json = json.substring(0, position) + "'" + json.substring(position + 3);
		}
		return json;
	}

	private static void inject(JSONObject object, OutputStream output) throws IOException {
		OutputStreamWriter bw = new OutputStreamWriter(output);
		bw.write("<script>");
		bw.write("object=" + object);
		bw.write("</script>");
		bw.flush();
	}

	static HashMap<Date, String> history = new HashMap<>();
	private static Integer client = null;

	private static void map(OutputStream output, String contenttype) throws IOException {
		OutputStreamWriter bw = new OutputStreamWriter(output);
		bw.write("HTTP/1.1 200 OK\n");
		bw.write("Content-Type: " + contenttype + "\n");
		bw.write("\n");
		bw.flush();
	}

	private static void goToChat(HashSet<Integer> group, LinkedList<Message> messages, OutputStream output, int admin,
			int groupId) throws IOException {
		if (group == null) {
			group = new HashSet<>();
		}
		if (!group.contains(client)) {
			group.add(client);
		}
		JSONObject ob = new JSONObject();
		HashMap<Integer, String> users = s1.users();
		JSONObject clients = new JSONObject();

		for (Integer id : users.keySet()) {
			var data = new JSONObject();
			data.put("login", users.get(id));
			data.put("inChat", group.contains(id));
			data.put("isBannedByAdmin", s1.isBannedByAdmin(id, groupId));
			clients.put(id + "", data);
		}
		ob.put("users", clients);
		JSONArray text = new JSONArray();
		for (Message m : messages) {
			JSONObject message = new JSONObject();
			message.put("author", m.getAuthor());
			message.put("text", m.text);
			message.put("date", m.getTimestamp());
			text.put(message);
		}
		// text.putAll(messages);
		ob.put("messages", text);
		ob.put("client", client);
		ob.put("admin", admin);
		inject(ob, output);
		sendFile("Chat.html", output);
	}

	private static Storage s1 = new Storage();

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server started on port " + PORT + "!");
			int count = 0;// номер обращения к серверу
			while (true) {
				// ожидаем подключения
				Socket socket = serverSocket.accept();
				// для подключившегося клиента открываем потоки
				// чтения и записи
				try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						OutputStream output = socket.getOutputStream();) {

					// ждем первой строки запроса
					while (!input.ready())
						;

					// считываем и печатаем все что было отправлено клиентом
					String line = input.readLine();

					String line1 = line.substring("GET /".length());
					String dataFromFrontEnd = line1.substring(0, line1.indexOf(" "));
					String contenttype;
					int questionIndex = dataFromFrontEnd.indexOf("?");
					String path;
					String json;
					JSONObject object;
					if (questionIndex > -1) {
						path = dataFromFrontEnd.substring(0, questionIndex);
						json = cleaning(dataFromFrontEnd.substring(questionIndex + "json=".length() + 1));
						object = new JSONObject(json);
					} else {
						path = dataFromFrontEnd;
						json = null;
						object = null;
					}
					int pointindex = path.indexOf('.');
					String extension;
					if (pointindex == -1) {
						extension = "";
					} else {
						extension = path.substring(pointindex + 1);
					}
					System.out.println("Extension=" + extension);
					switch (extension) {
					case "", "html":
						count++;
						System.out.println("-----------------Client-" + count + " is connected!------");
						contenttype = "text/html; charset=utf-8";
						break;
					case "jpg":
						System.out.println("Frontend asks for picture " + dataFromFrontEnd + "------------");
						contenttype = "image/jpeg";
						break;
					case "css":
						System.out.println("Frontend asks for style " + dataFromFrontEnd + "------------");
						contenttype = "text/css";
						break;
					case "js":
						System.out.println("Frontend asks for script " + dataFromFrontEnd + "------------");
						contenttype = "text/javascript";
						break;
					default:
						System.out.println("Frontend asks for " + dataFromFrontEnd + "------------");
						dataFromFrontEnd = null;
						contenttype = null;
					}

					while (input.ready()) {
						line = input.readLine();
					}
					System.out.println("Content type=" + contenttype);
					if (contenttype == null) {
						continue;
					}
					// отправляем ответ
					map(output, contenttype);

					System.out.println("From frontend comes: " + dataFromFrontEnd);

					System.out.println("json=" + json);
					if (extension.equals("")) {
						switch (path) {
						case "":
							if (object == null) {
								sendFile("Authentication.html", output);
							} else {
								if (!object.isNull("mail") && !object.isNull("password")) {
									client = s1.findUser(object.getString("mail"), object.getString("password"));
									if (client == null) {
										sendFile("Authentication.html", output);
									} else {
										goToChat(null, new LinkedList<>(), output, -1, -1);
									}
								} else {
									sendFile("Authentication.html", output);
								}
							}
							break;
						case "Registration":
							if (object != null && !object.isEmpty()) {
								String date = object.getString("date");
								String mail = object.getString("mail");
								String code = System.currentTimeMillis() + "";
								int res = s1.addUser(mail, object.getString("login"), date, code);
								if (res > -1) {
									
									sendFile("Congratulations.html", output);
									new EmailSender(mail, "Your password: " + code);
								} else {
									JSONObject o = new JSONObject();
									o.put("error", "Login " + object.getString("login")
											+ " or email " + object.getString("mail") + " is not vacant!");
									inject(o, output);
									sendFile("Registration.html", output);
									
									System.out.print(o.getString("error"));
								}
							} else {
								sendFile("Registration.html", output);
							}
							break;
						case "sendPassword":
							String mail = object.getString("mail");
							sendFile("Authentication.html", output);
							new EmailSender(mail, "Your password: " + s1.getPassword(mail));
							break;
						case "Profile":
							int id;
							JSONObject o = new JSONObject();
							if (object != null && !object.isNull("id")) {// смотрим профиль
								id = object.getInt("id");
							} else {// случай когда редактируем свой профиль
								id = client;
								if (object != null && !object.isNull("login")) {// сохраняем
									s1.saveProfile(id, object.getString("login"), object.getString("status"));
								}
								o.put("mail", s1.getMail(id));
							}

							o.put("login", s1.getLogin(id));
							o.put("status", s1.getStatus(id));
							// o.put("id",id);
							inject(o, output);
							sendFile("Profile.html", output);
							break;
						case "Chat":
							HashSet<Integer> group = new HashSet<>();
							int groupId;
							int admin = -1;
							if (object != null) {
								JSONArray inChat = object.getJSONArray("group");
								for (int i = 0; i < inChat.length(); i++) {
									group.add(inChat.getInt(i));
								}
								groupId = s1.getGroup(group);
								if (groupId > -1) {
									admin = s1.getAdmin(groupId);
								}
								if (!object.isNull("ban")) {
									s1.ban(object.getInt("ban"), groupId, client == s1.getAdmin(groupId));
								}
								if (!object.isNull("unban")) {
									s1.unban(object.getInt("unban"), groupId);
								}
								if (!object.isNull("message")) {
									String message = object.getString("message");
									if (groupId == -1) {
										admin = client;
										groupId = s1.createGroup(group, admin);
									}
									s1.createMessage(message, groupId, client);
								}
							} else {
								groupId = -1;
							}

							goToChat(group, s1.getMessages(groupId), output, admin, groupId);
							break;
						case "Exit":
							client = null;
							sendFile("Authentication.html", output);
						}

					} else {
						sendFile(path, output);
					}

				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}