package com.example.serial_demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends SerialPortActivity {
    static String serialDevPath = "/dev/ttyS2";//待测试串口路径
    Button sendButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        /*组件初始化*/
        //sendText = this.findViewById(R.id.sendData);
        //recText = this.findViewById(R.id.recData);
        sendButton = this.findViewById(R.id.button2);

        boolean flag = MainActivity.super.OpenSerialPort(serialDevPath , 19200);//打开串口

        if(!flag){
            Toast.makeText(MainActivity.this,"串口打开失败",Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MainActivity.this,"串口打开成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                // if (mReception != null) {
                //数据接收处理
                handleReceivedData(buffer , size);
                // }
            }
        });
    }

    public void sendData(View view) {
        //Toast.makeText(MainActivity.this,"发送数据",Toast.LENGTH_SHORT).show();
        //CharSequence t = sendText.getText();
        // 假设指令是固定的，这里以0x01为例
        byte cmd = 0x53;//开始指令
        // 将CharSequence转换为字节数组
        byte[] data = {0x01};;

        //整合成协议并发送
        Send(cmd , data);

    }
    // 计算校验码
    private static byte calculateFCS(byte[] cmd, byte[] data) {
        byte fcs = 0;
        for (byte b : cmd) {
            fcs ^= b;
        }
        for (byte b : data) {
            fcs ^= b;
        }
        return fcs;
    }

    // 创建数据帧
    public static byte[] createFrame(byte cmd, byte[] data) {
        byte startCode = (byte) 0x02;
        byte stopCode = (byte) 0x03;
        byte fcs = calculateFCS(new byte[]{cmd}, data);

        // 创建帧数据
        byte[] frame = new byte[1 + 1 + data.length + 1 + 1]; // 起始码+指令+数据+校验码+终止码
        frame[0] = startCode;
        frame[1] = cmd;
        System.arraycopy(data, 0, frame, 2, data.length);
        frame[2 + data.length] = fcs;
        frame[3 + data.length] = stopCode;

        return frame;
    }

    //数据发送函数
    public void Send(byte cmd, byte[] data){
        // 创建协议数据帧
        byte[] frame = createFrame(cmd, data);
        // 写入数据帧到串口
        // 将byte数据转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        //显示发送数据看是否正确
        for (byte b : frame) {
            hexString.append(String.format("%02X ", b));
        }
        Toast.makeText(MainActivity.this,hexString,Toast.LENGTH_SHORT).show();

        try {
            mOutputStream.write(frame);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this,"发送失败",Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    //接收数据处理

    // 校验码计算函数
    private byte calculateChecksum(byte[] data, int length) {
        byte checksum = 0;
        for (int i = 0; i < length; i++) {
            checksum ^= data[i];
        }
        return checksum;
    }

    // 串口接收处理函数
    public void handleReceivedData(byte[] receivedData, int size) {
        // 检查数据长度是否符合协议要求
        if (size < 4) { // 最小长度为起始码1字节 + 指令1字节 + 校验码1字节 + 终止码1字节
            System.out.println("数据长度不够，有问题");
            return;
        }

        // 提取各部分数据
        byte startCode = receivedData[0];
        byte command = receivedData[1]; // 指令为1字节
        int dataLength = size - 4; // 数据长度，可能为0
        byte[] data = new byte[dataLength];
        if (dataLength > 0) {
            System.arraycopy(receivedData, 2, data, 0, dataLength);
        }
        byte receivedChecksum = receivedData[size - 2];
        byte endCode = receivedData[size - 1];

        // 验证起始码和终止码
        if (startCode != 0x02 || endCode != 0x03) {
            System.out.println("数据帧格式有问题");
            return;
        }

        // 计算校验码并验证
        byte calculatedChecksum = calculateChecksum(new byte[]{startCode, command}, 2);
        if (dataLength > 0) {
            calculatedChecksum ^= calculateChecksum(data, dataLength);
        }
        if (calculatedChecksum != receivedChecksum) {
            System.out.println("校验码不匹配");
            return;
        }

        // 处理指令，在此处进行不同的命令操作
        handleCommand(command, data);
    }
    // 指令处理函数
    private void handleCommand(byte command, byte[] data) {
        // 这里可以根据指令的不同执行不同的操作
        //暂时未写
    }
}