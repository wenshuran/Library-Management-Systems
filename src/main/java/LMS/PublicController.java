package LMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/search_by_type")
    public List<Artifact> searchByType(String type){
        List<Artifact> artifactList = artifactRepository.findByType(type);
        return artifactList;
    }

    @PostMapping("/join")
    public User join(String email, String password, String name, String gender){
        User user = new User();
        user.set_admin(false);
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setGender(gender);
        userRepository.save(user);
        return user;
    }
}
