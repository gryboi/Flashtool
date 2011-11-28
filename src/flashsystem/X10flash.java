package flashsystem;

import com.sonyericsson.cs.generic.c.c;
import com.sonyericsson.cs.usbflashnative.a.a;
import flashsystem.HexDump;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import org.logger.MyLogger;
import org.system.Device;
import org.system.DeviceIdent;
import org.system.TextFile;

import java.util.Enumeration;
import java.util.Vector;

public class X10flash {

	private static a my_a = null;
    private static int my_ch;
    private Bundle _bundle;
    private Command cmd;

    public X10flash(Bundle bundle) {
    	_bundle=bundle;
    	if (!bundle.simulate()) System.loadLibrary("USBFlash");
    }

    private void setFlashState(boolean ongoing) throws IOException,X10FlashException
    {
	    	if (ongoing) {
	    		cmd.send(Command.CMD13,Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);
	    		cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);    		
	    	}
	    	else {
	    		cmd.send(Command.CMD13, Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	        	cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	    	}
    }

    private void sendTA(InputStream in,String name) throws FileNotFoundException, IOException,X10FlashException {
    	try {
    		TaFile ta = new TaFile(in);
    		MyLogger.getLogger().info("Flashing "+name);
			Vector<TaEntry> entries = ta.entries();
			for (int i=0;i<entries.size();i++) {
				MyLogger.getLogger().info("TA value : "+HexDump.toHex(entries.get(i).getWordbyte()));
				if (!_bundle.simulate()) {
					cmd.send(Command.CMD13, entries.get(i).getWordbyte(),false);
				}
			}
    	}
    	catch (TaParseException tae) {
    		MyLogger.getLogger().error("Error parsing TA file. Skipping");
    	}
    }

    private String testCmd12(int i) throws X10FlashException
    {
        
        return cmd.getLastReplyHex();
    }

    public String getHook() throws IOException, X10FlashException {
	    cmd = new Command(my_a,my_ch,_bundle.simulate());
		cmd.testPlugged();
		String hook = cmd.getLastReplyString();
		cmd.send(Command.CMD01, Command.VALNULL, false);
		cmd.send(Command.CMD09, Command.VAL2, false);
        cmd.send(Command.CMD10, Command.VALNULL, false);
        return hook;
    }

    // added method to retrieved long string returned by loader
    public String getHook2() throws IOException, X10FlashException {
	    cmd = new Command(my_a,my_ch,_bundle.simulate());
		cmd.testPlugged();
		cmd.send(Command.CMD01, Command.VALNULL, false);
		String hook = cmd.getLastReplyString();
		cmd.send(Command.CMD09, Command.VAL2, false);
        cmd.send(Command.CMD10, Command.VALNULL, false);
        return hook;
    }
    
    public String dumpProperty(int prnumber) throws IOException, X10FlashException
    {
    	try {
		    MyLogger.getLogger().info("Start Reading property");
	        MyLogger.getLogger().debug((new StringBuilder("%%% read property id=")).append(prnumber).toString());
	        cmd.send(Command.CMD12, c.a(prnumber, 4, false),false);
	        String reply = cmd.getLastReplyHex();
	        reply = reply.replace("[", "");
	        reply = reply.replace("]", "");
	        reply = reply.replace(",", "");
	        closeDevice();	    
			MyLogger.getLogger().info("Reading property finished.");
			MyLogger.initProgress(0);
			return reply;
	    }
    	catch (Exception ioe) {
    		closeDevice();
    		MyLogger.getLogger().error("Error dumping properties. Aborted");
    		MyLogger.initProgress(0);
    		return "";
    	}    	
    }

