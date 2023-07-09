package Group2.capstone_project.domain;

import Group2.capstone_project.repository.ClientRepository;

import java.util.List;

public class Club {
    ClientRepository clientRepository;

    private String clubName;
    private String leader;

    public int getWaitClient() {
        return waitClient;
    }

    public void setWaitClient(int waitClient) {
        this.waitClient = waitClient;
    }

    private int waitClient;

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }
}
