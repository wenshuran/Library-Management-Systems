package LMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Controller
public class UserController {
    @Autowired
    private UserSession userSession;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage(Model model){
        if (userSession.isLoginFailed()){
            model.addAttribute("login_status", "error");
            userSession.setLoginFailed(false);
        }
        return "login";
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws Exception {
        userSession.setUser(null);
            response.sendRedirect("/");
        }

        @PostMapping("/login")
        public void doLogin(String email, String password, HttpServletResponse response) throws Exception {
            Optional<User> user = userRepository.findByEmailAndPassword(email, password);
            if (user.isPresent()) {
                User user1 = user.get();
                userSession.setUser(user1);
                if (user1.is_admin())
                response.sendRedirect("/librarian/management");
            else
                response.sendRedirect("/member/management");
        } else {
            userSession.setLoginFailed(true);
            response.sendRedirect("/login");
        }

    }
}
