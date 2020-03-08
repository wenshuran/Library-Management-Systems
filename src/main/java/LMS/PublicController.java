package LMS;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserSession userSession;

    @PostMapping("/search_by_type")
    public List<Artifact> searchByType(String type){
        List<Artifact> artifactList = artifactRepository.findByType(type);
        return artifactList;
    }

    @PostMapping("/join")
    public void join(String email, String password, String name, String gender, HttpServletResponse response) throws IOException {
        User user = new User();
        user.set_admin(false);
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setGender(gender);
        userRepository.save(user);
        userSession.setUser(user);
        response.sendRedirect("/member/management");
    }

    @GetMapping("/join")
    public String joinPage(Model model){
        return "MemberJoin";
    }

    @GetMapping("/search")
    public String searchPage(Model model){
        return "NonMemberSearch";
    }

    @PostMapping("/doSearch")
    public String searchByName(@RequestParam String name, Model model){
        List<Artifact> artifactList = artifactRepository.findByNameLike("%"+name+"%");
        model.addAttribute("artifacts", artifactList);
        return "NonMemberSearchResults";
    }

    @GetMapping("/suggest")
    public String suggestPage(Model model){
        List<Feedback> feedbacks = feedbackRepository.findAll();
        model.addAttribute("feedbacks", feedbacks);
        return "Feedback";
    }

    @PostMapping("/doSuggest")
    public void suggest(@RequestParam String name, @RequestParam String email, @RequestParam String suggestion, HttpServletResponse response) throws IOException {
        Feedback feedback = new Feedback();
        feedback.setName(name);
        feedback.setEmail(email);
        feedback.setSuggestion(suggestion);
        feedbackRepository.save(feedback);
        response.sendRedirect("/public/suggest");
    }

}
