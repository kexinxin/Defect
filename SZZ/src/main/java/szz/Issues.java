package szz;

/**
 * Created by usi on 12/6/16.
 */

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Issues {


    private String user;
    private String repository;
    private Date created;
    private Date closed;
    private int number;
    private String Assignee;



    public Issues(String user,String repository){
        this.user=user;
        this.repository=repository;

    }


    public List<Issue> fetchIssue() throws Exception {

        GitHubClient git=new GitHubClient();
        git.setCredentials("TheMask", "sugandh4");
        RepositoryService repoService=new RepositoryService(git);
        IssueService issueService=new IssueService(git);
        org.eclipse.egit.github.core.Repository repo= repoService.getRepository(user, repository);
        //System.out.println(repo.getOpenIssues());
        Map<String, String> paramIssue=new HashMap<>();
        //paramIssue.put("sort", "created");
       paramIssue.put("state","closed");

        List<Issue> issueList=issueService.getIssues(user,repository,paramIssue);
        //List<Issue> issueList=issueService.getIssues(repository,paramIssue);
        //System.out.println("issuelist size------------->                  "+issueList.size());
        return issueList;

    }


}


