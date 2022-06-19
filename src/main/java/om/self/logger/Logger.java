package om.self.logger;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The Logger class is used to log messages to a file or print them.
 */
public class Logger {
    private LinkedList<Message> messages = new LinkedList<>();
    private Path filePath;
    private File file;

    /**
     * set the file path to log to.
     * @param path The path to the file you want to log to.
     */
    public void setFilePath(Path path){
        this.filePath = path;
    }

    /**
     * set the file path to current user directory.
     */
    public void setFilePathToCurrent(){
        this.filePath = getCurrentDirectory();
    }

    /**
     * get the current directory.
     */
    public static Path getCurrentDirectory(){
        return Path.of(System.getProperty("user.dir"));
    }

    /**
     * get the current stored file path(if no file path is stored then it will set file path to current dir).
     * @return The current file path.
     */
    public Path getFilePath(){
        if(filePath == null)
            setFilePathToCurrent();

        return filePath;
    }

    private static void createFile(File file)throws IOException, SecurityException{
        if(!file.exists())file.createNewFile();
    }

    /**
     * set the file to log to. It will create a new file if it does not exist.
     * @param file The file you want to log to.
     * @throws IOException If the file could not be created.
     * @throws SecurityException If there was a security exception.
     */
    public void setFile(File file) throws IOException, SecurityException{
        this.file = file;
        createFile(file);
    }

    /**
     * sets the file to the name or directory and name in
     * @param fileName The name of the file you want to log to. This can include a path as well.
     * @param useFilePath Whether or not to use the file path stored by the Logger.
     * @throws IOException If the file cannot be created.
     * @throws SecurityException If there is a security exception.
     */
    public void setFile(String fileName, boolean useFilePath) throws IOException, SecurityException{
        if(useFilePath)
            setFile(new File(getFilePath().toString() + "\\" + fileName));
        else
            setFile(new File(fileName));
    }

    /**
     * get the stored file in the Logger(if no file is stored then it will create a "log.txt" in stored directory).
     * @return current file.
     * @throws IOException If the file could not be created.
     * @throws SecurityException If there was a security exception.
     */
    public File getFile() throws IOException, SecurityException{
        if(file == null)
            setFile("log.txt", true);
        return file;
    }

    /**
     * Get file by name and path. If file does not exist then it will be created.
     * @param Name The name of the file.
     * @param path The path to the file.
     * @return The file.
     * @throws IOException If file can not be created.
     * @throws SecurityException If there are security issues.
     */
    public static File getFile(String Name, Path path)throws Exception, IOException, SecurityException{
        if(path == null)
            path = getCurrentDirectory();
        if (StringUtils.isEmpty(Name))
            throw new Exception("File name can't be empty");
        File f = new File(path.toString() + "\\" + Name);
        createFile(f);
        return f;
    }

    /**
     * makes/updates the file with the givin messages.
     * @param messages the messages to be added to file.
     * @param file the file to be updated.
     * @param append Whether or not to append to the file(keep original stuff or overwrite it).
     * @throws IOException if the file could not be made.
     * @throws SecurityException if there is a security exception.
    */
    public static void makeFile(List<Message> messages, File file, boolean append) throws IOException, SecurityException{
        createFile(file);

        //stores content of new file
        StringBuilder content = new StringBuilder();

        //copy old file to new file if append is true
        if(append){
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);

            for(String line : br.lines().toList()){
                content.append(line);
                content.append("\n");  
            }

            br.close();
        }

        //add new messages to file
        content.append(getMessagesAsString(messages, false));

        //write the file
        FileWriter writer = new FileWriter(file);
        writer.write(content.toString());
        writer.close();
    }

   /**
     * makes/updates a default file with the givin messages(default directory is current and default name is "log.txt").
     * @param messages the messages to be added to file.
     * @param append Whether or not to append to the file(keep original stuff or overwrite it).
     * @throws IOException if the file could not be made.
     * @throws SecurityException if there is a security exception.
    */
    public static void makeFile(List<Message> messages, boolean append) throws Exception, IOException, SecurityException{
        makeFile(messages, getFile("log.txt", getCurrentDirectory()), append);
    }

    /**
     * create a log file with stored messages.
     * @param file The file to write to.
     * @param append Whether or not to append to the file(keep original stuff or overwrite it).
     * @param clearMessagesAfterWrite Whether or not to clear the messages after writing.
     * @throws IOException If the file cannot be created.
     * @throws SecurityException If there is a security exception.
     */
    public void makeFile(File file, boolean append, boolean clearMessagesAfterWrite) throws IOException, SecurityException{
        //sets the file and creates it if it does not exist
        setFile(file);

        makeFile(messages, file, append);

        //clear messages if clearMessagesAfterWrite is true
        if(clearMessagesAfterWrite){
            clearMessages();
        }
    }

    /**
     * creates a log file with stored messages using the set file(if ther is no set file then it will use default).
     * @param append Whether or not to append to the file(keep original stuff or overwrite it).
     * @param clearMessagesAfterWrite Whether or not to clear the messages after writing.
     * @throws IOException If the file cannot be created.
     * @throws SecurityException if there is a security exception.
     */
    public void makeFile(boolean append, boolean clearMessagesAfterWrite) throws IOException, SecurityException{
        makeFile(getFile(), append, clearMessagesAfterWrite);
    }

    /**
     * add a message to the Logger.
     * @param message The message to be added.
     * @param print Whether or not to print the message.
     * @param store Whether or not to store the message.
     */
    public void addMessage(Message message, boolean print, boolean store, boolean writeToFile) {
        if (print) {
            System.out.println(message.getFormatedMessage(true));
        }
        if (store) {
            messages.add(message);
        }
        if(writeToFile){
            try{
                makeFile(Arrays.asList(new Message[]{message}), getFile(), true);
            }
            catch(IOException | SecurityException e){}
        }
    }
       
    /**
     * get the stored messages as a list of strings.
     * @return The list of messages.
     */
    public LinkedList<Message> getStoredMessages(){
        return messages;
    }

    /**
     * get all stored messages that are of a certain type.
     * @param type The type of message to get.
     */
    public LinkedList<Message> getStoredMessagesOfType(Message.Type type){
        LinkedList<Message> messages = new LinkedList<>();
        for(Message message : messages){
            if(message.type == type)
                messages.add(message);
        }
        return messages;
    }

    /**
     * clear the stored messages.
     */
    public void clearMessages(){
        messages.clear();
    }

    /**
     * print the stored messages to the console.
     * @param clearAfterPrint Whether or not to clear the messages after printing.
     */
    public void printStoredMessages(boolean clearAfterPrint){
        for(Message m : messages)
            System.out.println(m.getFormatedMessage(true));
        if(clearAfterPrint)
            clearMessages();
    }

    /**
     * get the stored messages as a string.
     * @param includeColor Whether or not to include color in the messages.
     * @return The messages as a string with new lines between each message.
     */
    public String getStoredMessagesAsString(boolean includeColor){
        return getMessagesAsString(messages, includeColor);
    }

    /**
     * get the passed in messages as a string.
     * @param messages The messages to get as a string.
     * @param includeColor Whether or not to include color in the messages.
     * @return The messages as a string with new lines between each message.
     */
    public static String getMessagesAsString(List<Message> messages, boolean includeColor){
        StringBuilder sb = new StringBuilder();

        for(Message m : messages)
            sb.append(m.getFormatedMessage(includeColor) + "\n");

        //clear last newline
        int length = sb.length();
        sb.delete(length - 1, length);

        return sb.toString();
    }
}
