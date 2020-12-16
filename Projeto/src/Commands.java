import Util.ByteArrayUtils;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.text.MessageFormat;

public class Commands {

    public static void ReceivingENQS(SerialPort comPort) throws IOException, InterruptedException {
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
                    Commands.SendCommand(comPort);
                    break;
                }
            }
        }
    }

    public static void SendCommand(SerialPort comPort) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        boolean keep = true;
        byte[] command = new byte[]{0x21, 0x12, 0x34, 0x56, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        Commands.GenerateCRC(command);

        comPort.getOutputStream().write(command);
        Thread.sleep(1000);

        while (!(System.currentTimeMillis() - start >= 10000) && keep) {
            if (comPort.getInputStream().available() > 1) {
                byte[] reading = new byte[comPort.getInputStream().available()];
                comPort.getInputStream().read(reading);
                for (byte b : reading) {
                    if (b == 0x21 && Commands.CheckCRC(reading)) {
                        if(Commands.CheckCRC(reading)){
                            Commands.PrintInfo(reading);
                        }
                        else{
                            System.out.println("Mensagem inválida: código CRC inválido. ");
                            comPort.closePort();
                        }
                        keep = false;
                        break;
                    }
                }
            }
        }
    }

    public static void PrintInfo(byte[] reading) {
        String command = ByteArrayUtils.byteToHex(reading[0]);
        String serialnumber = MessageFormat.format("{0}{1}{2}{3}", ByteArrayUtils.byteToHex(reading[1]), ByteArrayUtils.byteToHex(reading[2]), ByteArrayUtils.byteToHex(reading[3]), ByteArrayUtils.byteToHex(reading[4]));
        String actualData = reading[5] + ":" + reading[6] + ":" + reading[7] + " " + reading[8] + "/" + reading[9] + "/" + reading[10];
        String lastDemandData = reading[18] + ":" + reading[19] + ":" + reading[20] + " " + reading[21] + "/" + reading[22] + "/" + reading[23];
        String multiplicationsConstantsCh1 = "";
        String multiplicationsConstantsCh2 = "";
        String multiplicationsConstantsCh3 = "";
        for (int i = 128; i <= 133; i = i + 1) {
            multiplicationsConstantsCh1 = multiplicationsConstantsCh1.concat(ByteArrayUtils.byteToHex(reading[i]));
            if (i == 130) {
                multiplicationsConstantsCh1 = multiplicationsConstantsCh1.concat("/");
            }
        }
        for (int i = 134; i <= 139; i = i + 1) {
            multiplicationsConstantsCh2 = multiplicationsConstantsCh2.concat(ByteArrayUtils.byteToHex(reading[i]));
            if (i == 136) {
                multiplicationsConstantsCh2 = multiplicationsConstantsCh2.concat("/");
            }
        }
        for (int i = 140; i <= 145; i = i + 1) {
            multiplicationsConstantsCh3 = multiplicationsConstantsCh3.concat(ByteArrayUtils.byteToHex(reading[i]));
            if (i == 142) {
                multiplicationsConstantsCh3 = multiplicationsConstantsCh3.concat("/");
            }
        }
        String softwareVersion = ByteArrayUtils.byteToHex(reading[147]) + ByteArrayUtils.byteToHex(reading[148]);

        System.out.println("Command: " + command);
        System.out.println("Series number: " + serialnumber);
        System.out.println("Actual data: " + actualData);
        System.out.println("Last demand data: " + lastDemandData);
        System.out.println("Multiplication constants Channel 1: " + multiplicationsConstantsCh1);
        System.out.println("Multiplication constants Channel 2: " + multiplicationsConstantsCh2);
        System.out.println("Multiplication constants Channel 3: " + multiplicationsConstantsCh3);
        System.out.println("Software Version: " + softwareVersion);
    }

    public static boolean CheckCRC(byte[] reading) {
        String crc = ByteArrayUtils.byteToHex(reading[reading.length - 1]) + ByteArrayUtils.byteToHex(reading[reading.length - 2]);
        int crcInt = Integer.parseInt(crc, 16);
        return CRC16CAS.check(crcInt, reading[257], reading[256]);
    }

    public static void GenerateCRC(byte[] command) {
        int CRC = CRC16CAS.calculate(command, 0, 64);
        byte msb = CRC16CAS.getMSB(CRC);
        byte lsb = CRC16CAS.getLSB(CRC);
        command[64] = lsb;
        command[65] = msb;
    }
}