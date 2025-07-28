import java.security.MessageDigest;
import java.util.Base64;

public class HashGenerator {
    public static void main(String[] args) {
        try {
            String password = "admin123";
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            String hash = Base64.getEncoder().encodeToString(hashBytes);
            
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
