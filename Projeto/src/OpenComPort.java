import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;

public class OpenComPort {

    public static void main(String[] args) throws IOException, InterruptedException {

        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.setBaudRate(9600);
        comPort.openPort();
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        new commands(comPort);
        comPort.closePort();
    }
}
