package LMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.*;

@Controller
@RequestMapping("/member")
public class MemberController {
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

    @GetMapping("/list_loans")
    public String listLoans(Model model){
        List<Loan> loans = loanRepository.findByUser_id(userSession.getUser().getId());
        if (!loans.isEmpty()){
            List<Loan> currentLoans = new ArrayList<>();
            Date date = new Date(new java.util.Date().getTime());
            for (Loan loan : loans){
                if (loan.getDue_date().after(date)){
                    currentLoans.add(loan);
                }
            }
            model.addAttribute("list", currentLoans);
        }
        else {
            model.addAttribute("list", loans);
        }
        model.addAttribute("username", userSession.getUser().getName());
        return "MemberViewRenew";
    }

    @PostMapping("/renew_loan/{id}")
    public String renewLoan(@PathVariable(value = "id",required = true) Long id, Model model){
        Optional<Loan> loan = loanRepository.findById(id);
        if (loan.isPresent()){
            Long artifact_id = loan.get().getArtifact_id();
            List<Reservation> reservations = reservationRepository.findByArtifact_id(artifact_id);
            Date current_due_date = loan.get().getDue_date();
            Calendar calendar =new GregorianCalendar();
            calendar.setTime(current_due_date);
            calendar.add(Calendar.DATE, 30); //Add 30 days
            java.util.Date utilDate = (java.util.Date)calendar.getTime();
            Date newDate =new Date(utilDate.getTime());
            for (Reservation reservation : reservations){
                if(reservation.getDate().before(newDate))
                    model.addAttribute("renew_status", "fail");
            }
            loanRepository.updateDueDate(loan.get().getId(), newDate);
            model.addAttribute("renew_status", "success");
        }
        else {
            model.addAttribute("renew_status", "wrong id");
        }
        List<Loan> loans = loanRepository.findByUser_id(userSession.getUser().getId());
        List<Loan> currentLoans = new ArrayList<>();
        Date date = new Date(new java.util.Date().getTime());
        for (Loan loan1 : loans){
            if (loan1.getDue_date().after(date)){
                currentLoans.add(loan1);
            }
        }
        model.addAttribute("list", currentLoans);
        model.addAttribute("username", userSession.getUser().getName());
        return "MemberViewRenew";
    }

    @GetMapping("/reserve_page")
    public String reservePage(Model model){
        model.addAttribute("username", userSession.getUser().getName());
        List<Artifact> artifactList = artifactRepository.findAll();
        model.addAttribute("artifacts", artifactList);
        return "MemberReserve";
    }

    @GetMapping("/reserve/{id}")
    public void reserveById(@PathVariable(value = "id",required = true) Long artifact_id, Model model, HttpServletResponse response) throws IOException {
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
                model.addAttribute("reserve_status", "fail");
            }

        }
        Reservation reservation = new Reservation();
        reservation.setUser_id(userSession.getUser().getId());
        reservation.setArtifact_id(artifact_id);
        reservation.setDate(date);
        reservationRepository.save(reservation);
        model.addAttribute("reserve_status", "success");
        response.sendRedirect("/member/reserve_page");
    }

