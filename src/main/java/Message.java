public class Message {
    // 报文类型，规定：0:initialization  1:agreement  2:reverseRequest   3:reverseAnswer
    private short type;
    // 当 type=0 时就是任务书图示里的 N（总块数），2、3 就是传输 data 的长度。
    private int length;
    private String data;

    // 无参构造方法
    public Message() {
    }

    // 有参构造方法
    public Message(short type, int length, String data) {
        this.type = type;
        this.length = length;
        this.data = data;
    }

    // Getter 和 Setter 方法
    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // 重写 toString 方法，方便打印对象信息
    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", length=" + length +
                ", data='" + data + '\'' +
                '}';
    }

    //序列化
    public byte[] serialize() {
        return null;
    }

    //反序列化
    public static Message deserialize(byte[] bytes) {
        return null;
    }
}