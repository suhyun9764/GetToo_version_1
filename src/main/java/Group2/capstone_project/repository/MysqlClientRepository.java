package Group2.capstone_project.repository;

import Group2.capstone_project.domain.Apply;
import Group2.capstone_project.domain.Client;
import Group2.capstone_project.domain.Club;
import Group2.capstone_project.domain.MemberShip;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class MysqlClientRepository implements ClientRepository{

    private final JdbcTemplate jdbcTemplate;

    public MysqlClientRepository(DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Override
    public void save(Client client) {

        String sql = "INSERT INTO client(id,name,age,studentNumber,email,school,department, pwd,imagepath,question, answer) values(?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,client.getId(),client.getName(),client.getAge(),client.getStudentNumber(),
                client.getEmail(),client.getSchool(),client.getDepartment(),client.getPwd(),client.getImagePath(),client.getQuestion(),client.getAnswer());
    }

    @Override
    public Optional<Client> findId(String name, String studentNumber, String email) {

        String[] object = {name,studentNumber,email};
        String sql = "SELECT * FROM client where name = ? and studentNumber = ? and email = ?";
        List<Client> result = jdbcTemplate.query(sql,clientRowMapper(),object);
        if(result.isEmpty()){

            return Optional.empty();
        }
        return result.stream().findAny();

    }

    @Override
    public Optional<Client> findPwd(String name, String id, String studentNumber, String question,String answer) {
        String[] object = {name,id,studentNumber, question, answer};
        String sql = "SELECT * FROM client where name = ? and id = ? and studentNumber = ? and question =? and answer=?";
        List<Client> result = jdbcTemplate.query(sql,clientRowMapper(),object);
        if(result.isEmpty()){

            return Optional.empty();
        }else {

            return result.stream().findAny();
        }
    }
    @Override
    public List<Client> findAll() {
        String sql = "SELECT * FROM client where joincheck ='NO'";
        return jdbcTemplate.query(sql,clientRowMapper());
    }


    @Override
    public Optional<Client> findById(String id) {
        String sql = "SELECT *FROM client WHERE id =?";
        List<Client> result = jdbcTemplate.query(sql, clientRowMapper(), id);
        return result.stream().findAny();
    }

    private RowMapper<Client> clientRowMapper(){
        return (rs, rowNum) -> {

            Client client = new Client();
            client.setId(rs.getString("id"));
            client.setName(rs.getString("name"));
            client.setStudentNumber(rs.getString("studentNumber"));
            client.setAge(rs.getString("age"));
            client.setPwd(rs.getString("pwd"));
            client.setEmail(rs.getString("email"));
            client.setSchool(rs.getString("school"));
            client.setDepartment(rs.getString("department"));
            client.setJoinCheck(rs.getString("joincheck"));
            client.setAdminCheck(rs.getString("admincheck"));
            client.setImagePath(rs.getString("imagepath"));
            return client;
        };
    }
    @Override
    public Optional<Client> login (Client client){
        String sql = "SELECT * FROM client where id = ?";
        List<Client> result = jdbcTemplate.query(sql,clientRowMapper(),client.getId());
        return result.stream().findAny();
    }



    @Override
    public void authJoin(String id) {
        String sql = "UPDATE client SET joincheck ='YES' WHERE id=?";
        jdbcTemplate.update(sql,id);
    }

    @Override
    public void clientDelete(String id, String studentNumber) {
        String sql1 = "DELETE FROM membership WHERE studentName = ?";
        String sql2 = "DELETE FROM client WHERE id = ? AND studentNumber = ?";
        jdbcTemplate.update(sql1,id);
       jdbcTemplate.update(sql2,id,studentNumber);
    }

    @Override
    public void updateInfo(Client client) {
        String sql = "UPDATE client SET name= ? , studentNumber =? , age=? WHERE id =?  ";
        String[] object = {client.getName(),client.getStudentNumber(),client.getAge(),client.getId()};
        jdbcTemplate.update(sql,client.getName(),client.getStudentNumber(),client.getAge(),client.getId());
    }

    @Override
    public List<Club> getClubByClient(String id) {
        String sql = "SELECT c.* FROM membership m JOIN club c ON m.clubName = c.clubName WHERE m.studentName = ? AND m.joinAuth =?";
        String[] ob = {id,"OK"};
        List<Club> clubs = jdbcTemplate.query(sql,clubRowMapper(),ob);
        return clubs;
    }

    @Override
    public List<Club> getClubLeaderByClient(String id) {
        String sql = "SELECT * FROM club WHERE leaderName =?";
        List<Club> clubs = jdbcTemplate.query(sql,clubRowMapper(),id);
        return clubs;
    }

    @Override
    public String getLeaderByClub(String clubName) {
        String sql = "SELECT * FROM club WHERE clubName =?";
        List<Club> club = jdbcTemplate.query(sql,clubRowMapper(),clubName);
        String result = club.stream().findAny().get().getLeader();
        return result;
    }

    @Override
    public List<Club> getClubNotAuth(String id) {
        String sql = "SELECT c.* FROM membership m JOIN club c ON m.clubName = c.clubName WHERE m.studentName = ? AND m.joinAuth =?";
        String[] ob = {id,"NO"};
        List<Club> clubs = jdbcTemplate.query(sql,clubRowMapper(),ob);
        return clubs;
    }

    @Override
    public List<Client> getJoinClub(String clubName) {
        String sql ="SELECT c.* FROM membership m JOIN client c ON m.studentName = c.id WHERE m.clubName=? AND m.joinAuth=?";
        String[] ob = {clubName,"OK"};
        List<Client> waitClients = jdbcTemplate.query(sql,clientRowMapper(),ob);
        return waitClients;
    }

    @Override
    public List<Client> getWaitJoinClub(String clubName) {
        String sql ="SELECT c.* FROM membership m JOIN client c ON m.studentName = c.id WHERE m.clubName=? AND m.joinAuth=?";
        String[] ob = {clubName,"NO"};
        List<Client> waitClients = jdbcTemplate.query(sql,clientRowMapper(),ob);
        return waitClients;
    }

    @Override
    public Club getClubByName(String clubName) {
        String sql ="SELECT * FROM club WHERE clubName=?";
        List<Club> clubs= jdbcTemplate.query(sql,clubRowMapper(),clubName);
        Club club = clubs.stream().findAny().get();
        return club;
    }

    @Override
    public void clubAuth(String clientName, String clubName) {
        String sql = "UPDATE membership SET joinAuth ='OK' WHERE studentName=? AND clubName=?";
        String[] ob = {clientName,clubName};
        jdbcTemplate.update(sql,ob);
    }

    @Override
    public void clubReject(String clientName, String clubName) {
        String sql = "DELETE FROM membership WHERE studentName = ? AND clubName = ?";
        String[] ob = {clientName,clubName};
        jdbcTemplate.update(sql,ob);
    }

    @Override
    public void applyClub(Apply apply) {
        String sql = "INSERT INTO membership(studentName,clubName,motive,intro) values(?,?,?,?)";
        String[] ob = {apply.getClientName(),apply.getClubName(), apply.getMotive(), apply.getIntro()};
        jdbcTemplate.update(sql,ob);
    }

    @Override
    public Optional<MemberShip> getApplyClub(String clubName, String clientName) {
        String sql = "SELECT motive,intro,clubName FROM membership WHERE clubName=? AND studentName=? AND joinAuth=?";
        String[] ob = {clubName,clientName,"NO"};
        List<MemberShip> memberShips = jdbcTemplate.query(sql,memberShipRowMapper(),ob);

        return memberShips.stream().findAny();
    }

    @Override
    public Optional<MemberShip> isJoinClub(String clubName, String clientName) {
        String sql = "SELECT studentName,clubName,joinAuth FROM membership WHERE clubName=? AND studentName=?";
        String[] ob = {clubName,clientName};
        List<MemberShip> memberShips = jdbcTemplate.query(sql,isJoinClubRowMapper(),ob);

        return memberShips.stream().findAny();
    }

    @Override
    public void outClub(String clubName, String clientId) {
        String sql = "DELETE FROM membership WHERE studentName = ? AND clubName=? AND joinAuth=?";
        String[] ob = {clientId,clubName,"OK"};
        jdbcTemplate.update(sql,ob);

    }

    @Override
    public void cancelApply(String clubName, String clientId) {
        String sql = "DELETE FROM membership WHERE studentName = ? AND clubName=? AND joinAuth=?";
        String[] ob = {clientId,clubName,"NO"};
        jdbcTemplate.update(sql,ob);
    }


    private RowMapper<Club> clubRowMapper(){
        return (rs, rowNum) -> {
            Club club = new Club();
            club.setClubName(rs.getString("clubName"));
            club.setLeader(rs.getString("leaderName"));
            return club;
        };
    }

    private RowMapper<MemberShip> memberShipRowMapper(){
        return (rs, rowNum) -> {
            MemberShip memberShip = new MemberShip();
            memberShip.setClubName(rs.getString("clubName"));
            memberShip.setIntro(rs.getString("intro"));
            memberShip.setMotive(rs.getString("motive"));
            return memberShip;
        };
    }

    private RowMapper<MemberShip> isJoinClubRowMapper(){
        return (rs, rowNum) -> {
            MemberShip memberShip = new MemberShip();
            memberShip.setClubName(rs.getString("clubName"));
            memberShip.setJoinAuth(rs.getString("joinAuth"));
            memberShip.setStudentName(rs.getString("studentName"));
            return memberShip;
        };
    }


    @Override
    public void updatePwd(String id, String newEncodePwd) {
        String sql = "UPDATE client SET pwd= ? WHERE id =?  ";

        jdbcTemplate.update(sql,newEncodePwd,id);
    }


}
