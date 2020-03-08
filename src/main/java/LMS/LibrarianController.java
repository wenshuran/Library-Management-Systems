package LMS;

import com.sun.org.apache.xpath.internal.operations.Mod;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.*;

@Controller
@RequestMapping("/librarian")
public class LibrarianController {
    @Autowired
    private UserSession userSession;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ArtifactRepository artifactRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list_artifacts")
    public String listArtifacts(Model model){
        model.addAttribute("username", userSession.getUser().getName());
        model.addAttribute("artifacts", artifactRepository.findAll());
        return "AdminAddRemove";
    }

    @PostMapping("/add_artifact")
    public void addArtifact(@RequestParam String name, @RequestParam String type, HttpServletResponse response) throws IOException {
        Optional<Artifact> artifactOptional = artifactRepository.findByName(name);
        if (artifactOptional.isPresent()){ //Making a copy of an artifact
            artifactRepository.makeCopy(artifactOptional.get().getId());
        }
        Artifact artifact = new Artifact();
        artifact.setIs_copy(false);
        artifact.setName(name);
        artifact.setType(type);
        artifactRepository.save(artifact);
        response.sendRedirect("/librarian/list_artifacts");
    }

    @GetMapping("/remove_artifact/{id}")
    public void removeArtifact(@PathVariable(value = "id",required = true) long id, HttpServletResponse response) throws IOException {
        artifactRepository.deleteById(id);
        response.sendRedirect("/librarian/list_artifacts");
    }

    @GetMapping("/search_member_page")
    public String searchMemberPage(){
        return "AdminSearchMem";
    }

    @PostMapping("/search_member")
    public String searchMember(@RequestParam String name, Model model){
        List<User> userList = userRepository.findByNameLike("%"+name+"%");
        model.addAttribute("members", userList);
        return "AdminSearchMemRes";
    }

    @GetMapping("/view_current_loan_page")
    public String viewCurrentLoanPage(Model model){
        return "AdminViewCurrByID";
    }

    @PostMapping("/view_current_loan")
    public String viewCurrentLoan(@RequestParam long user_id, Model model){
        List<Loan> loans = loanRepository.findByUser_id(user_id);
        List<Loan> currentLoans = new ArrayList<>();
        Date date = new Date(new java.util.Date().getTime());
        for (Loan loan : loans){
            if (loan.getDue_date().after(date)){
                currentLoans.add(loan);
            }
        }
        model.addAttribute("currentLoans", currentLoans);
        model.addAttribute("username", userSession.getUser().getName());
        return "AdminViewCurr";
    }

    @GetMapping("/view_historical_loan_page")
    public String viewHisLoanPage(Model model){
        return "AdminViewHisByID";
    }

    @PostMapping("/view_historical_loan")
    public String viewHistoricalLoan(@RequestParam long user_id, Model model){
        List<Loan> loans = loanRepository.findByUser_id(user_id);
        model.addAttribute("loans", loans);
        model.addAttribute("username", userSession.getUser().getName());
        return "AdminViewHistory";
    }

    @GetMapping("/renew_loan/{id}")
    public void renewLoan(@PathVariable(value = "id",required = true) long id, HttpServletResponse response) throws IOException {
        Optional<Loan> loan = loanRepository.findById(id);
        if (loan.isPresent()){
            Long artifact_id = loan.get().getArtifact_id();
            List<Reservation> reservations = reservationRepository.findByArtifact_id(artifact_id);
            Date current_due_date = loan.get().getDue_date();
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(current_due_date);
            calendar.add(Calendar.DATE, 30); //Add 30 days
            java.util.Date utilDate = (java.util.Date)calendar.getTime();
            Date newDate = new Date(utilDate.getTime());
            for (Reservation reservation : reservations){
                if(reservation.getDate().before(newDate))
                    response.sendRedirect("/librarian/view_current_loan_page");
            }
            loanRepository.updateDueDate(loan.get().getId(), newDate);
            response.sendRedirect("/librarian/view_current_loan_page");
        }
        else {
            response.sendRedirect("/librarian/view_current_loan_page");// "Wrong loan id";
        }
    }

    @GetMapping("/reserve_page")
    public String reservePage(Model model){
        model.addAttribute("username", userSession.getUser().getName());
        return "AdminReserve";
    }

