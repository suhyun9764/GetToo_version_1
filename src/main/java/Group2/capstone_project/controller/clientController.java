package Group2.capstone_project.controller;

import Group2.capstone_project.domain.Apply;
import Group2.capstone_project.domain.Client;
import Group2.capstone_project.domain.Club;
import Group2.capstone_project.domain.MemberShip;
import Group2.capstone_project.dto.client.ApplyDto;
import Group2.capstone_project.dto.client.ClientDto;
import Group2.capstone_project.service.clientService;
import Group2.capstone_project.session.SessionConst;
import Group2.capstone_project.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class clientController {


    private final PasswordEncoder passwordEncoder;
    private final clientService clientserivce;
    private final SessionManager sessionManager = new SessionManager();

    private final ResourceLoader resourceLoader;
    @Value("${upload.directory}")
    private String uploadDirectory;


    @Autowired
    public clientController(clientService clientService,PasswordEncoder passwordEncoder,ResourceLoader resourceLoader){
        this.clientserivce = clientService;
        this.passwordEncoder =passwordEncoder;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/")
    public String Home(HttpServletRequest request, Model model){
        HttpSession session = request.getSession(false);
        if(session == null){
            return "index.html";
        }
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        if(client==null)
            return "index.html";
        if("YES".equals(client.getAdminCheck())){
            return "redirect:/admin/adminIndex";
        }else {
            return "redirect:/loginClient";
        }
    }

    @GetMapping("/admin/adminIndex")
    public String adminPage(){
        return "admin/admin_index.html";
    }

    @GetMapping("/loginClient")
    public String loginClient(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if(session == null){
            return "login.html";
        }
        if(session!=null){
            Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
            if(client!=null){
                model.addAttribute("name", client.getName());
                return "loginClient/login_index.html";
            }
        }
        return "login.html";
    }

    @GetMapping("/admin/adminRedirLogin_index")
    public String adminRedirLoginIndex(){
        return "redirect:/loginClient";
    }
    @GetMapping("/admin/AdminGoLogin_index")
    public String adminLoginIndex(Model model,HttpServletRequest request){
        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("name",client.getName());
        return "loginClient/login_index.html";
    }



    @GetMapping("/index.html")
    public String Home2(HttpServletRequest request, Model model){
        HttpSession session = request.getSession(false);

        if(session == null){
            return "/index.html";
        }
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        if(client==null)
            return "/index.html";
        model.addAttribute("errorMessage", "권한이 없습니다");
        model.addAttribute("name", client.getName());
        return "/loginClient/login_index.html";
    }


    @GetMapping("/notAdmin")
    public String notAdmin(Model model){
        model.addAttribute("errorMessage", "권한이 없습니다");
        return "/loginClient/login_index.html";
    }
    @GetMapping("/gotoMembership")
    public String membership(){
        return "membership.html";
    }


    @GetMapping("/gotoJoin")
    public String joinForm(){
        return "join.html";
    }

    @GetMapping("/gotoLogin")
    public String loginHome(HttpServletRequest request, Model model){
        HttpSession session = request.getSession(false);
        if(session != null){
            Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
            if(client != null){

                session.setAttribute(SessionConst.LOGIN_CLIENT,client);
                model.addAttribute("name", client.getName());
                return "redirect:/loginClient/redirectLoginIndex";
            }
        }
        return "login.html";

    }




    @GetMapping("/client/goLogin_index")
    public String main(Model model,HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("errorMessage", "권한이 없습니다");
        model.addAttribute("name", client.getName());

        return "loginClient/login_index.html";
    }



    @PostMapping("/clientlogin")
    public String loginV3(@ModelAttribute ClientDto clientDto,@RequestParam(defaultValue = "/")String redirectURL,
                          HttpServletRequest request,Model model){

        Client client = new Client();
        client.setId(clientDto.getId());
        client.setPwd(clientDto.getPassword());
        Optional<Client> result = clientserivce.login(client);
        if(result!=null) {
            if("NO".equals(result.get().getJoinCheck())){
                model.addAttribute("errorMessage", "가입이 승인되지 않았습니다");
                return "login.html";
            }

            HttpSession session = request.getSession();
            session.setAttribute(SessionConst.LOGIN_CLIENT, result.get() );
            return "redirect:"+redirectURL;
        } else{
            model.addAttribute("errorMessage", "일치하는 계정정보가 없습니다");
            return "login.html";

        }
    }

    @GetMapping("/loginClient/outClub")
    public String outClub(@RequestParam("clubName")String clubName, HttpServletRequest request,Model model){
        HttpSession session = request.getSession(false);
        Client client =(Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        System.out.println(clientserivce.getLeaderByClub(clubName));
        if(clientserivce.getLeaderByClub(clubName).equals(client.getId())){
            List<Club> clubs = clientserivce.getClubByClient(client.getId());
            List<Club> leaderClubs = clientserivce.getClubLeaderByClient(client.getId());
            List<Club> notJoinClubs = clientserivce.getClubNotAuth(client.getId());
            model.addAttribute("clubs",clubs);
            model.addAttribute("leaderClubs",leaderClubs);
            model.addAttribute("notauthclub",notJoinClubs);
            model.addAttribute("name",client.getName());
            model.addAttribute("errorMessage","본인이 운영중인 동아리는 탈퇴할 수 없습니다");

            return "loginClient/login_group.html";
        }
        clientserivce.outClub(clubName,client.getId());

        List<Club> clubs = clientserivce.getClubByClient(client.getId());
        List<Club> leaderClubs = clientserivce.getClubLeaderByClient(client.getId());
        List<Club> notJoinClubs = clientserivce.getClubNotAuth(client.getId());
        model.addAttribute("clubs",clubs);
        model.addAttribute("leaderClubs",leaderClubs);
        model.addAttribute("notauthclub",notJoinClubs);
        model.addAttribute("name",client.getName());
        model.addAttribute("errorMessage","동아리 탈퇴가 완료되었습니다");

        return "loginClient/login_group.html";
    }

    @GetMapping("/loginClient/cancelApply")
    public String cancelApply(@RequestParam("clubName")String clubName, HttpServletRequest request,Model model){
        HttpSession session = request.getSession(false);
        Client client =(Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        clientserivce.cancelApply(clubName,client.getId());

        List<Club> clubs = clientserivce.getClubByClient(client.getId());
        List<Club> leaderClubs = clientserivce.getClubLeaderByClient(client.getId());
        List<Club> notJoinClubs = clientserivce.getClubNotAuth(client.getId());
        model.addAttribute("clubs",clubs);
        model.addAttribute("leaderClubs",leaderClubs);
        model.addAttribute("notauthclub",notJoinClubs);
        model.addAttribute("name",client.getName());
        model.addAttribute("errorMessage","동아리 지원신청이 취소되었습니다");

        return "loginClient/login_group.html";
    }



    @PostMapping("/client/join")
    public String create(ClientDto ClientDto){



        Client client = new Client();
        client.setId(ClientDto.getId());
        client.setName(ClientDto.getName());
        client.setAge(ClientDto.getAge());
        client.setEmail(ClientDto.getEmail());
        client.setStudentNumber(ClientDto.getStudentNumber());
        client.setPwd(passwordEncoder.encode(ClientDto.getPassword()));
        client.setSchool(ClientDto.getSchool());
        client.setDepartment(ClientDto.getDepartment());
        client.setClub(ClientDto.getClub());
        client.setLeader(ClientDto.getLeader());
        MultipartFile imageFile = ClientDto.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 이미지 파일을 저장할 경로를 설정합니다.
                String fileName = imageFile.getOriginalFilename();
                String storeFileName = createStoreFileName(fileName);
                Path filePath = Path.of(uploadDirectory, storeFileName);

                // 파일을 지정된 경로로 복사합니다.
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // 이미지 파일 경로를 클라이언트 객체에 설정합니다.
                client.setImagePath(filePath.toString());
            } catch (IOException e) {
                e.printStackTrace();
                // 에러 처리 방식에 따라 예외 처리 코드를 작성하세요.
            }
        }
        client.setQuestion(ClientDto.getQuestion());
        client.setAnswer(ClientDto.getAnswer());
        clientserivce.join(client);

        return "redirect:/afterjoin";
    }
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    @GetMapping("/afterjoin")
    public String afterjoin(Model model){
        model.addAttribute("errorMessage", "회원가입이 완료되었습니다. 가입승인까지 2~3일 정도 소요됩니다");
        return "login.html";
    }

 // 아이디 찾기 관련
    @PostMapping("/client/findID")
    public String findID(Model model, @ModelAttribute ClientDto ClientDto){

        Client client = new Client();
        client.setName(ClientDto.getName());
        client.setStudentNumber(ClientDto.getStudentNumber());
        client.setEmail(ClientDto.getEmail());
        String result = clientserivce.findId(client.getName(), client.getStudentNumber(), client.getEmail());
        if(result=="false"){
            return "redirect:/redirectIdSearch";
        }
        model.addAttribute("result",result);

        return "loginclient/checkyourId";
    }



    @GetMapping("/gotoIdSearch")
    public String gotoIdSearch(){
        return "idSearch.html";
    }

    @GetMapping("/gotoPwdSearch")
    public String gotoPwdSearch(){
        return "passwordsearch.html";
    }



    @GetMapping("/redirectIdSearch")
    public String redirectIdSearch(Model model){
        model.addAttribute("errorMessage", "일치하는 계정정보가 없습니다");
        return "idSearch.html";
    }

// 비밀번호 찾기 관련
    @GetMapping("/client/findPwd")
    public String findPwd(Model model, ClientDto ClientDto){
        Client client = new Client();
        client.setName(ClientDto.getName());
        client.setId(ClientDto.getId());
        client.setStudentNumber(ClientDto.getStudentNumber());
        client.setQuestion(ClientDto.getQuestion());
        client.setAnswer(ClientDto.getAnswer());
        Optional<Client> result = clientserivce.findPwd(client.getName(), client.getId(), client.getStudentNumber(),client.getQuestion(),client.getAnswer());
        if(result.isPresent()){
            model.addAttribute("id",result.get().getId());

            return "changeyourpwd.html";
        }else{
            return "redirect:/returnPwdSearch";
        }

    }
    @GetMapping("/returnPwdSearch")
    public String returnPwdSearch(Model model){
        model.addAttribute("errorMessage","일치하는 계정이 없습니다");
        return "passwordsearch.html";
    }

    @PostMapping("/changePwd")
    public String changePwd(@RequestParam("id") String id,@RequestParam("confirmPassword") String confirmPassword, @RequestParam("newPassword") String newPassword,Model model){
        if(!confirmPassword.equals(newPassword)){
            model.addAttribute("id",id);
            model.addAttribute("errorMessage","두 비밀번호가 일치하지 않습니다.");
            return "changeyourpwd.html";
        }
        String newEncodePwd = passwordEncoder.encode(newPassword);
        clientserivce.changePwd(id,newEncodePwd);
        return "login.html";

    }
    @GetMapping("/admin/list")
    public String adminPage(Model model) {
        List<Client> clients = clientserivce.findAll();
        model.addAttribute("clients", clients); // 클라이언트 리스트를 모델에 추가

        return "admin/adminPage"; // 클라이언트 리스트를 표시할 뷰의 이름 반환
    }


     @PostMapping ("/clientlogout")
    public String logOut(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session!=null) {
            session.invalidate();
        }
        return "redirect:/gotoLogin";
    }

    @GetMapping("/loginClient/clientInfo")
    public String clientInfo() {
        return "redirect:/loginClient/goClientInfo";
    }

    @GetMapping("loginClient/goClientInfo")
    public String goClientInfo(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);

        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client", client);
//        return "clientInfo.html";
        return "loginClient/login_profile.html";
    }

    @GetMapping("/loginClient/gotoDelete")
    public String gotoDelete(HttpServletRequest request,Model model){
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        List<Club> clubs = clientserivce.getClubLeaderByClient(client.getId());
        if(!clubs.isEmpty()){
            model.addAttribute("errorMessage","동아리장은 탈퇴할 수 없습니다");
            model.addAttribute("client", client);
            return "loginClient/login_profile.html";
        }

        return "loginClient/deletecheckpwd.html";
    }
    @PostMapping("/loginClient/clientDelete")
    public String clientDelete(HttpServletRequest request, @RequestParam("password") String password){
        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        boolean chk = clientserivce.checkPwd(client,password);
        if(chk){

            clientserivce.clientDelete(client);
            return "redirect:/afterDelete";
        }
        return "redirect:/afterCantDelete";
    }

    @GetMapping("/afterDelete")
    public String afterDelete(Model model){
        model.addAttribute("errorMessage", "회원탈퇴가 정상적으로 완료되었습니다. 감사합니다");
        return "login.html";
    }

    @GetMapping("/afterCantDelete")
    public String afterCantDelete(HttpServletRequest request,Model model){
        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client", client);
        model.addAttribute("errorMessage", "비밀번호가 틀렸습니다");
        return "loginClient/deletecheckpwd.html";
    }

    @GetMapping("/loginClient/DeleteInfoPage")
    public String DeleteInfo(HttpServletRequest request,Model model){
        HttpSession session = request.getSession(false);

        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client", client);
        return "loginClient/deleteinfo.html";
    }

    @GetMapping("/redirectLogin")
    public String redirectLogin(HttpServletRequest request, Model model) {
        String redirectURL = request.getParameter("redirectURL");
        model.addAttribute("redirectURL", redirectURL);
        model.addAttribute("errorMessage", "로그인 후 이용해주세요");
        return "login.html";
    }

    @GetMapping("loginClient/writeBoard")
    public String onlyLeader(HttpServletRequest request, Model model, RedirectAttributes attributes) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_CLIENT) == null) {
            model.addAttribute("errorMessage", "권한이 없습니다");
            return "redirect:/loginClient/redirectLoginIndex";
        }

        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        if (client == null || client.getId() == null) {
            model.addAttribute("errorMessage", "권한이 없습니다");
            return "redirect:/loginClient/redirectLoginIndex";
        }

            Client chkClient = clientserivce.findById(client.getId());
            if (chkClient == null || !"YES".equals(chkClient.getLeader())) {
                return "redirect:/loginClient?error=auth";
        }

        return "loginClient/createclubBoard_1.html";
    }

    @PostMapping("/loginClient/loginPwdChange")
    public String loginPwdChange(@RequestParam("password") String password,HttpServletRequest request){
        HttpSession session = request.getSession();
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);

        boolean chk = clientserivce.checkPwd(client, password);
        if(chk == true){
            return "/loginClient/loginPwdChange";
        }
        else{
            return "redirect:/loginClient/changePwdPage2";
        }
    }

    @PostMapping("/clientlogin/loginChangePassword")
    public String loginChangePassword(Model model,HttpServletRequest request,@RequestParam("newPassword") String password, @RequestParam("confirmPassword") String confirmPassword){
        if(!confirmPassword.equals(password)){
            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            return "loginClient/loginpwdchange.html";

        }

        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        String newEncodePwd = passwordEncoder.encode(password);
        clientserivce.changePwd(client.getId(),newEncodePwd);
            session.invalidate();

        return "redirect:/afterChange";
    }

    @GetMapping("/afterChange")
    public String afterChange(Model model){
        model.addAttribute("errorMessage", "비밀번호가 변경되었습니다. 다시 로그인해주세요");
        return "login.html";
    }

    @GetMapping("/loginClient/loginPwdChange")
    public String loginPwdChange(){
        return "loginClient/loginpwdchange";
    }
    @PostMapping("/client/check-id")
    public ResponseEntity<String> checkId(@RequestParam("id") String id) {

        boolean isAvailable = clientserivce.checkIdAvailability(id);
        if (isAvailable) {
            return ResponseEntity.ok("available");
        } else {
            return ResponseEntity.ok("not-available");
        }
    }

    @PostMapping("/admin/auth")
    public String authClient(@RequestParam("clientId") String clientId) {
        clientserivce.joinAuth(clientId);
        return "redirect:/admin/list";  // 작업 완료 후 리다이렉션할 페이지를 반환
    }

    @GetMapping("/admin/checkimg")
    public ResponseEntity<Resource> showStudentCardImage(@RequestParam("clientId") String clientId) throws IOException {
        Client client = clientserivce.findById(clientId);
        String imagePath = client.getImagePath() ;

        Resource imageResource = resourceLoader.getResource("file:" + imagePath);

        if (imageResource.exists()) {

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageResource);
        } else {

            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/loginClient/ShowMypage")
    public String gotoMypage(){
        return "loginClient/login_profile.html";
    }
    @GetMapping("/loginClient/changePwdPage")
    public String gotoChangePwd(){
        return "loginClient/loginpwdcheck.html";
    }
    @GetMapping("/loginClient/changePwdPage2")
    public String gotoChangePwd2(Model model)
    {
        model.addAttribute("errorMessage", "비밀번호가 틀렸습니다");
        return "loginClient/loginpwdcheck.html";
    }

    @GetMapping("/loginClient/loginGroup")
    public String loginGroup(Model model,HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        List<Club> clubs = clientserivce.getClubByClient(client.getId());
        List<Club> leaderClubs = clientserivce.getClubLeaderByClient(client.getId());
        List<Club> notJoinClubs = clientserivce.getClubNotAuth(client.getId());
            model.addAttribute("clubs",clubs);
            model.addAttribute("leaderClubs",leaderClubs);
            model.addAttribute("notauthclub",notJoinClubs);
            model.addAttribute("name",client.getName());

        return "loginClient/login_group.html";
    }

    @GetMapping("/loginClient/goClub")
    public String goClub(@RequestParam("name") String name,Model model,HttpServletRequest request){
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("name",client.getName());
        return "loginClient/"+name+".html";
    }


    @GetMapping("/loginClient/goClubHome")
    public String goClubHome(@RequestParam("name") String name,Model model,HttpServletRequest request){
        if(name ==null){
            HttpSession session = request.getSession(false);
            Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
            List<Club> clubs = clientserivce.getClubByClient(client.getId());
            List<Club> leaderClubs = clientserivce.getClubLeaderByClient(client.getId());
            List<Club> notJoinClubs = clientserivce.getClubNotAuth(client.getId());
            model.addAttribute("clubs",clubs);
            model.addAttribute("leaderClubs",leaderClubs);
            model.addAttribute("notauthclub",notJoinClubs);
            model.addAttribute("name",client.getName());
            model.addAttribute("errorMessage","가입이 승인되지 않은 동아리입니다");
            return "loginClient/login_group.html";
        }
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        Club club = clientserivce.getClubByClubName(name);
        Optional<MemberShip> memberShip = clientserivce.isJoinClub(name, client.getId());
        List<Client> clients = clientserivce.getJoinClub(memberShip.get().getClubName());
        System.out.println(memberShip.get().getClubName());
        if(memberShip.isPresent()){
            if(memberShip.get().getJoinAuth().equals("OK")) {
                model.addAttribute("member",clients.size());
                model.addAttribute("client", client);
                model.addAttribute("clubName", memberShip.get().getClubName());
                return "loginClient/group_home.html";
            }
        }
        List<Club> clubs = clientserivce.getClubByClient(client.getId());
        List<Club> leaderClubs = clientserivce.getClubLeaderByClient(client.getId());
        List<Club> notJoinClubs = clientserivce.getClubNotAuth(client.getId());
        model.addAttribute("clubs",clubs);
        model.addAttribute("leaderClubs",leaderClubs);
        model.addAttribute("notauthclub",notJoinClubs);
        model.addAttribute("name",client.getName());
        model.addAttribute("errorMessage","가입이 승인되지 않은 동아리입니다");
        return "loginClient/login_group.html";
    }

    @GetMapping("/loginClient/goClubJoinAuth")
    public String goClubJoinAuth(HttpServletRequest request,Model model,@RequestParam("clubName") String clubName){
        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        Club club = clientserivce.getClubByClubName(clubName);
        List<Client> clients = clientserivce.getWaitJoinClub(club.getClubName());

        if(client.getId()!=club.getLeader()){
            model.addAttribute("errorMessage","권한이 없습니다");
        }
        model.addAttribute("club",club);
        model.addAttribute("clients",clients);

        return "loginClient/clubjoinAuth.html";
    }

    @PostMapping("/loginClient/clubAuth")
    public String clubAuth(@RequestParam("clientId") String clientId, @RequestParam("clubName") String clubName,Model model){
        clientserivce.clubAuth(clientId,clubName);
        Club club = clientserivce.getClubByClubName(clubName);
        List<Client> clients = clientserivce.getWaitJoinClub(club.getClubName());
        model.addAttribute("club",club);
        model.addAttribute("clients",clients);
        return "loginClient/clubjoinAuth.html";
    }

    @PostMapping("/loginClient/clubReject")
    public String clubReject(@RequestParam("clientId") String clientId, @RequestParam("clubName") String clubName,Model model){
        clientserivce.clubReject(clientId,clubName);
        Club club = clientserivce.getClubByClubName(clubName);
        List<Client> clients = clientserivce.getWaitJoinClub(club.getClubName());
        model.addAttribute("club",club);
        model.addAttribute("clients",clients);
        return "loginClient/clubjoinAuth.html";
    }
    @GetMapping("loginClient/goApplyForm")
    public String goApplyForm(Model model,HttpServletRequest request,@RequestParam("name") String name){
        HttpSession session = request.getSession(false);

        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        Club club = clientserivce.getClubByClubName(name);
        Optional<MemberShip> memberShipOptional = clientserivce.isJoinClub(club.getClubName(),client.getId());
        if(memberShipOptional.isPresent()){
            MemberShip memberShip = memberShipOptional.get();
            if("OK".equals(memberShip.getJoinAuth())){
                model.addAttribute("errorMessage","이미 가입된 동아리입니다");
                model.addAttribute("name",client.getName());
                return "loginClient/"+name+".html";
            }else{
                model.addAttribute("errorMessage","이미 지원한 동아리입니다");
                model.addAttribute("name",client.getName());
                return "loginClient/"+name+".html";
            }
        }
        model.addAttribute("club",club);
        model.addAttribute("client",client);

        return "loginClient/apply_form.html";
    }

    @PostMapping("loginClient/applyClub")
    public String applyClub(ApplyDto applyDto ){

        Apply apply = new Apply();
        apply.setClientName(applyDto.getClientName());
        apply.setClubName(applyDto.getClubName());
        apply.setMotive(applyDto.getMotive());
        apply.setIntro(applyDto.getIntro());
        clientserivce.applyClub(apply);

        return "redirect:/loginClient/loginGroup";
    }

    @GetMapping("/loginClient/checkMotiveIntro")
    public String checkMotiveIntro(Model model,@RequestParam("clientId") String clientID, @RequestParam("clubName") String clubName){
        MemberShip apply = clientserivce.getApply(clubName,clientID);
        model.addAttribute("apply",apply);
        return "loginClient/motiveIntro.html";
    }

    @GetMapping("loginClient/goAlbum")
    public String goAlbum(Model model,HttpServletRequest request){
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client",client);
        return "loginClient/act_board.html";
    }
    @GetMapping("loginClient/goActBoard")
    public String goActBoard(Model model,HttpServletRequest request){
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client",client);
        return "loginClient/free_board.html";
    }

    @GetMapping("loginClient/goNoticeBoard")
    public String goNoticeBoard(Model model,HttpServletRequest request){
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client",client);
        return "loginClient/notice_board.html";
    }
}
