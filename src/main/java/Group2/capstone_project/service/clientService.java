package Group2.capstone_project.service;

import Group2.capstone_project.domain.Apply;
import Group2.capstone_project.domain.Client;
import Group2.capstone_project.domain.Club;
import Group2.capstone_project.domain.MemberShip;
import Group2.capstone_project.repository.ClientRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;


public class clientService {

    PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();

    private final ClientRepository clientRepository;

    public clientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void join(Client client) {
        validateDuplicateMember(client);
        clientRepository.save(client);
    }

    private void validateDuplicateMember(Client client) {
        clientRepository.findById(client.getId())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 id입니다");
                });
    }

    public String findId(String clientName, String clientStudentNumber, String clientEmail) {
        Optional<Client> client = clientRepository.findId(clientName, clientStudentNumber,clientEmail);
        if(client.isPresent())
            return client.get().getId();
        else
            return "false";
    }

    public Optional<Client> findPwd(String clientName, String clientId, String clientStudentNumber, String question, String answer) {
        Optional<Client> client = clientRepository.findPwd(clientName, clientId, clientStudentNumber,question,answer);
        return client;
    }

    public void changePwd(String id, String newEncodePwd){
        clientRepository.updatePwd(id,newEncodePwd);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public boolean checkPwd(Client client,String password){

        if(passwordEncoder.matches(password,client.getPwd())){
            return true;
        }
        else {
            return false;
        }
    }
    public Optional<Client> login(Client client) {
        Optional<Client> result = clientRepository.login(client);
        if (result.isPresent()) {

            Client chkclient = result.get();
            if (passwordEncoder.matches(client.getPwd(),chkclient.getPwd())) {
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Client updateForm(String id){
        if(clientRepository.findById(id).isPresent()) {
            Client client = clientRepository.findById(id).get();
            return client;
        }
        else{
            return null;
        }

    }
    public void clientDelete(Client client){
        clientRepository.clientDelete(client.getId(),client.getStudentNumber());
    }

    public Client findById(String id){
        return clientRepository.findById(id).get();
    }
    public void updateInfo(Client client){
        clientRepository.updateInfo(client);
    }

    public void joinAuth(String id){
        clientRepository.authJoin(id);
    }
    public boolean checkIdAvailability(String id) {
        Optional<Client> optionalClient = clientRepository.findById(id);
        return !optionalClient.isPresent();
    }

    public List<Club> getClubByClient(String id){
        List<Club> clubs = clientRepository.getClubByClient(id);
        return clubs;
    }

    public List<Club> getClubLeaderByClient(String id){
        List<Club> leaderClubs = clientRepository.getClubLeaderByClient(id);
        return leaderClubs;
    }

    public List<Club> getClubNotAuth(String id){
        List<Club> notJoinClubs = clientRepository.getClubNotAuth(id);
        return notJoinClubs;
    }

    public List<Client> getWaitJoinClub(String clubName){
        List<Client> waitClients = clientRepository.getWaitJoinClub(clubName);
        return waitClients;
    }

    public List<Client> getJoinClub(String clubName){
        List<Client> Clients = clientRepository.getJoinClub(clubName);
        return Clients;
    }



    public Club getClubByClubName(String clubName){
        Club club = clientRepository.getClubByName(clubName);
        return club;
    }

    public void clubAuth(String clientName,String clubName){
        clientRepository.clubAuth(clientName,clubName);
    }

    public void clubReject(String clientName,String clubName){
        clientRepository.clubReject(clientName,clubName);
    }

    public void applyClub(Apply apply){
        clientRepository.applyClub(apply);
    }

    public MemberShip getApply(String clubName, String clientName){
        Optional<MemberShip> memberShip = clientRepository.getApplyClub(clubName,clientName);
        MemberShip memberShip1 = memberShip.get();
        return memberShip1;
    }

    public Optional<MemberShip> isJoinClub(String clubName,String clientName){
        Optional<MemberShip> memberShip = clientRepository.isJoinClub(clubName,clientName);
        return memberShip;
    }

    public void outClub(String clubName,String clientId){
        clientRepository.outClub(clubName,clientId);
    }

    public void cancelApply(String clubName,String clientId){
        clientRepository.cancelApply(clubName,clientId);
    }

    public String getLeaderByClub(String clubName){
        String result = clientRepository.getLeaderByClub(clubName);
        return result;
    }

}
