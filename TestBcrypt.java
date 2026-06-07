import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        String[] passwords = {"admin", "123456", "admin123", "password", "root"};
        for (String p : passwords) {
            if (encoder.matches(p, hash)) {
                System.out.println("MATCH FOUND: " + p);
                return;
            }
        }
        System.out.println("NO MATCH");
    }
}
