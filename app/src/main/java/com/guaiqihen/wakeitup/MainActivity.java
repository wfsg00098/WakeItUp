package com.guaiqihen.wakeitup;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("HandlerLeak")
    private Handler ToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG + 5).show();
        }
    };

    private static byte[] hexToBinary(String hexString){
        //1.定义变量：用于存储转换结果的数组
        byte[] result = new byte[hexString.length()/2];

        //2.去除字符串中的16进制标识"0X"并将所有字母转换为大写
        hexString = hexString.toUpperCase().replace("0X", "");

        //3.开始转换
        //3.1.定义两个临时存储数据的变量
        char tmp1 = '0';
        char tmp2 = '0';
        //3.2.开始转换，将每两个十六进制数放进一个byte变量中
        for(int i = 0; i < hexString.length(); i += 2){
            tmp1 = hexString.charAt(i);
            tmp2 = hexString.charAt(i+1);
            result[i/2] = (byte)((hexToDec(tmp1)<<4)|(hexToDec(tmp2)));
        }
        return result;
    }

    private static byte hexToDec(char c){
        return (byte)"0123456789ABCDEF".indexOf(c);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn = findViewById(R.id.button);
        final EditText mac = findViewById(R.id.editText);

        class WakeUp extends Thread{
            @Override
            public void run(){
                String MacAddr = mac.getText().toString();
                String ip = "255.255.255.255";//广播IP地址
                int port = 40000;//端口号
                //魔术包数据
                String magicPackage = "0xFFFFFFFFFFFF" +
                        MacAddr + MacAddr + MacAddr + MacAddr +
                        MacAddr + MacAddr + MacAddr + MacAddr +
                        MacAddr + MacAddr + MacAddr + MacAddr +
                        MacAddr + MacAddr + MacAddr + MacAddr ;
                //转换为2进制的魔术包数据
                byte[] command = hexToBinary(magicPackage);
                Message msg1 = new Message();
                msg1.obj = magicPackage;
                ToastHandler.sendMessage(msg1);

                try{
                    //1.获取ip地址
                    InetAddress address = InetAddress.getByName(ip);
                    //2.获取广播socket
                    MulticastSocket socket = new MulticastSocket(port);
                    //3.封装数据包
                    /*public DatagramPacket(byte[] buf,int length
                     *      ,InetAddress address
                     *      ,int port)
                     * buf：缓存的命令
                     * length：每次发送的数据字节数，该值必须小于等于buf的大小
                     * address：广播地址
                     * port：广播端口
                    */
                    DatagramPacket packet = new DatagramPacket(command, command.length, address, port);
                    //4.发送数据
                    socket.send(packet);
                    //5.关闭socket
                    socket.close();
                }catch (Exception e){
                    Message msg = new Message();
                    msg.obj = e.toString();
                    ToastHandler.sendMessage(msg);
                }

            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new WakeUp().start();
                //Toast.makeText(MainActivity.this,"已发送数据报",Toast.LENGTH_LONG+5).show();
            }
        });

    }

}