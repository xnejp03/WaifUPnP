import com.dosse.upnp.UPnP;

/**
 *
 * @author Federico
 */
public class Example1 {
    public static void main(String args[]){
        //this will attempt to open port TCP 4137. all errors will be ignored
        UPnP.openPortTCP("test",30, 4137, 4137);
    }
}