    @PostMapping("/reserve_artifact")
    public void reserveArtifact(@RequestParam long user_id, @RequestParam long artifact_id, HttpServletResponse response) throws IOException {
        List<Loan> loanList = loanRepository.findByArtifact_id(artifact_id);
        Date date = new Date(new java.util.Date().getTime());
        for (Loan loan : loanList){
            date = loan.getDue_date();
        }
        for (Loan loan : loanList){
            if (loan.getDue_date().after(date)){
                date = loan.getDue_date(); //Get the latest due date
            }
        }
        List<Reservation> reservationList = reservationRepository.findByArtifact_id(artifact_id);
        Calendar calendar =new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //Reserve for 1 day
        java.util.Date utilDate = (java.util.Date)calendar.getTime();
        Date reservation_due = new Date(utilDate.getTime());
        for (Reservation reservation : reservationList){
            Date reservation_start = reservation.getDate(); //The start time of another member's reservation
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            java.util.Date newDate = (java.util.Date)calendar.getTime();
            Date reservation_end =new Date(utilDate.getTime()); //The end time of another member's reservation
            if ((reservation_due.after(reservation_start) && reservation_due.before(reservation_end)) ||
                    (date.after(reservation_start) && date.before(reservation_end))){
                response.sendRedirect("/librarian/reserve_page");
            }

        }
        Reservation reservation = new Reservation();
        reservation.setUser_id(user_id);
        reservation.setArtifact_id(artifact_id);
        reservation.setDate(date);
        reservationRepository.save(reservation);
        response.sendRedirect("/librarian/reserve_page");
    }

    @GetMapping("/record_loan_page")
    public String recordLoanPage(Model model){
        return "AdminRecLoan";
    }

    @PostMapping("/record_loan")
    public void recordLoan(@RequestParam long user_id, @RequestParam long artifact_id, HttpServletResponse response) throws IOException {
        Date loan_date = new Date(new java.util.Date().getTime());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(loan_date);
        calendar.add(Calendar.DATE, 30); //Loan for 30 day
        java.util.Date utilDate = (java.util.Date)calendar.getTime();
        Date due_date = new Date(utilDate.getTime());

        List<Reservation> reservationList = reservationRepository.findByArtifact_id(artifact_id);
        for (Reservation reservation : reservationList){
            Date reservation_start = reservation.getDate(); //The start time of another member's reservation
            if (loan_date.before(reservation_start) && due_date.after(reservation_start)){
                response.sendRedirect("/librarian/record_loan_page");
            }
        }

        Loan loan = new Loan();
        loan.setUser_id(user_id);
        loan.setArtifact_id(artifact_id);
        loan.setLoan_date(loan_date);
        loan.setDue_date(due_date);
        loanRepository.save(loan);
        response.sendRedirect("/librarian/record_loan_page");
    }

    @GetMapping("/record_return_page")
    public String recordReturnPage(Model model){
        return "AdminRecReturn";
    }

    @PostMapping("/record_return")
    public void recordReturn(@RequestParam long id, HttpServletResponse response) throws IOException {
        Date due_date = new Date(new java.util.Date().getTime());
        loanRepository.updateDueDate(id, due_date);
        response.sendRedirect("/librarian/record_return_page");
    }

    @GetMapping("/search_page")
    public String searchPage(Model model){
        return "AdminSearch";
    }

    @PostMapping("/search_by_name")
    public String searchByName(@RequestParam String name, Model model){
        List<Artifact> artifactList = artifactRepository.findByNameLike("%"+name+"%");
        model.addAttribute("artifacts", artifactList);
        return "AdminSearchResults";
    }

    @GetMapping("/search_user_page")
    public String searchUserPage(Model model){
        return "AdminEditByID";
    }

    @PostMapping("/view_profile")
    public String viewProfile(@RequestParam long user_id, Model model){
        Optional<User> userOptional =  userRepository.findById(user_id);
        User user = new User();
        if (userOptional.isPresent())
            user = userOptional.get();
        model.addAttribute("user", user);
        return "AdminEdit";
    }

    @PostMapping("/update_profile")
    public void updateProfile(@RequestParam long user_id, @RequestParam String name, HttpServletResponse response) throws IOException {
        userRepository.updateUser(user_id, name);
        response.sendRedirect("/librarian/search_user_page");
    }

    @GetMapping("/management")
    public String managementPage(Model model){
        model.addAttribute("User", userSession.getUser());
        return "AdminPortal";
    }

    @GetMapping("/login")
    public String login(){
        if (userSession.getUser()==null){
            return "AdminLogin";
        }
        else
            return "AdminPortal";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String email, @RequestParam String password, HttpServletResponse response) throws Exception {
        List<User> user = userRepository.findUser(email, password);
        if (user.size()>0) {
            User user1 = user.get(0); //TODO
            if (user1.is_admin()){
                userSession.setUser(user1);
                return "AdminPortal";
            }
            else{
                return "AdminLogin";
            }
        } else {
            userSession.setLoginFailed(true);
            return "AdminLogin";
        }

    }
}
