//Michael Ly Cs380

import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;
import java.net.*;

public class Ipv4Client {

    public static void main(String[] args)
    {
        try{
            Socket socket = new Socket("codebank.xyz",38003);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            byte[] Ipv4Packet = new byte[20];
            Arrays.fill(Ipv4Packet, (byte)0);

            //local host ip
            byte[] source = {127, 0, 0, 1};
           //  String dest = socket.getInetAddress().getHostAddress(); gets the ip address of the socket which is
            byte[] destaddr = {52, 37, 88, (byte) 154};

            for(int j = 0; j < source.length; j++)
            {
                Ipv4Packet[12+j] = source[j];
            }
            for(int j = 0; j < destaddr.length; j++)
            {
                Ipv4Packet[16+j] = destaddr[j];
            }

            for(int i = 0; i < 12; i++)
            {
                int length = (int) Math.pow(2,i+1);
                System.out.println("data length: " + length);

                //version and header
                int version = 4;
                int hLen = 5;
                byte fbyte = (byte) ((version << 4 & 0xF0) | (hLen & 0xF));
                Ipv4Packet[0] = fbyte;

                //no tos

                //Min. length is 20, start off by adding 2
                int Tlength = 20 + (int)Math.pow(2,i+1);
                Ipv4Packet[2] = (byte)((Tlength >> 8) & 0xFF);
                Ipv4Packet[3] = (byte)(Tlength & 0xFF);

                //no id

                //flag, assume no frag so 010
                Ipv4Packet[6] = (byte) 64;

                //no offset

                //ttl
                Ipv4Packet[8] = (byte) 50;

                //protocol = tcp
                Ipv4Packet[9] = (byte) 6;

                //Needed to reset checksum for next iteration
                Ipv4Packet[10] = (byte) 0;
                Ipv4Packet[11] = (byte) 0;

                //source addr
                for(int j = 0; j < source.length; j++)
                {
                    Ipv4Packet[12+j] = source[j];
                }

                //dest addr
                for(int j = 0; j < destaddr.length; j++)
                {
                    Ipv4Packet[16+j] = destaddr[j];
                }

                //calcualte checksum
                short headerchksum = checksum(Ipv4Packet);
            //    System.out.printf("\nChecksum calculated: 0x%02X\n", headerchksum );

                ByteBuffer bcs = ByteBuffer.allocate(2);
                bcs.putShort(headerchksum);

             //   System.out.printf("0x%02X\n", bcs.array()[0]);
            //    System.out.printf("0x%02X\n", bcs.array()[1]);

                //checksum, two bytes
                Ipv4Packet[10] = (byte) (bcs.array()[0]);
                Ipv4Packet[11] = (byte) (bcs.array()[1]);

                String serverres;

                for(byte b : Ipv4Packet) {
                    os.write(b);
                }

                //data
                for(int j = 0; j < length; j++)
                {
                    os.write(0);
                }

                //server response
                serverres = br.readLine();

                if(serverres.equals("good"))
                {
                    System.out.println("good\n");
                }
                else{
                    System.out.println(serverres);
                    System.exit(1);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //from ex.3
    //checksum alg.
    public static short checksum(byte[] b) {
        long sum = 0;
        long temp1, temp2;

        for (int i = 0; i < b.length / 2; i++) {
            temp1 = (b[(i*2)] << 8) & 0xFF00;
            temp2 = (b[(i*2) + 1]) & 0xFF;
            sum += (long) (temp1 + temp2);
            if ((sum & 0xFFFF0000) > 0) {
                sum &= 0xFFFF;
                sum++;
            }
        }

        //handler for odd length byte array
        if (b.length % 2 == 1)
        {
            sum += ((b[b.length-1] << 8) & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) {
                sum &= 0xFFFF;
                sum++;
            }
        }
        return (short) ~(sum & 0xFFFF);
    }
}
