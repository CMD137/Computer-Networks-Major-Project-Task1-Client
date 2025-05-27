import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class client {
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
        int fileLength = Math.toIntExact(origin.length());
        System.out.println("fileLength:" + fileLength);

        //分块
        List<Integer> lengthList =new ArrayList<>();
        List<Message> messagesList  =   new ArrayList<>();
        int N=0;
        int tempTotalLength = fileLength;
        Random random=new Random();
        while (tempTotalLength>0){
            int length=Lmin+random.nextInt(Lmax-Lmin);
            //System.out.println("total"+tempTotalLength+'\t'+length);
            if (tempTotalLength>=length){
                tempTotalLength-=length;
                lengthList.add(length);

            }else{
                lengthList.add(tempTotalLength);
                tempTotalLength=0;
            }
            N++;
        }

        System.out.println("总块数："+N);
        for (int i = 0; i < N; i++) {
            System.out.println("第"+i+"块长度："+lengthList.get(i));
        }



        //2.建立连接

        //3.开启监听线程

        //4.循环N次，关闭连接
    }
    private class Listener extends Thread{
        public void run(){
            while(true){
                //接收服务器返回的消息
                //判断消息类型
                //处理消息
            }
        }
    }
}
