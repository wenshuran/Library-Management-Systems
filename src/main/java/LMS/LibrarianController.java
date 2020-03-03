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

    @PostMapping("/add_artifact")
    public Artifact addArtifact(String name, String type){
        Optional<Artifact> artifactOptional = artifactRepository.findByName(name);
        if (artifactOptional.isPresent()){ //Making a copy of an artifact
            artifactRepository.makeCopy(artifactOptional.get().getId());
        }
        Artifact artifact = new Artifact();
        artifact.setIs_copy(false);
        artifact.setName(name);
        artifact.setType(type);
        artifactRepository.save(artifact);
        return artifact;
    }

    @PostMapping("/remove_artifact")
    public void removeArtifact(long id){
        artifactRepository.deleteById(id);
    }

    @PostMapping("/search_member")
    public List<User> searchMember(String name){
        List<User> userList = userRepository.findByNameLike("%"+name+"%");
        return userList;
    }

    @PostMapping("/view_current_loan")
    public List<Loan> viewCurrentLoan(long user_id){
        List<Loan> loans = loanRepository.findByUser_id(user_id);
        List<Loan> currentLoans = new ArrayList<>();
        Date date = new Date(new java.util.Date().getTime());
        for (Loan loan : loans){
            if (loan.getDue_date().after(date)){
                currentLoans.add(loan);
            }
        }
        return currentLoans;
    }

    @PostMapping("/view_historical_loan")
    public List<Loan> viewHistoricalLoan(long user_id){
        List<Loan> loans = loanRepository.findByUser_id(user_id);
        if (!loans.isEmpty()){
            return loans;
        }
        else
            return null;
    }

    @PostMapping("/renew_loan")
    public String renewLoan(long id){
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
                    return "The book has been reserved by another member";
            }
            loanRepository.updateDueDate(loan.get().getId(), newDate);
            return "Success!";
        }
        else {
            return "Wrong loan id";
        }
    }

    @PostMapping("/reserve_artifact")
    public String reserveArtifact(long user_id, long artifact_id, Date date){
        List<Loan> loanList = loanRepository.findByArtifact_id(artifact_id);
        for (Loan loan : loanList){
            if(loan.getDue_date().after(date)){
                return "The artifact will still be hold by another member";
            }
        }
        List<Reservation> reservationList = reservationRepository.findByArtifact_id(artifact_id);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //Reserve for 1 day
        java.util.Date utilDate = (java.util.Date)calendar.getTime();
        Date reservation_due =new Date(utilDate.getTime());
        for (Reservation reservation : reservationList){
            Date reservation_start = reservation.getDate(); //The start time of another member's reservation
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            java.util.Date newDate = (java.util.Date)calendar.getTime();
            Date reservation_end = new Date(utilDate.getTime()); //The end time of another member's reservation
            if ((reservation_due.after(reservation_start) && reservation_due.before(reservation_end)) ||
                    (date.after(reservation_start) && date.before(reservation_end))){
                return "The artifact has been reserved by another member at this time";
            }
        }
        Reservation reservation = new Reservation();
        reservation.setUser_id(user_id);
        reservation.setArtifact_id(artifact_id);
        reservation.setDate(date);
        reservationRepository.save(reservation);
        return "Success";
    }

    @PostMapping("/record_loan")
    public String recordLoan(long user_id, long artifact_id){
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
                return "The artifact has been reserved by another member";
            }
        }

        Loan loan = new Loan();
        loan.setUser_id(user_id);
        loan.setArtifact_id(artifact_id);
        loan.setLoan_date(loan_date);
        loan.setDue_date(due_date);
        loanRepository.save(loan);
        return "Success!";
    }

    @PostMapping("/record_return")
    public void recordReturn(long id){
        Date due_date = new Date(new java.util.Date().getTime());
        loanRepository.updateDueDate(id, due_date);
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

    @PostMapping("/view_profile")
    public User viewProfile(long user_id){
        Optional<User> userOptional =  userRepository.findById(user_id);
        if (userOptional.isPresent())
            return userOptional.get();
        else
            return null;
    }

    @PostMapping("/update_profile")
    public void updateProfile(long user_id, String name, String gender){
        userRepository.updateUser(user_id, name, gender);
    }

    @GetMapping("/management")
    public String managementPage(Model model){
        model.addAttribute("User", userSession.getUser());
        return "member";
    }
}
