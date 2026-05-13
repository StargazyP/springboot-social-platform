package kr.co.inhatc.inhatc;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@Controller
public class HomeController {

    @GetMapping("/")
    public String login() {
        return "login";  // static/main.html 로 리다이렉트
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    /**
     * 게시물 상세 페이지
     */
    @GetMapping("/posts/{id}")
    public String postDetail(@PathVariable Long id) {
        return "post";
    }
    


    
}
