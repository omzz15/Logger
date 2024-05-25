package om.self.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The Logger class is used to log messages to a file or print them.
 */
public class Logger {
    private static String defaultFileName = "log.txt";
    private static Logger instance = new Logger("root");
    /**
     * stack of stored messages(in order)
     * NOTE: not all messages will be stored, the store flag must be true when calling addMessage()
     */
    private final String name;
    private final LinkedList<Message> messages = new LinkedList<>();
    private Path filePath;
    private File file;
    private Consumer<Message> handler;

    //flags
    private boolean printNewMessages;
    private boolean storeNewMessages;
    private boolean writeNewMessagesToFile;
    private boolean sendNewMessagesToHandler;


    /**
     * Creates a new logger instance with the given name (note at the moment the loggers are not connected)
     * @param name the name of the logger instance
     */
    public Logger(String name) {
        this.name = name;
    }

    public static String getDefaultFileName() {
        return defaultFileName;
    }

    public static void setDefaultFileName(String defaultFileName) {
        Logger.defaultFileName = defaultFileName;
    }

    /**
     * gets the main Logger instance
     * @return an instance of Logger
     */
    public static Logger getInstance(){
        return instance;
    }

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

    public void setPrintNewMessages(boolean printNewMessages) {
        this.printNewMessages = printNewMessages;
    }

    public void setStoreNewMessages(boolean storeNewMessages) {
        this.storeNewMessages = storeNewMessages;
    }

    public void setWriteNewMessagesToFile(boolean writeNewMessagesToFile) {this.writeNewMessagesToFile = writeNewMessagesToFile;}

