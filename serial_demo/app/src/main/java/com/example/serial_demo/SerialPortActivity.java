package com.example.serial_demo;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.serial_demo.seria.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;


public abstract class SerialPortActivity extends Activity {
    protected Application mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    /**
     * 读取数据
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    byte[] actualData = null;
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        actualData = new byte[size];
                        System.arraycopy(buffer, 0, actualData, 0, size);
                        // 使用 actualData 作为实际读取到的数据
                        //将数据送进数据处理函数进行解析
                        onDataReceived(actualData, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //SerialPortActivity.this.finish();
            }
        });
        b.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public boolean OpenSerialPort(String path, int baudrate) {
        try {
            mSerialPort = getSerialPort(path, baudrate);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            /* Create a receiving thread 创建一个接受数据的线程 */
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            //DisplayError(R.string.error_security);
            return false;
        } catch (IOException e) {
            //DisplayError(R.string.error_unknown);
            return false;
        } catch (InvalidParameterException e) {
            //DisplayError(R.string.error_configuration);
            return false;
        }
        return true;
    }

    public SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            // mSerialPort = new SerialPort(new File("/dev/ttySAC1"), 9600, 0);
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    //重写接收函数,在这个函数中进行数据协议解析
    protected abstract void onDataReceived(final byte[] buffer, final int size);

}
