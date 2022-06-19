package om.self.logger;

public class Message{
    //resets color for terminal
    static final String ANSI_RESET = "\u001B[0m";

    public final String payload;
    public final String source;
    public final Type type;

    /**
     * Create a new Message Object with the given payload and type. Usually used by the Logger class. 
     * @param payload The message to be logged.
     * @param type The type of message.
     * @param getCallSource Whether or not to get the source of the call.
     */
    public Message(String payload, Type type, boolean getCallSource){
        this.type = type;
        if(getCallSource){
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            source = stackTrace[stackTrace.length - 1].toString(); 
        }
        else
            source = null;
        this.payload = payload;     
    }


    /**
     * Get the message as a string.
     * @param includeColor Whether or not to include color in the message.
     * @return The message as a string.
     */
    public String getFormatedMessage(boolean includeColor){
        StringBuilder sb = new StringBuilder();
        
        if(includeColor)
            sb.append(type.color);
        
        sb.append(type.value);

        if(source != null)
            sb.append(" from " + source);
        
        sb.append(" - " + payload);

        if(includeColor)
            sb.append(ANSI_RESET);

        return sb.toString();
    }

    /**
     * The type of message.
     */
    public enum Type{
        ERROR("ERROR!!", "\u001B[31m"), //red
        WARNING("WARNING!", "\u001B[33m"), //yellow
        INFO("Info", "\u001B[32m"),
        DEBUG("Debug", "\u001B[34m"),
        TRACE("Trace", "\u001B[35m"),
        UNKNOWN("Unknown", "\u001B[36m");

        public final String value;
        public final String color;

        Type(String value, String color){
            this.value = value;
            this.color = color;
        }
    }
}