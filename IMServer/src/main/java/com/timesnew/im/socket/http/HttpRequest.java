package com.timesnew.im.socket.http;


import com.alibaba.fastjson.JSON;
import com.timesnew.im.socket.entity.LoginParam;

import java.io.InputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Http请求
 * 该类的每一个实例用于表示客户端发送过来的一个
 * 请求内容.
 *
 * @author adminitartor
 */
public class HttpRequest {
    private Socket socket;
    private InputStream in;

    /*
     * 请求行相关信息定义
     */
    //请求的方式
    private String method;
    //请求的资源路径
    private String url;
    //请求路径中的请求部分(url中"?"左面的内容)
    private String requestURI;
    //请求路径中的参数部分(url中"?"右面的内容)
    private String queryString;

    //请求所使用的协议版本
    private String protocol;

    /*
     * 消息头相关信息定义
     */
    private Map<String, String> headers = new HashMap<>();


    /*
     * 客户端传递过来的参数内容
     * key:参数名
     * value:参数值
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * 初始化HttpRequest
     */
    public HttpRequest(Socket socket) {
        try {
            this.socket = socket;
            //通过socket获取输入流读取客户端发送的请求内容
            this.in = socket.getInputStream();
            /*
             * 开始解析请求内容
             * 1:解析请求行
             * 2:解析消息头
             * 3:解析消息正文
             */
            //1 解析请求行
            parseRequestLine();
            //2 解析消息头
            parseHeaders();
            //3 解析消息正文
            parseContent();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析消息正文
     */
    private void parseContent() {
        /*
         * 当一个请求包含消息正文时,会在消息头中
         * 出现Content-Length说明长度,以及
         * Content-Type说明内容类型.
         * 若消息头中不含有它们说明没有消息正文
         */
        try {
            if (this.headers.containsKey("Content-Length")) {
                int len = Integer.parseInt(
                        this.headers.get("Content-Length")
                );
                byte[] data = new byte[len];
                in.read(data);
                //获取消息正文内容的类型
                String type = this.headers.get("Content-Type");
                //判断是否为post提交的json数据
                if ("application/json".equals(type)) {
                    String str = new String(data, "ISO8859-1");
                    str = URLDecoder.decode(str, "UTF-8");
                    LoginParam loginParam = JSON.parseObject(str, LoginParam.class);
                    parseLoginParam(loginParam);
                }
                /*
                 * 判断是否为post提交的form表单数据
                 */
                if ("application/x-www-form-urlencoded".equals(type)) {
                    /*
                     * 这些字节实际上表示的内容就是用GET请求
                     * 提交form表单时在url中"?"右侧的参数部分.
                     */
                    String str = new String(data, "ISO8859-1");
                    //再进一步按照UTF-8将"%XX"的形式解码
                    str = URLDecoder.decode(str, "UTF-8");
                    //解析form表单参数
                    parseParameters(str);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseLoginParam(LoginParam loginParam) {
        List<String> idList = new ArrayList<>();
        idList.add("001");
        idList.add("002");
        idList.add("003");
        idList.add("004");
        String platform = loginParam.getPlatform();
        String userId = loginParam.getUserId();
        if (null != platform && "".equals(platform)){
            switch (platform) {
                case "ios":
                    System.out.println("ios端登录");
                    break;
                case "android":
                    System.out.println("android端登录");
                    break;
                case "web":
                    System.out.println("web端登录");
                    break;
                default:
                    System.out.println("反正你登录了");
            }
        }
        if (null != userId && "".equals(userId)){

        }
    }


    /**
     * 解析消息头
     */
    private void parseHeaders() {
        System.out.println("开始解析消息头");
        /*
         * 循环读取每一行(若干消息头),当读取的这行
         * 字符串是空字符串时,说明单独读取了CRLF,
         * 那么就可以停止读取消息头操作.
         *
         * 每读取一个消息头时,将消息头的名字作为key
         * 消息头的值作为value,存入到headers这个map
         * 中.最终完成解析消息头工作
         */
        String line = null;
        while (true) {
            line = readLine();
            if ("".equals(line)) {
                break;
            }
            String[] arr = line.split(":\\s");
            headers.put(arr[0], arr[1]);
        }
        System.out.println("headers:" + headers);
        System.out.println("解析消息头完毕");
    }

    /**
     * 解析请求行
     */
    private void parseRequestLine() {
        /*
         * 1:通过输入流读取一行字符串,相当于
         *   读取出了请求行内容
         * 2:按照空格拆分请求行,可以得到对应的
         *   三部分内容
         * 3:分别将methid,url,protocol设置到
         *   对应的属性上完成请求行的解析工作
         */
        String line = readLine();
        String[] data = line.split("\\s");

        /*
         * 这里可能出现下标越界错误,后期优化
         */
        this.method = data[0];
        this.url = data[1];
        this.protocol = data[2];

        //进一步解析url
        parseURL();
    }

    /**
     * 进一步解析请求行中的url部分
     * url可能的两种样子如:
     * /myweb/reg.html
     * /myweb/reg?username=123&password=123...
     */
    private void parseURL() {
        try {
            /*
             * 由于请求有两种情况,带参数或不带参数,那么
             * 要先判断url是否带参数,不带则直接将url的
             * 值赋值给属性requestURI即可.
             * 若带参数,则需要按照?先拆分url,然后将?左面
             * 内容设置到requestURI中,将?右面的内容设置
             * 到queryString中.并进一步对参数部分解析,
             * 将每个参数解析出来,将参数名作为key,参数值
             * 作为value存入到parameters这个Map类型的属
             * 性中.
             */
            System.out.println("进一步解析URL");
            /*
             * 将url解码,由于url中在传递像中文这样非
             * ISO8859-1字符集所支持的字符时,会被浏览器
             * 将其中的这些字符以"%XX"的形式转码后发送.
             * 所以,要对url中所有"%XX"内容进行解码
             */
            this.url = URLDecoder.decode(this.url, "UTF-8");

            //判断url中是否含有?
            if (this.url.contains("?")) {
                String[] data = this.url.split("\\?");
                this.requestURI = data[0];
                //确保url中"?"右侧有内容
                if (data.length > 1) {
                    this.queryString = data[1];
                    //解析queryString
                    parseParameters(this.queryString);
                } else {
                    this.queryString = "";
                }
            } else {
                this.requestURI = this.url;
            }

            System.out.println("requestURI:" + requestURI);
            System.out.println("queryString:" + queryString);
            System.out.println("parameter:" + parameters);
            System.out.println("进一步解析URL完毕!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析参数
     * 参数的格式:
     * name=value&name=value&name....
     *
     * @param line
     */
    private void parseParameters(String line) {
        String[] data = line.split("&");
        //将每个参数按照"="拆分并存入parameters
        for (String parameter : data) {
            String[] paraArr = parameter.split("=");
            if (paraArr.length > 1) {
                parameters.put(paraArr[0], paraArr[1]);
            } else {
                parameters.put(paraArr[0], "");
            }
        }
    }


    /**
     * 读取一行字符串,以CRLF结尾为一行
     *
     * @return
     */
    private String readLine() {
        try {
            /*
             * 顺序从in中读取每个字符,当连续读取了
             * CR,LF时停止.并将之前读取的字符以一个
             * 字符串形式返回即可.
             */
            StringBuilder builder = new StringBuilder();
            int d = -1;
            //c1表示上次读取到的字符,c2为本次读取的字符
            char c1 = 'a', c2 = 'a';
            while ((d = in.read()) != -1) {
                c2 = (char) d;
                if (c1 == 13 && c2 == 10) {
                    break;
                }
                builder.append(c2);
                c1 = c2;
            }
            //trim的目的是去除最后的CR符号
            return builder.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取请求的方式
     *
     * @return
     */
    public String getMethod() {
        return method;
    }

    /**
     * 获取请求的资源路径
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取请求使用的协议版本
     *
     * @return
     */
    public String getProtocol() {
        return protocol;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    /**
     * 获取给定名字对应的参数
     *
     * @param name
     * @return
     */
    public String getParameter(String name) {
        return this.parameters.get(name);
    }


}









