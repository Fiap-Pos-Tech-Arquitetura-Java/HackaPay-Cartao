package br.com.fiap.postech.hackapay.cartao.helper;

import br.com.fiap.postech.hackapay.security.JwtService;
import br.com.fiap.postech.hackapay.security.User;
import br.com.fiap.postech.hackapay.security.UserDetailsImpl;
import org.springframework.security.core.userdetails.UserDetails;

public class UserHelper {
    public static UserDetails getUserDetails(String login) {
        return new UserDetailsImpl(getUserForSecurity(login));
    }
    public static String getToken(String login) {
        br.com.fiap.postech.hackapay.security.User userSecurity = getUserForSecurity(login);
        return "Bearer " + new JwtService().generateToken(userSecurity);
    }
    private static User getUserForSecurity(String login) {
        return new User(login, "umaSenhaQualquer");
    }
}
