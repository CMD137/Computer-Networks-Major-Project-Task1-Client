import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {
    //注：该文本有785个字符  UTF-8(非BOM)
    private static String fileName = "src/main/resources/origin.txt";
    public static void main(String[] args) {
        //TODO 开发时先直接写
        /*
        // 检查参数数量是否正确
        if (args.length != 4) {
            System.err.println("参数错误：需要提供 server IP, server port, Lmin, Lmax");
            System.err.println("用法：java Client <serverIP> <serverPort> <Lmin> <Lmax>");
            System.exit(1);
        }
        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        int Lmin = Integer.parseInt(args[2]);
        int Lmax = Integer.parseInt(args[3]);
        */

        //TODO 最后完成后注释
        String serverIP = "127.0.0.1";
        int serverPort = 10086;
        int Lmin = 10;
        int Lmax = 100;


        //1.打开文件进行分段并计算N
        File origin = new File(fileName);

        // 检查文件是否存在
        if (!origin.exists()) {
            System.out.println("错误：文件不存在，路径为：" + fileName);
            return;
        }
        int fileLength = (int)origin.length();
        System.out.println("fileLength:" + fileLength);

        //读文件,得到文本字符串
        byte[] allBytes = new byte[fileLength];
        try (FileInputStream fis =new FileInputStream(origin)){
            allBytes = fis.readAllBytes(); // 按字节读取整个文件
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String  text= new String(allBytes, StandardCharsets.UTF_8);
        System.out.println(text);

        //分块,加入消息队列
        List<Message> messagesList = new ArrayList<>();
        int N=0;
        int tempTotalLength = fileLength;
        Random random=new Random();
        while (tempTotalLength>0){
            int length=Lmin+random.nextInt(Lmax-Lmin);
            //test:
            //System.out.println("total"+tempTotalLength+'\t'+length);
            if (tempTotalLength>=length){
                String content=text.substring(0,length);
                text=text.substring(length);

                Message message=new Message((short)3,length,content);
                messagesList.add(message);

                tempTotalLength-=length;

                //test:
                //System.out.println(N+":"+length+":"+content);
            }else{
                String content=text.substring(0,tempTotalLength);
                text=text.substring(tempTotalLength);

                Message message=new Message((short)3,tempTotalLength,content);
                messagesList.add(message);

                //test:
                //System.out.println(N+":"+tempTotalLength+":"+content);

                tempTotalLength=0;

            }
            N++;
        }

        System.out.println("总块数："+N);


        //2.建立连接
        StringBuilder result=new StringBuilder();
        try (Socket socket = new Socket(serverIP, serverPort);
             InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {

            //初始化信息
            Message firstMessage = new Message((short)1, N);
            sendMessage(outputStream, firstMessage);

            //接收Accept
            Message firstResponse = receiveMessage(inputStream);
            if (firstResponse.getType() != 2) {
                System.out.println("服务器拒绝连接或消息出错");
                return;
            }

            //3.循环N次对话，关闭连接
            int index=1;
            for (Message message:messagesList){
                sendMessage(outputStream, message);
                Message response = receiveMessage(inputStream);
                result.insert(0, response.getData());
                System.out.println("第"+index+"块："+response.getData());
                index++;

                //为了展示多线程运行的，不然新的还没启动就结束了。
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            System.out.println("服务器连接异常");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String reverseText= result.toString();
        System.out.println("反转："+reverseText);

        //4.写入文件
        //文件名添加时间戳以方便多线程测试：
        String resultFile = "src/main/resources/result_"+System.currentTimeMillis()+".txt";
        try (FileOutputStream fos = new FileOutputStream(resultFile)){
            fos.write(reverseText.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessage(OutputStream out, Message message) throws IOException {
        byte[] data = message.serialize();
        out.write(data);
        out.flush();
    }

    private static Message receiveMessage(InputStream in) throws IOException {
        byte[] typeBytes = new byte[2];
        byte[] lengthBytes = new byte[4];

        // 先读type2Byte
        int bytesRead = 0;
        while (bytesRead < typeBytes.length) {
            int count = in.read(typeBytes, bytesRead, typeBytes.length - bytesRead);
            if (count == -1) throw new EOFException("连接已关闭");
            bytesRead += count;
        }
        // 解析type
        ByteBuffer buffer = ByteBuffer.wrap(typeBytes);
        short type = buffer.getShort();

        // 根据type决定后续读取长度:client只会收到2,4
        int dataLength = 0;
        if (type == 2) {
            //直接返回一个agree
            return new Message((short) 2);
        } else if (type == 4) {
            // type=4时，再读四字节：
            bytesRead = 0;
            while (bytesRead < lengthBytes.length) {
                int count = in.read(lengthBytes, bytesRead, lengthBytes.length - bytesRead);
                if (count == -1) throw new EOFException("连接已关闭");
                bytesRead += count;
            }
            buffer = ByteBuffer.wrap(lengthBytes);
            dataLength = buffer.getInt();
        } else {
            throw new IOException("未知消息类型: " + type);
        }

        // 读取文本
        byte[] data = new byte[dataLength];
        bytesRead = 0;
        while (bytesRead < dataLength) {
            int count = in.read(data, bytesRead, dataLength - bytesRead);
            if (count == -1) throw new EOFException("连接已关闭");
            bytesRead += count;
        }

        // 合并头部和数据部分
        byte[] fullData = new byte[2+4+dataLength];
        System.arraycopy(typeBytes, 0, fullData, 0, 2);
        System.arraycopy(lengthBytes, 0, fullData, 2, 4);
        System.arraycopy(data, 0, fullData, 6, dataLength);

        return Message.deserialize(fullData);
    }
}
