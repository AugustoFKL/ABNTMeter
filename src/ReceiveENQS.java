import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;

public class ReceiveENQS {

    public ReceiveENQS(SerialPort comPort) throws IOException, InterruptedException {

        int ENQs = 0;
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 10000) {
            if (comPort.getInputStream().available() > 0) {
                byte[] reading = new byte[comPort.getInputStream().available()];
                comPort.getInputStream().read(reading);
                for (byte b :
                        reading) {
                    if (b == 0x05) {
                        ENQs = ENQs + 1;
                    }
                }
                if (ENQs == 3) {
                    System.out.println("Received " + ENQs + " ENQs.");
                    new EnvioComando(comPort);
                    break;
                }
            }
        }
    }
}
