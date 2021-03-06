/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package decrypting;

import javacard.framework.*;

/**
 *
 * @author user
 */
public class Decrypting extends Applet {
    public final static byte CRYPTO_CLA     = (byte)0xA0;
    public final static byte CRYPTO_DECRYPT = (byte)0xB2;
    public final static byte CRYPTO_GETKEY = (byte)0xB4;
    
    PrivateKeyManager keym = null;
    short messageLength = (short)0;
    
    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Decrypting();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected Decrypting() {
        keym = new PrivateKeyManager((short)64);
        register();
    }

    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     */
    public void process(APDU apdu) {
        //Insert your code here
        byte [] buffer = apdu.getBuffer();

        if(this.selectingApplet())
            return;

        if(buffer[ISO7816.OFFSET_CLA] != CRYPTO_CLA)
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);


        switch(buffer[ISO7816.OFFSET_INS]){
            case CRYPTO_GETKEY:
                messageLength = keym.getPublicKey(buffer, ISO7816.OFFSET_CDATA);
                apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, messageLength);
                break;
            case CRYPTO_DECRYPT:
                messageLength = apdu.setIncomingAndReceive();
                messageLength = keym.decryptPrivate(buffer,ISO7816.OFFSET_CDATA,messageLength);
                apdu.setOutgoing();
                apdu.setOutgoingLength(messageLength);
                apdu.sendBytesLong(buffer, ISO7816.OFFSET_CDATA, messageLength);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
