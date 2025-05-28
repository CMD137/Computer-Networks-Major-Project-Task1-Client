import java.io.*;
import java.net.Socket;
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
        try {
            Socket socket=new Socket(serverIP,serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            //初始化信息
            Message firstMessage=new Message((short) 1,N);
            writer.println(firstMessage.serialize());
            byte[] bytes = reader.readLine().getBytes(StandardCharsets.UTF_8);
            Message firstResponse=Message.deserialize(bytes);

            if (firstResponse.getType()!=2){
                System.out.println("服务器拒绝连接");
                return;
            }
            //3.循环N次对话，关闭连接
            for (Message message:messagesList){
                writer.println(message.serialize());
                byte[] responseBytes = reader.readLine().getBytes(StandardCharsets.UTF_8);
                Message response=Message.deserialize(responseBytes);
                result.insert(0,response.getData());
            }

            writer.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("服务器连接异常");
            throw new RuntimeException(e);
        }

        String reverseText= result.toString();
        System.out.println(reverseText);

        //4.写入文件
        try (FileOutputStream fos = new FileOutputStream("src/main/resources/result.txt")){
            fos.write(reverseText.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
