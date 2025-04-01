package com.example.bookreview.controller;

import com.example.bookreview.model.BingoCard;
import com.example.bookreview.model.PersonalBingoCard;
import com.example.bookreview.model.User;
import com.example.bookreview.repository.BingoCardRepository;
import com.example.bookreview.repository.PersonalBingoCardRepository;
import com.example.bookreview.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/bingo")
public class BingoController {

    @Autowired
    private BingoCardRepository bingoCardRepository;

    @Autowired
    private PersonalBingoCardRepository personalBingoCardRepository;

    @Autowired
    private UserService userService;

    private final List<String> CHALLENGE_POOL = Arrays.asList(
        "Read a book published this year",
        "Read a book with more than 400 pages",
        "Read a book from a genre you don't usually read",
        "Read a book recommended by a friend",
        "Read a book that was made into a movie",
        "Read a debut novel",
        "Read a book written by a local author",
        "Read a book with a one-word title",
        "Read a book translated from another language",
        "Read a book with a blue cover",
        "Read a book set in your hometown",
        "Read a book that won an award",
        "Read a book from your childhood",
        "Read a non-fiction book",
        "Read a book with a number in the title",
        "Read a book with a character who shares your profession",
        "Read a book written by an author under 30",
        "Read a book with a season in the title",
        "Read a book set in the future",
        "Read a book with a map in it",
        "Read a book recommended by a librarian",
        "Read a book published in the decade you were born",
        "Read a book with a protagonist over 60",
        "Read a book about books",
        "Read a book with a mythological creature"
    );

    @GetMapping("/card")
    public String viewCard(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        Optional<BingoCard> activeCard = bingoCardRepository.findFirstByUserAndCompletedFalseOrderByCreatedAtDesc(user);
        long completedCards = bingoCardRepository.countCompletedCards(user);

        if (!activeCard.isPresent()) {
            // Create a new card if no active card exists
            BingoCard newCard = createNewBingoCard(user);
            activeCard = Optional.of(newCard);
        }

        model.addAttribute("bingoCard", activeCard.get());
        model.addAttribute("completedCards", completedCards);
        model.addAttribute("customerName", customerName);
        return "book-bingo";
    }

    @PostMapping("/new-card")
    public String createNewCard(HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (userOpt.isPresent()) {
            createNewBingoCard(userOpt.get());
        }

        return "redirect:/bingo/card";
    }

    @PostMapping("/complete-challenge/{cardId}")
    @ResponseBody
    public Map<String, Object> completeChallenge(
            @PathVariable Long cardId,
            @RequestParam String challenge,
            @RequestParam(required = false, defaultValue = "false") boolean unselect,
            HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        Map<String, Object> response = new HashMap<>();
        
        if (customerName == null) {
            response.put("success", false);
            response.put("message", "Please log in to continue");
            return response;
        }

        Optional<BingoCard> cardOpt = bingoCardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Card not found");
            return response;
        }

        BingoCard card = cardOpt.get();
        if (card.getCompletedChallenges() == null) {
            card.setCompletedChallenges(new ArrayList<>());
        }

        if (unselect) {
            // Remove the challenge if it exists
            if (card.getCompletedChallenges().contains(challenge)) {
                card.getCompletedChallenges().remove(challenge);
                card.setCompleted(false); // Reset bingo status since we're removing a challenge
                bingoCardRepository.save(card);
                response.put("success", true);
                response.put("message", "Challenge unselected");
                response.put("progress", card.getProgress());
            } else {
                response.put("success", false);
                response.put("message", "Challenge was not completed");
            }
        } else {
            // Add the challenge if it doesn't exist
            if (!card.getCompletedChallenges().contains(challenge)) {
                card.getCompletedChallenges().add(challenge);
                
                if (card.hasBingo()) {
                    card.setCompleted(true);
                    response.put("bingo", true);
                    response.put("message", "BINGO! You've completed a line!");
                } else {
                    response.put("message", "Challenge completed!");
                }
                
                bingoCardRepository.save(card);
                response.put("success", true);
                response.put("progress", card.getProgress());
            } else {
                response.put("success", false);
                response.put("message", "Challenge already completed");
            }
        }