    public void dumpProperties() throws IOException, X10FlashException
    {
    	try {
		    MyLogger.getLogger().info("Start Dumping properties");

		    cmd = new Command(my_a,my_ch,_bundle.simulate());
	    	
			cmd.testPlugged();
			cmd.send(Command.CMD01, Command.VALNULL, false);
			cmd.send(Command.CMD09, Command.VAL2, false);
	        cmd.send(Command.CMD10, Command.VALNULL, false);

	        TextFile tazone = new TextFile("./tazone.ta","ISO8859-1");
	        tazone.open(false);
	        TextFile tazoneS = new TextFile("./tazoneString.ta","ISO8859-1");
	        tazoneS.open(false);
	        for(int i = 0; i < 3000; i++)
	        {
	        	MyLogger.getLogger().debug((new StringBuilder("%%% read property id=")).append(i).toString());
	        	cmd.send(Command.CMD12, c.a(i, 4, false),false);
	        	String reply = cmd.getLastReplyHex();
	        	String replyS = cmd.getLastReplyString();
	        	reply = reply.replace("[", "");
	        	reply = reply.replace("]", "");
	        	reply = reply.replace(",", "");
	        	if (cmd.getLastReplyLength()>0) {
	        		tazone.writeln(HexDump.toHex(i) + " " + HexDump.toHex(cmd.getLastReplyLength()) + " " + reply.trim());
	        		tazoneS.writeln(HexDump.toHex(i) + " " + HexDump.toHex(cmd.getLastReplyLength()) + " " + replyS.trim());
	        	}
	        }
	        tazone.close();
	        tazoneS.close();
            
	        closeDevice();
		    
			MyLogger.getLogger().info("Dumping properties finished.");
	    }
    	catch (Exception ioe) {
    		closeDevice();
    		MyLogger.getLogger().error("Error dumping properties. Aborted");
    	}
    }

    private void processHeader(InputStream fileinputstream) throws X10FlashException {
    	byte abyte2[];
    	try {
			byte abyte0[] = new byte[6];
			int j = fileinputstream.read(abyte0);
			if(j != 6) {
				fileinputstream.close();
				throw new X10FlashException("Error in processHeader");
			}
			int k;
			byte abyte1[] = new byte[4];
			System.arraycopy(abyte0, 2, abyte1, 0, 4);
			k = c.c(abyte1, false);
			abyte1 = new byte[k - 6];
			k = fileinputstream.read(abyte1);
			if(k != abyte1.length) {
				fileinputstream.close();
				throw new X10FlashException("Error in processHeader");
			}
			abyte2 = new byte[k + 6];
			System.arraycopy(abyte0, 0, abyte2, 0, 6);
			System.arraycopy(abyte1, 0, abyte2, 6, abyte2.length - 6);
            cmd.send(Command.CMD05,abyte2,false);
            if(cmd.getLastFlags() == 0)
                cmd.send(Command.CMD07,Command.VALNULL,false);
    	}
    	catch (IOException ioe) {
    		throw new X10FlashException("Error in processHeader : "+ioe.getMessage());
    	}
    }
 
    private void uploadImage(InputStream fileinputstream, int buffer) throws X10FlashException {
    	try {
	    	processHeader(fileinputstream);
	    	byte readarray[] = new byte[buffer];
			int readCount;
			do {
				readCount = fileinputstream.read(readarray);
				if(readCount != buffer)
					break;
				cmd.send(Command.CMD06, readarray, true);
			} while(true);
			fileinputstream.close();
	        if (readCount != 0) {
	            byte abyte4[] = new byte[readCount];
	            System.arraycopy(readarray, 0, abyte4, 0, readCount);
	            cmd.send(Command.CMD06, abyte4, false);
	        }
    	}
    	catch (Exception e) {
    		try {fileinputstream.close();}catch(Exception cl) {}
    		throw new X10FlashException (e.getMessage());
    	}
    }

    public void sendLoader() throws FileNotFoundException, IOException, X10FlashException {
		MyLogger.getLogger().info("Flashing loader");
		uploadImage(_bundle.getLoader().getInputStream(), 0x1000);
    }

    public void sendSystemAndUserData() throws FileNotFoundException,IOException, X10FlashException {
        Enumeration<BundleEntry> e = _bundle.systemdataEntries();
    	while (e.hasMoreElements()) {
    		BundleEntry entry = e.nextElement();
    		MyLogger.getLogger().info("Flashing "+entry.getName());
    		uploadImage(entry.getInputStream(),0x10000);
    	}    	
    }
    
