    import java.util.Vector;
    
    public class MockPlayer extends Player{
        public Vector<String[]> messages = new Vector();
        public int messagenumber = 0;
        public int messageblock = 0;
        @Override
        public String receiveMessage() { 
            if (messages.size() == 0) return "Brick";
            String message = "";
            try {
                message = messages.get(messageblock)[messagenumber];
                if (messagenumber<2) messagenumber++;
                else{
                    messagenumber = 0;
                    messageblock++;
                }
            } catch (Exception e) {
                
            }
            
            return message; }
        
    }