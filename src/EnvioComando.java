import Util.ByteArrayUtils;
import com.fazecast.jSerialComm.SerialPort;
import java.io.IOException;

public class EnvioComando {
    public EnvioComando(SerialPort comPort) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        boolean keep = true;
        byte[] command = new byte[] {0x21, 0x12, 0x34, 0x56, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, (byte) 0xaa};

        comPort.getOutputStream().write(command);
        Thread.sleep(1000);

        while(System.currentTimeMillis()-start<10000 && keep){
            if(comPort.getInputStream().available()>1){
                byte[] reading = new byte[comPort.getInputStream().available()];
                int len = comPort.getInputStream().read(reading);
                for (byte b: reading){
                    if (b == 0x21) {
                        System.out.println(ByteArrayUtils.byteToHex(reading));
                        keep = false;
                        break;
                    }
                }
            }
        }
    }
}
