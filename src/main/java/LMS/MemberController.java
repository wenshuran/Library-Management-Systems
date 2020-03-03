package LMS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public List<Loan> listLoans(){
        List<Loan> loans = loanRepository.findByUser_id(userSession.getUser().getId());
        List<Loan> currentLoans = new ArrayList<>();
        Date date = new Date(new java.util.Date().getTime());
        for (Loan loan : loans){
            if (loan.getDue_date().after(date)){
                currentLoans.add(loan);
            }
        }
        return currentLoans;
    }

    @GetMapping("/renew_loans")
    public String renewLoans(Long id){
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
                    return "The book has been reserved by another member";
            }
            loanRepository.updateDueDate(loan.get().getId(), newDate);
            return "Success!";
        }
        else {
            return "Wrong loan id";
        }
    }

    @PostMapping("/reserve_loan")
    public String reserveLoan(Long artifact_id, Date date){
        List<Loan> loanList = loanRepository.findByArtifact_id(artifact_id);
        for (Loan loan : loanList){
            if(loan.getDue_date().after(date)){
                return "The artifact will still be hold by another member";
            }
        }
        List<Reservation> reservationList = reservationRepository.findByArtifact_id(artifact_id);
        Calendar calendar =new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //Reserve for 1 day
        java.util.Date utilDate = (java.util.Date)calendar.getTime();
        Date reservation_due =new Date(utilDate.getTime());
        for (Reservation reservation : reservationList){
            Date reservation_start = reservation.getDate(); //The start time of another member's reservation
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            java.util.Date newDate = (java.util.Date)calendar.getTime();
            Date reservation_end =new Date(utilDate.getTime()); //The end time of another member's reservation
            if ((reservation_due.after(reservation_start) && reservation_due.before(reservation_end)) ||
                    (date.after(reservation_start) && date.before(reservation_end))){
                return "The artifact has been reserved by another member at this time";
            }

        }
        Reservation reservation = new Reservation();
        reservation.setUser_id(userSession.getUser().getId());
        reservation.setArtifact_id(artifact_id);
        reservation.setDate(date);
        reservationRepository.save(reservation);
        return "Success";
    }

    @PostMapping("/reserve_available")
    public String reserveAvailable(Long artifact_id){
        java.util.Date date = new java.util.Date();
        List<Reservation> reservationList = reservationRepository.findByArtifact_id(artifact_id);
        Calendar calendar =new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //Reserve for 1 day
        java.util.Date utilDate = (java.util.Date)calendar.getTime();
        Date reservation_due =new Date(utilDate.getTime());
        for (Reservation reservation : reservationList){
            Date reservation_start = reservation.getDate(); //The start time of another member's reservation
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            java.util.Date newDate = (java.util.Date)calendar.getTime();
            Date reservation_end =new Date(utilDate.getTime()); //The end time of another member's reservation
            if ((reservation_due.after(reservation_start) && reservation_due.before(reservation_end)) ||
                    (date.after(reservation_start) && date.before(reservation_end))){
                return "The artifact has been reserved by another member at this time";
            }
        }
        Reservation reservation = new Reservation();
        reservation.setUser_id(userSession.getUser().getId());
        reservation.setArtifact_id(artifact_id);
        reservation.setDate(new Date(date.getTime()));
        reservationRepository.save(reservation);
        return "Success";
    }

    @GetMapping("/loan_history")
    public List<Loan> loanHistory(){
        List<Loan> loanList = loanRepository.findByUser_id(userSession.getUser().getId());
        return loanList;
    }

    @PostMapping("/search_by_name")
    public List<Artifact> searchByName(String name){
        List<Artifact> artifactList = artifactRepository.findByNameLike("%"+name+"%");
        return artifactList;
    }

    @PostMapping("/search_by_type")
    public List<Artifact> searchByType(String type){
        List<Artifact> artifactList = artifactRepository.findByType(type);
        return artifactList;
    }

    @GetMapping("/view_profile")
    public User viewProfile(){
        return userSession.getUser();
    }

    @PostMapping("/update_profile")
    public void updateProfile(String name, String gender){
        userRepository.updateUser(userSession.getUser().getId(), name, gender);
    }

    @GetMapping("/management")
    public String managementPage(Model model){
        model.addAttribute("User", userSession.getUser());
        return "member";
    }
}