    public void sendImages() throws FileNotFoundException, IOException,X10FlashException {            
    		Enumeration<BundleEntry> e = _bundle.entries();
        	while (e.hasMoreElements()) {
        		BundleEntry entry = e.nextElement();
        		MyLogger.getLogger().info("Flashing "+entry.getName());
        		uploadImage(entry.getInputStream(),0x10000);
        		MyLogger.getLogger().debug("Flashing "+entry.getName()+" finished");
        	}
    }
    
    public long getNumberPasses() {
	    Enumeration<BundleEntry> e = _bundle.allEntries();
	    long totalsize = 0;
	    while (e.hasMoreElements()) {
	    	BundleEntry entry = e.nextElement();
	    	if (entry.getName().contains("loader"))
	    		totalsize = totalsize + entry.getSize()/0x1000*3+5;
	    	else 
	    		totalsize = totalsize + entry.getSize()/0x10000*6+6;
	    }
	    return totalsize+100;
    }

    private void init() throws X10FlashException,FileNotFoundException, IOException {
	    cmd = new Command(my_a,my_ch,_bundle.simulate());
    	
		cmd.testPlugged();
		cmd.send(Command.CMD01, Command.VALNULL, false);
		cmd.send(Command.CMD09, Command.VAL2, false);
        cmd.send(Command.CMD10, Command.VALNULL, false);
        
		sendLoader();
		
		cmd.testPlugged();
		cmd.send(Command.CMD01,Command.VALNULL,false);
        cmd.send(Command.CMD09, Command.VAL2,false);    	
    }
    
    public void flashDevice() {
    	try {
		    MyLogger.getLogger().info("Start Flashing");
		    MyLogger.initProgress(getNumberPasses());
		    
		    init();
		    
			setFlashState(true);
			
			sendImages();
        	if (_bundle.hasPreset()) sendTA(_bundle.getPreset().getInputStream(),"preset");
        	//if (_bundle.hasSimlock()) sendTA(_bundle.getSimlock().getInputStream(),"simlock");
        	sendSystemAndUserData();

			cmd.send(Command.CMD07,Command.VALNULL,false);
            
			setFlashState(false);
            
			cmd.send(Command.CMD07,Command.VALNULL,false);
            cmd.send(Command.CMD10,Command.VALNULL,false);
            cmd.send(Command.CMD04,Command.VAL1,false);
	
            closeDevice();
		    
			MyLogger.getLogger().info("Flashing finished.");
		    MyLogger.initProgress(0);
	    }
    	catch (Exception ioe) {
    		closeDevice();
    		MyLogger.getLogger().error("Error flashing. Aborted");
    	}
    }

    public void closeDevice() {
    	try {
    		my_a.close(my_ch);
    		my_ch = 0;
    	}
    	catch (Exception e) {
    		my_a = null;
    		my_ch = 0;
    	}
    }
    
    public boolean openDevice() {
    	return openDevice(_bundle.simulate());
    }
    
    public boolean openDevice(boolean simulate) {
    	if (simulate) return true;
    	boolean found=false;
    	DeviceIdent device = new DeviceIdent();
    	my_a = com.sonyericsson.cs.usbflashnative.b.a.a();
    	MyLogger.getLogger().info("Searching Xperia....");
    	try {
    		device=Device.getLastConnected();
    		if (device.isDriverOk() && device.getStatus().equals("flash")) {
    			MyLogger.getLogger().debug("Trying "+device.getDeviceId());
    			my_ch = my_a.openChannel(device.getDeviceId(), false);
    			MyLogger.getLogger().info("Found at "+device.getDeviceId());
    			found = true;
    		}
    		else throw new Exception(device.getDeviceId());
    	}
    	catch (Exception e){
    		closeDevice();
    		found=false;
    		MyLogger.getLogger().error("Please plug you device in flash mode");
    		if (!device.isDriverOk() && device.getStatus().equals("flash"))
    			MyLogger.getLogger().error("Please install or reinstall device drivers from drivers folder");
    	}
    	return found;
    }

}