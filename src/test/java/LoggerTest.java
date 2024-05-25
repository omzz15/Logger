import om.self.logger.*;

import java.io.File;
import java.util.List;

public class LoggerTest {
    public static void main(String[] args) throws Exception{
        //create new logger instance
        //every instance can store a separate message stack and path/file
        //all instances will print to the same console
        Logger l = new Logger("test");

        //you can also use the central logger instance with this:
        Logger l2 = Logger.getInstance();

        //create messages of different types
        Message info = new Message("hey got some info: -40 is the same temperature in celsius and fahrenheit", Message.Type.INFO, false);
        Message debug = new Message("just a quick debug", Message.Type.DEBUG, true);
        Message warning = new Message("it's not failing. yet...", Message.Type.WARNING, false);
        Message error = new Message("now the code is blowing up :(", Message.Type.ERROR, true);
        Message realError = new Message(Logger.getExceptionAsString(new Exception("I told you it was blowing up")), Message.Type.ERROR, false);//call source is already in error

        //change the file were messages will be stored
        //can throw IO and security exceptions
        l.setFile("example.txt", false);

        //add them to logger(print will print to console, store will put them in the logger message stack and, writeToFile will write it to a file)
        l.addMessage(info, true, true, true);
        l.addMessage(debug, true, true, true);
        l.addMessage(warning, true, true, true);
        l.addMessage(error, true, true, true);
        l.addMessage(realError, true, true, true);
        l.addMessage(new Message("idk", Message.Type.UNKNOWN, true), true, true, true);

        //lets add a message to l2
        l2.addMessage(realError, true, true, false);

        //let's print them one more time
        //first get all stored messages
        List<Message> messageList = l.getStoredMessages();
        //then we can print them
        System.out.println(Logger.getMessagesAsString(messageList,true));
        //alternatively we can use this
        l.printStoredMessages(false);

        //now we can write to a file using the central logger instance that has a message stored
        Logger.getInstance().makeFile(false,false);
    }
}