//    @PostMapping("/reserve_loan")
//    public String reserveLoan(Long artifact_id, Date date){
//        List<Loan> loanList = loanRepository.findByArtifact_id(artifact_id);
//        Date date1 = new Date(new java.util.Date().getTime());
//        for (Loan loan : loanList){
//            date1 = loan.getDue_date();
//        }
//        for (Loan loan : loanList){
//            if (loan.getDue_date().after(date1)){
//                date1 = loan.getDue_date(); //Get the latest due date
//            }
//        }
//        List<Reservation> reservationList = reservationRepository.findByArtifact_id(artifact_id);
//        Calendar calendar =new GregorianCalendar();
//        calendar.setTime(date);
//        calendar.add(Calendar.DATE, 1); //Reserve for 1 day
//        java.util.Date utilDate = (java.util.Date)calendar.getTime();
//        Date reservation_due = new Date(utilDate.getTime());
//        for (Reservation reservation : reservationList){
//            Date reservation_start = reservation.getDate(); //The start time of another member's reservation
//            calendar.setTime(date);
//            calendar.add(Calendar.DATE, 1);
//            java.util.Date newDate = (java.util.Date)calendar.getTime();
//            Date reservation_end =new Date(utilDate.getTime()); //The end time of another member's reservation
//            if ((reservation_due.after(reservation_start) && reservation_due.before(reservation_end)) ||
//                    (date.after(reservation_start) && date.before(reservation_end))){
//                return "The artifact has been reserved by another member at this time";
//            }
//
//        }
//        Reservation reservation = new Reservation();
//        reservation.setUser_id(userSession.getUser().getId());
//        reservation.setArtifact_id(artifact_id);
//        reservation.setDate(date);
//        reservationRepository.save(reservation);
//        return "Success";
//    }
//
//    @PostMapping("/reserve_available")
//    public String reserveAvailable(Long artifact_id){
//        java.util.Date date = new java.util.Date();
//        List<Reservation> reservationList = reservationRepository.findByArtifact_id(artifact_id);
//        Calendar calendar =new GregorianCalendar();
//        calendar.setTime(date);
//        calendar.add(Calendar.DATE, 1); //Reserve for 1 day
//        java.util.Date utilDate = (java.util.Date)calendar.getTime();
//        Date reservation_due =new Date(utilDate.getTime());
//        for (Reservation reservation : reservationList){
//            Date reservation_start = reservation.getDate(); //The start time of another member's reservation
//            calendar.setTime(date);
//            calendar.add(Calendar.DATE, 1);
//            java.util.Date newDate = (java.util.Date)calendar.getTime();
//            Date reservation_end =new Date(utilDate.getTime()); //The end time of another member's reservation
//            if ((reservation_due.after(reservation_start) && reservation_due.before(reservation_end)) ||
//                    (date.after(reservation_start) && date.before(reservation_end))){
//                return "The artifact has been reserved by another member at this time";
//            }
//        }
//        Reservation reservation = new Reservation();
//        reservation.setUser_id(userSession.getUser().getId());
//        reservation.setArtifact_id(artifact_id);
//        reservation.setDate(new Date(date.getTime()));
//        reservationRepository.save(reservation);
//        return "Success";
//    }

    @GetMapping("/loan_history")
    public String loanHistory(Model model){
        List<Loan> loanList = loanRepository.findByUser_id(userSession.getUser().getId());
        model.addAttribute("loanList", loanList);
        model.addAttribute("username", userSession.getUser().getName());
        return "MemberLoanHistory";
    }

    @GetMapping("/search_page")
    public String searchPage(){
        return "MemberSearch";
    }

    @PostMapping("/doSearch")
    public String searchByName(@RequestParam String name, Model model){
        List<Artifact> artifactList = artifactRepository.findByNameLike("%"+name+"%");
        model.addAttribute("artifacts", artifactList);
        return "MemberSearchResults";
    }


    @GetMapping("/view_profile")
    public String viewProfile(Model model){
        model.addAttribute("user", userSession.getUser());
        return "MemberUpdate";
    }

    @PostMapping("/update_profile")
    public void updateProfile(@RequestParam String name, HttpServletResponse response) throws IOException {
        userRepository.updateUser(userSession.getUser().getId(), name);
        User user = userSession.getUser();
        user.setName(name);
        userSession.setUser(user);
        response.sendRedirect("/member/view_profile");
    }

    @GetMapping("/management")
    public String managementPage(Model model){
        model.addAttribute("username", userSession.getUser().getName());
        return "MemberPortal";
    }

    @GetMapping("/login")
    public String login(){
//        if (userSession.getUser()==null){
//            return "MemberLogin";
//        }
//        else
//            return "MemberPortal";
        return "MemberLogin";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String email, @RequestParam String password, Model model, HttpServletResponse response) throws Exception {
        Optional<User> user = userRepository.findByEmailAndPassword(email, password);
        if (user.isPresent()) {
            User user1 = user.get();
            userSession.setUser(user1);
            model.addAttribute("username", user1.getName());
            return "MemberPortal";
        } else {
            userSession.setLoginFailed(true);
            return "MemberLogin";
        }

    }

}
