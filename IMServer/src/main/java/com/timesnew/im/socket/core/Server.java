package com.timesnew.im.socket.core;

import com.timesnew.im.websocket.Chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {

    private ServerSocket server;

    private List<String> userIdList = new ArrayList<>();

    //保存离线消息
    private Queue<String> outLineMessages = new ConcurrentLinkedQueue<>();

    private static Map<String, Socket> userSocketMap = new HashMap<>();

    public static Socket getUserSocketById(String id){
        return userSocketMap.get(id);
    }

    private Map<String, Queue<String>> outLineMessagesMap = new HashMap<>();

//    private static RandomAccessFile randomAccessFile;
//
//    private static File file;

    public Server() {
        try {
            server = new ServerSocket(2000);
            userIdList.add("1");
            userIdList.add("2");
            userIdList.add("3");
//            file = new File("./messages.bat");
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            randomAccessFile = new RandomAccessFile("./messages.bat", "rw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void start() {
        try {
            while (true) {
                System.out.println("等待客户端连接...");
                Socket socket = server.accept();
                System.out.println("一个客户端连接了!");
                //启动一个线程,处理该客户端交互工作
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    /**
     * 获取历史消息
     *
     * @return
     */
//    public Queue<String> getHistoryMessages() throws IOException {
//        Queue<String> queue = new ArrayDeque<>();
//        for (int i = 0; i < randomAccessFile.length() / 100; i++) {
//            randomAccessFile.seek(i * 100);
//            byte[] data = new byte[100];
//            randomAccessFile.read(data);
//            String historyMessage = new String(data, "GBK").trim();
//            System.out.println("historyMessage:" + historyMessage);
//            queue.add(historyMessage);
//        }
//        return queue;
//    }

    /**
     * 判断字符串是否包含数字
     *
     * @param message
     * @return
     */
    public static boolean isContainNumber(String message) {
        String[] str = message.split(":");
        if (str.length > 0) {
            return true;
        }
//        Pattern p = Pattern.compile("[0-9]");
//        Matcher m = p.matcher(message);
//        if (m.find()) {
//            return true;
//        }
        return false;
    }

//    /**
//     * 写入信息
//     *
//     * @param message
//     * @throws IOException
//     */
//    public static void writeMessage(String message) throws IOException {
//        byte[] data = message.getBytes("GBK");
//        data = Arrays.copyOf(data, 100);
//        randomAccessFile.write(data);
//        randomAccessFile.seek(file.length());
//    }

    /**
     * 提取信息
     *
     * @param message
     * @return
     */
    public static String getMessage(String message) {
        return message.split(":")[1];
    }

    /**
     * 提取字符串中数字
     *
     * @param message
     * @return
     */
    public static String getNumber(String message) {
        String[] str = message.split(":");
//        String regex = "[^0-9]";
//        Pattern p = Pattern.compile(regex);
//        Matcher m = p.matcher(message);
//        String number = m.replaceAll("").trim();
        return str[0];
    }

    /**
     * 获取输入流
     *
     * @param socket
     * @return
     * @throws IOException
     */
    public static BufferedReader getBr(Socket socket) throws IOException {
        return new BufferedReader(
                new InputStreamReader(
//                                "UTF-8"
                        socket.getInputStream(),
                        "GBK"
                )
        );
    }

    /**
     * 获取输出流
     *
     * @param socket
     * @return
     * @throws IOException
     */
    public static PrintWriter getPw(Socket socket) throws IOException {
        return new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                socket.getOutputStream(),
//                                        "UTF-8"
                                "GBK"
                        )
                ), true
        );
    }

    /**
     * 该线程任务负责与指定Socket所对应的客户端
     * 进行交互
     */
    private class ClientHandler implements Runnable {

        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            PrintWriter pw = null;
            String id = null;
            try {
                String ip = socket.getInetAddress().toString();
                //获取输入流，用于读取客户端发送的信息
                BufferedReader br = getBr(socket);
                //获取输出流,用于给客户端回复消息
                pw = getPw(socket);
//                String request = getBr(socket).readLine();
//                System.out.println("request:" + request);
//                if (request != null) {
//                    String[] data = request.split("\\s");
//                    String url = data[1];
//                    System.out.println("请求url:" + url);
//                    File file = new File("resource" + url);
//                    if (file.exists()) {
//                        System.out.println("找到该文件");
//                        pw.println("HTTP/1.1 200 OK");// 返回应答消息,并结束应答
//                        pw.println("Content-Type: text/html");
//                        pw.println("Content-Length: " + file.length());// 返回内容字节数
//                        pw.println("");// 根据 HTTP 协议, 空行将结束头信息
//                        FileInputStream fis = new FileInputStream(file);
//                        byte[] d = new byte[1024*10];
//                        int len = -1;
//                        while((len = fis.read(d))!=-1){
//                            socket.getOutputStream().write(d,0,len);
//                        }
//                    }
//                }
                pw.println("请输入您的ID");
                id = br.readLine();
                while (!userIdList.contains(id)) {
                    pw.println("请输入正确ID");
                    id = br.readLine();
                }
                pw.println("ID正确，请发言");

                //查询该用户是否收到离线消息
                if (outLineMessagesMap.containsKey(id)) {
                    pw.println("您有离线消息");
                    Queue<String> nowUserOutLineMessage = outLineMessagesMap.get(id);
                    Iterator<String> iterator = nowUserOutLineMessage.iterator();
                    while (iterator.hasNext()) {
                        pw.println(iterator.next());
                    }
                    Queue<String> queue = outLineMessagesMap.remove(id);
                    System.out.println("清空的离线消息" + queue);
                }
                //把当前用户ID，socket存入map
                synchronized (userSocketMap) {
                    userSocketMap.put(id, socket);
                }

                String message = null;
                while ((message = br.readLine()) != null) {
                    System.out.println(ip + "说:" + message);
                    if (isContainNumber(message)) {
                        System.out.println("当前用户id:" + id);
                        System.out.println("发送到用户id:" + getNumber(message));
//                        //获取历史消息
//                        Queue<String> historyMsg = getHistoryMessages();
//                        if (historyMsg != null) {
//                        pw.println("有历史消息");
//                        Iterator<String> iterator = historyMsg.iterator();
//                        while (iterator.hasNext()) {
//                            pw.println(iterator.next());
//                        }
//                        } else {
//                            pw.println("无历史消息");
//                        }
                        //保存当前用户的信息及发送的消息
//                        writeMessage(id + ":" + getMessage(message) + ":" + new Date());
//                        writeMessage(message + ":" + new Date());
                        System.out.println("指定联系人：" + getNumber(message));
                        Chat chat = Chat.getChatById(Integer.parseInt(getNumber(message)));
                        System.out.println("chat:" + chat);
                        chat.sendMessage(message);
//                        synchronized (userSocketMap) {
//                            if (!userSocketMap.containsKey(getNumber(message))) {
//                                pw.println("该用户未登陆或已下线");
//                                pw.println("是否发送离线消息，同意请按1，不同意请按2");
//                                String code = br.readLine();
//                                if ("1".equals(code)) {
//                                    pw.println("请发送离线消息");
//                                    String outLineMessage = null;
//                                    while ((outLineMessage = br.readLine()) != null) {
//                                        outLineMessages.add(outLineMessage);
//                                        if (!outLineMessagesMap.containsKey(getNumber(message))) {
//                                            outLineMessagesMap.put(getNumber(message), outLineMessages);
//                                        } else {
//                                            outLineMessagesMap.replace(getNumber(message), outLineMessages);
//                                        }
//                                    }
//                                } else {
//                                    pw.println("请您在线联系");
//                                }
//                            } else {
////                                for (int i = 0; i < userSocketMap.size(); i++) {
////                                    Socket socket = userSocketMap.get(getNumber(message));
////                                    pw = getPw(socket);
////                                    pw.println(ip + "说:" + message);
////                                }
//
//                            }
//                        }
                    } else {
                        System.out.println("不指定联系人");
                        synchronized (userSocketMap) {
                            for (Socket socket : userSocketMap.values()
                            ) {
                                pw = getPw(socket);
                                pw.println(ip + "说:" + message);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //处理客户端断开连接后的操作
                synchronized (userSocketMap) {
                    userSocketMap.remove(id);
                }

                /*
                 * 客户端断开连接后,服务端关闭该客户端Socket,释放资源
                 */
                try {
                    socket.close();
//                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}