        return response;
    }

    @PostMapping("/save-card/{cardId}")
    @ResponseBody
    public Map<String, Object> saveCard(@PathVariable Long cardId, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        Map<String, Object> response = new HashMap<>();
        
        if (customerName == null) {
            response.put("success", false);
            response.put("message", "Please log in to continue");
            return response;
        }

        Optional<BingoCard> cardOpt = bingoCardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Card not found");
            return response;
        }

        BingoCard card = cardOpt.get();
        try {
            bingoCardRepository.save(card);
            response.put("success", true);
            response.put("message", "Card saved successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to save card. Please try again.");
        }
        
        return response;
    }

    private BingoCard createNewBingoCard(User user) {
        BingoCard card = new BingoCard(user);
        
        // Randomly select 25 unique challenges for a 5x5 grid
        List<String> shuffledChallenges = new ArrayList<>(CHALLENGE_POOL);
        Collections.shuffle(shuffledChallenges);
        card.setChallenges(shuffledChallenges.subList(0, Math.min(25, shuffledChallenges.size())));
        card.setCompletedChallenges(new ArrayList<>());
        
        return bingoCardRepository.save(card);
    }

    @GetMapping("/personal")
    public String viewPersonalCards(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        List<PersonalBingoCard> personalCards = personalBingoCardRepository.findByUserOrderByCreatedAtDesc(user);
        
        model.addAttribute("personalCards", personalCards);
        model.addAttribute("customerName", customerName);
        return "personal-bingo-cards";
    }

    @PostMapping("/personal/create")
    @ResponseBody
    public Map<String, Object> createPersonalCard(
            @RequestParam String title,
            @RequestParam List<String> challenges,
            HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        Map<String, Object> response = new HashMap<>();
        
        if (customerName == null) {
            response.put("success", false);
            response.put("message", "Please log in to continue");
            return response;
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        try {
            PersonalBingoCard card = new PersonalBingoCard(userOpt.get(), title);
            card.setChallenges(challenges);
            personalBingoCardRepository.save(card);
            
            response.put("success", true);
            response.put("message", "Personal bingo card created successfully!");
            response.put("cardId", card.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create personal card. Please try again.");
        }
        
        return response;
    }

    @PostMapping("/personal/complete-challenge/{cardId}")
    @ResponseBody
    public Map<String, Object> completePersonalChallenge(
            @PathVariable Long cardId,
            @RequestParam String challenge,
            @RequestParam(required = false, defaultValue = "false") boolean unselect,
            HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        Map<String, Object> response = new HashMap<>();
        
        if (customerName == null) {
            response.put("success", false);
            response.put("message", "Please log in to continue");
            return response;
        }

        Optional<PersonalBingoCard> cardOpt = personalBingoCardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Card not found");
            return response;
        }

        PersonalBingoCard card = cardOpt.get();
        if (!card.getUser().getUsername().equals(customerName)) {
            response.put("success", false);
            response.put("message", "You don't have permission to modify this card");
            return response;
        }

        if (unselect) {
            if (card.getCompletedChallenges().contains(challenge)) {
                card.getCompletedChallenges().remove(challenge);
                card.setCompleted(false);
                personalBingoCardRepository.save(card);
                response.put("success", true);
                response.put("message", "Challenge unselected");
                response.put("progress", card.getProgress());
            } else {
                response.put("success", false);
                response.put("message", "Challenge was not completed");
            }
        } else {
            if (!card.getCompletedChallenges().contains(challenge)) {
                card.getCompletedChallenges().add(challenge);
                
                if (card.hasBingo()) {
                    card.setCompleted(true);
                    response.put("bingo", true);
                    response.put("message", "BINGO! You've completed a line!");
                } else {
                    response.put("message", "Challenge completed!");
                }
                
                personalBingoCardRepository.save(card);
                response.put("success", true);
                response.put("progress", card.getProgress());
            } else {
                response.put("success", false);
                response.put("message", "Challenge already completed");
            }
        }

        return response;
    }

    @PostMapping("/add-challenge/{cardId}")
    @ResponseBody
    public Map<String, Object> addCustomChallenge(
            @PathVariable Long cardId,
            @RequestParam String challenge,
            HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        Map<String, Object> response = new HashMap<>();
        
        if (customerName == null) {
            response.put("success", false);
            response.put("message", "Please log in to continue");
            return response;
        }

        Optional<BingoCard> cardOpt = bingoCardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Card not found");
            return response;
        }

        BingoCard card = cardOpt.get();
        if (!card.getUser().getUsername().equals(customerName)) {
            response.put("success", false);
            response.put("message", "You don't have permission to modify this card");
            return response;
        }

        try {
            if (card.getChallenges() == null) {
                card.setChallenges(new ArrayList<>());
            }
            
            if (card.getChallenges().size() >= 30) {
                response.put("success", false);
                response.put("message", "Card is already full (maximum 30 challenges)");
                return response;
            }

            // Add the new challenge
            card.getChallenges().add(challenge);
            bingoCardRepository.save(card);
            
            response.put("success", true);
            response.put("message", "Challenge added successfully!");
            response.put("challenge", challenge);
            response.put("totalChallenges", card.getChallenges().size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add challenge. Please try again.");
        }
        
        return response;
    }

    @PostMapping("/delete-challenge/{cardId}")
    @ResponseBody
    public Map<String, Object> deleteChallenge(
            @PathVariable Long cardId,
            @RequestParam String challenge,
            HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        Map<String, Object> response = new HashMap<>();
        
        if (customerName == null) {
            response.put("success", false);
            response.put("message", "Please log in to continue");
            return response;
        }

        Optional<BingoCard> cardOpt = bingoCardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Card not found");
            return response;
        }

        BingoCard card = cardOpt.get();
        if (!card.getUser().getUsername().equals(customerName)) {
            response.put("success", false);
            response.put("message", "You don't have permission to modify this card");
            return response;
        }

        try {
            if (card.getChallenges() == null || !card.getChallenges().contains(challenge)) {
                response.put("success", false);
                response.put("message", "Challenge not found");
                return response;
            }

            // Remove the challenge
            card.getChallenges().remove(challenge);
            
            // Also remove from completed challenges if it was completed
            if (card.getCompletedChallenges() != null) {
                card.getCompletedChallenges().remove(challenge);
            }
            
            bingoCardRepository.save(card);
            
            response.put("success", true);
            response.put("message", "Challenge deleted successfully!");
            response.put("totalChallenges", card.getChallenges().size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete challenge. Please try again.");
        }
        
        return response;
    }

    @GetMapping("/history")
    public String viewHistory(Model model, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        if (customerName == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findByUsername(customerName);
        if (!userOpt.isPresent()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        List<BingoCard> cards = bingoCardRepository.findByUserOrderByCreatedAtDesc(user);
        
        model.addAttribute("cards", cards);
        model.addAttribute("customerName", customerName);
        return "bingo-history";
    }

    @PostMapping("/delete-card/{cardId}")
    @ResponseBody
    public Map<String, Object> deleteCard(@PathVariable Long cardId, HttpSession session) {
        String customerName = (String) session.getAttribute("customerName");
        Map<String, Object> response = new HashMap<>();
        
        if (customerName == null) {
            response.put("success", false);
            response.put("message", "Please log in to continue");
            return response;
        }

        Optional<BingoCard> cardOpt = bingoCardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Card not found");
            return response;
        }

        BingoCard card = cardOpt.get();
        if (!card.getUser().getUsername().equals(customerName)) {
            response.put("success", false);
            response.put("message", "You don't have permission to delete this card");
            return response;
        }

        try {
            bingoCardRepository.delete(card);
            response.put("success", true);
            response.put("message", "Card deleted successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete card. Please try again.");
        }
        
        return response;
    }
} 