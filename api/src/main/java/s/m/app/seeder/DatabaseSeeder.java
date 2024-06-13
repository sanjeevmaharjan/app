package s.m.app.seeder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.print.DocFlavor;
import java.io.IOException;

@Component
@Slf4j
public class DatabaseSeeder {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JdbcUserDetailsManager jdbcUserDetailsManager;

    public DatabaseSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, JdbcUserDetailsManager jdbcUserDetailsManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jdbcUserDetailsManager = jdbcUserDetailsManager;
    }

    @EventListener({ApplicationReadyEvent.class})
    public void seed() {
        try {
            seedSecuritySchema();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean tableExists(String tablename) {
        String query = "select count(*) "
                + "from information_schema.tables "
                + "where table_name = ? and table_schema = 'public'";
        Integer result = jdbcTemplate.queryForObject(query, Integer.class, tablename);

        return Integer.valueOf(1).equals(result);
    }

    private void seedSecuritySchema() throws IOException {
        var resource = new ClassPathResource("userdetails/jdbc/users.ddl");
        try (var is = resource.getInputStream()) {
            var sql = new String(is.readAllBytes());

            if (!tableExists("users")) {
                jdbcTemplate.execute(sql);

                var admin = User.builder()
                        .username("sanjeev")
                        .password(passwordEncoder.encode("sanjeev"))
                        .roles("USER")
                        .build();

                try {
                    jdbcUserDetailsManager.loadUserByUsername(admin.getUsername());
                }
                catch (UsernameNotFoundException usernameNotFoundException) {
                    jdbcUserDetailsManager.createUser(admin);
                }

                log.info("Created Security Tables");
            }
        }
    }
}