    public void setSendNewMessagesToHandler(boolean sendNewMessagesToHandler) {
        this.sendNewMessagesToHandler = sendNewMessagesToHandler;
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
     * @param useFilePath Whether to use the file path stored by the Logger.
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
     * get the stored file in the Logger(if no file is stored then it will create a File with name based on defaultFileName in stored directory).
     * @return current file.
     * @throws IOException If the file could not be created.
     * @throws SecurityException If there was a security exception.
     */
    public File getFile() throws IOException, SecurityException{
        if(file == null)
            setFile(defaultFileName, true);
        return file;
    }

    /**
     * Get file by name and path. If file does not exist then it will be created.
     * @param name The name of the file.
     * @param path The path to the file.
     * @return The file.
     * @throws IOException If file can not be created.
     * @throws SecurityException If there are security issues.
     * @throws IllegalArgumentException if file name is empty
     */
    public static File getFile(String name, Path path)throws IllegalArgumentException, IOException, SecurityException{
        if(path == null)
            path = getCurrentDirectory();
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("File name can't be null or empty");
        File f = new File(path + "/" + name);
        createFile(f);
        return f;
    }

    /**
     * makes/updates the file with the given messages.
     * @param messages the messages to be added to file.
     * @param file the file to be updated.
     * @param append Whether to append to the file(keep original stuff or overwrite it).
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
     * makes/updates a default file with the given messages(default directory is current and default name is defined by defaultFileName).
     * @param messages the messages to be added to file.
     * @param append Whether to append to the file(keep original stuff or overwrite it).
     * @throws IOException if the file could not be made.
     * @throws SecurityException if there is a security exception.
    */
    public static void makeFile(List<Message> messages, boolean append) throws Exception, IOException, SecurityException{
        makeFile(messages, getFile(defaultFileName, getCurrentDirectory()), append);
    }

    /**
     * makes/updates a file named by fileName parameter with the given messages(default directory is current).
     * @param messages the messages to be added to file.
     * @param fileName the name of the file you want to update/change(ending needs to be set, for example use "log.txt" not "log")
     * @param append Whether to append to the file(keep original stuff or overwrite it).
     * @throws IOException if the file could not be made.
     * @throws SecurityException if there is a security exception.
     */
    public static void makeFile(List<Message> messages, String fileName, boolean append) throws Exception, IOException, SecurityException{
        makeFile(messages, getFile(fileName, getCurrentDirectory()), append);
    }

    /**
     * create a log file with stored messages.
     * @param file The file to write to.
     * @param append Whether to append to the file(keep original stuff or overwrite it).
     * @param clearMessagesAfterWrite Whether to clear the messages after writing.
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
     * creates a log file with stored messages using the set file(if there is no set file then it will use default).
     * @param append Whether to append to the file(keep original stuff or overwrite it).
     * @param clearMessagesAfterWrite Whether to clear the messages after writing.
     * @throws IOException If the file cannot be created.
     * @throws SecurityException if there is a security exception.
     */
    public void makeFile(boolean append, boolean clearMessagesAfterWrite) throws IOException, SecurityException{
        makeFile(getFile(), append, clearMessagesAfterWrite);
    }

    /**
     * creates a log file with stored messages using the file name and set path(if there is no set path then it will use default).
     * @param append Whether to append to the file(keep original stuff or overwrite it).
     * @param clearMessagesAfterWrite Whether to clear the messages after writing.
     * @throws IOException If the file cannot be created.
     * @throws SecurityException if there is a security exception.
     * @throws IllegalArgumentException if fileName is empty
     */
    public void makeFile(String fileName, boolean append, boolean clearMessagesAfterWrite) throws IOException, SecurityException, IllegalArgumentException{
        makeFile(getFile(fileName, getFilePath()), append, clearMessagesAfterWrite);
    }

    /**
     * add a message to the Logger.
     * @param message The message to be added.
     * @param print Whether to print the message.
     * @param store Whether to store the message.
     * @param writeToFile Whether to write the message to a file. This will keep the past contents of the file.
     */
    public void addMessage(Message message, boolean print, boolean store, boolean writeToFile) {
        if (print) {
            System.out.println(message.getFormattedMessage(true));
        }
        if (store) {
            messages.add(message);
        }
        if(writeToFile){
            try{
                makeFile(Collections.singletonList(message), getFile(), true);
            }
            catch(Exception e){
                addMessage(new Message("Could not write message to file.\n" + getExceptionAsString(e), Message.Type.ERROR, true), true, true, false);
            }
        }
    }

    /**
     * add a message to the Logger using settings from instance.
     * @param message The message to be added.
     */
    public void addMessage(Message message) {
        addMessage(message, printNewMessages, storeNewMessages, writeNewMessagesToFile);
    }
       
    /**
     * get the stored messages as a list of strings.
     * @return The list of messages.
     */
    public List<Message> getStoredMessages(){
        return messages;
    }

    /**
     * get all messages that are of a certain type.
     * @param messages the messages you want to filter through
     * @param type The type of message to get.
     */
    public static List<Message> getStoredMessagesOfType(List<Message> messages, Message.Type type){
        return messages.stream().filter((m) -> m.type == type).toList();
    }

    /**
     * get all messages that are of certain types.
     * <p></p>
     * only use with multiple types because it is slower than {@link om.self.logger.Logger#getStoredMessagesOfType(List, Message.Type)}
     * @param messages the messages you want to filter through
     * @param types The types of message to get.
     */
    public static List<Message> getStoredMessagesOfTypes(List<Message> messages, Message.Type... types){
        return messages.stream().filter((m) -> Arrays.stream(types).anyMatch((t) -> t == m.type)).toList();
    }

    /**
     * implementation of {@link om.self.logger.Logger#getStoredMessagesOfType(List, Message.Type)} for Logger instances using stored messages
     */
    public List<Message> getStoredMessagesOfType(Message.Type type){
        return getStoredMessagesOfType(messages, type);
    }

    /**
     * implementation of {@link om.self.logger.Logger#getStoredMessagesOfTypes(List, Message.Type...)} for Logger instances using stored messages
     */
    public List<Message> getStoredMessagesOfTypes(Message.Type... types){
        return getStoredMessagesOfTypes(messages, types);
    }

    /**
     * clear the stored messages.
     */
    public void clearMessages(){
        messages.clear();
    }

    /**
     * print the stored messages to the console.
     * @param clearAfterPrint Whether to clear the messages after printing.
     */
    public void printStoredMessages(boolean clearAfterPrint){
        for(Message m : messages)
            System.out.println(m.getFormattedMessage(true));
        if(clearAfterPrint)
            clearMessages();
    }

    /**
     * get the stored messages as a string.
     * @param includeColor Whether to include color in the messages.
     * @return The messages as a string with new lines between each message.
     */
    public String getStoredMessagesAsString(boolean includeColor){
        return getMessagesAsString(messages, includeColor);
    }

    /**
     * get the passed in messages as a string.
     * @param messages The messages to get as a string.
     * @param includeColor Whether to include color in the messages.
     * @return The messages as a string with new lines between each message.
     */
    public static String getMessagesAsString(List<Message> messages, boolean includeColor){
        StringBuilder sb = new StringBuilder();

        for(Message m : messages)
            sb.append(m.getFormattedMessage(includeColor)).append("\n");

        //clear last newline
        int length = sb.length();
        sb.delete(length - 1, length);

        return sb.toString();
    }

    public static String getExceptionAsString(Exception e){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(e.getMessage()).append("\n");
        for(StackTraceElement element : e.getStackTrace()){
            stringBuilder.append("\t").append(element.toString()).append("\n");
        }
        return stringBuilder.toString();
    }
}
