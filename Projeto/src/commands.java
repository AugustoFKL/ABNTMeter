import Util.ByteArrayUtils;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.text.MessageFormat;

public class commands {
    long start = System.currentTimeMillis();
    byte[] commandArray = new byte[66];


    public commands(SerialPort comPort) throws IOException, InterruptedException {
        boolean received = false;
        setArray(commandArray);
        if (checkPort(comPort, 0)) {
            received = receivingenqs(comPort);
        }
        if(received){
            sendCommand(comPort);
        }

    }

    public void setArray(byte[] commandArray){
        commandArray[0] = 0x21;
        commandArray[1] = 0x12;
        commandArray[2] = 0x34;
        commandArray[3] = 0x56;
    }

    boolean checkPort(SerialPort comPort, int i) throws IOException {

        long start = System.currentTimeMillis();
        boolean available = false;
        while (System.currentTimeMillis() - start < 10000) {
            if (comPort.getInputStream().available() > i) {
                available = true;
                break;
            }
        }
        return available;
    }

    public boolean receivingenqs(SerialPort comPort) throws IOException {
        int enqs = 0;
        boolean received = false;
        while(clock() && !received){
            byte[] reading = readInputStream(comPort);
            for (byte b :
                    reading) {
                if (b == 0x05) {
                    enqs = enqs + 1;
                }
                if (enqs == 3) {
                    received = true;
                    break;
                }
            }
        }
        return received;
    }

    public byte[] readInputStream(SerialPort comPort) throws IOException {
        byte[] b = new byte[comPort.getInputStream().available()];
        comPort.getInputStream().read(b);
        return b;
    }

    public void sendCommand(SerialPort comPort) throws IOException, InterruptedException {

        generateCRC(commandArray);

        comPort.getOutputStream().write(commandArray);

        Thread.sleep(1000);

        if(checkPort(comPort,1)){
            receiveAnswer(comPort);
        }


    }

    public void receiveAnswer(SerialPort comPort) throws IOException {
        boolean keep = true;
        while (clock() && keep) {
            byte[] reading = readInputStream(comPort);

            for (byte b : reading) {
                if (b == 0x21 && checkCRC(reading)) {
                    if(checkCRC(reading)){
                        printInfo(reading);
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

    public void printInfo(byte[] reading) {
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

    public boolean checkCRC(byte[] reading) {
        String crc = ByteArrayUtils.byteToHex(reading[reading.length - 1]) + ByteArrayUtils.byteToHex(reading[reading.length - 2]);
        int crcInt = Integer.parseInt(crc, 16);
        return CRC16CAS.check(crcInt, reading[257], reading[256]);
    }

    public void generateCRC(byte[] command) {
        int CRC = CRC16CAS.calculate(command, 0, 64);
        byte msb = CRC16CAS.getMSB(CRC);
        byte lsb = CRC16CAS.getLSB(CRC);
        command[64] = lsb;
        command[65] = msb;
    }

    public boolean clock(){
        return System.currentTimeMillis() - start < 10000;
    }
}