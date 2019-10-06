package szz;

/**
 * Created by usi on 11/15/16.
 */

import com.gitblit.models.PathModel.PathChangeModel;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.revwalk.*;
import java.util.Date;

import java.util.*;

public class Main {

    private static String REMOTE_URL = null;
    private static String commitHash=null;
    public static boolean flag= false;
    private static Map<Commit, List<Commit>> blameMap = new HashMap<>();
    public static Map<Commit, List<Issue>> issueMap = new HashMap<>();
    public static String user=null;
    public static String repositoryName=null;

    public static void findBugInducingCommits(Commit commit, int fileCounter, Repository repository, Map<Commit, List<Commit>> blameMap ) throws Exception {
        List<PathChangeModel> plist = commit.getFilesInCommit(true);
        List<Commit> bCommits = new ArrayList<>();
        for (PathChangeModel path : plist) {
            //System.out.println(path.path);
            //System.out.println("deletions:   " + path.deletions);
            //System.out.println("insertions:   " + path.insertions);

            Blamer blamer = new Blamer(path, repository);
            //Set<RevCommit> listCommiter = blamer.blameGeneration(commit);
            BlameResult bResult= blamer.blameGeneration(commit);

            new Diff(repository,commit).blameOnDiff(bResult, fileCounter,bCommits);
            fileCounter++;

        }
        blameMap.put(commit,bCommits);
    }
    public static void main(String[] args) throws Exception {
        REMOTE_URL = args[0];
        String[] urlParts= REMOTE_URL.split("/");
        String repoName=urlParts[urlParts.length-1];
        String[] repoParts= repoName.split("\\.");
        List<String> bugNumber = new LinkedList<>();
        List<Issue> issueList=null;

        user=urlParts[urlParts.length - 2];
        repositoryName=repoParts[0];
        Repository repository = Repository.getRemoteRepository(REMOTE_URL,urlParts[urlParts.length - 2]+repoParts[0].toUpperCase());
        if(true) {
            //System.out.println(urlParts[urlParts.length - 2]+repoParts[0]);

            issueList = new Issues(user, repositoryName).fetchIssue();
            System.out.println("Number of Issues in Repository:\t"+ issueList.size());

            for(Issue issue: issueList){

                bugNumber.add(String.valueOf(issue.getNumber()));
                //System.out.println(issue.getNumber());
            }

        }

        if(true){
            List<Commit> commitList = repository.getCommits();
            int commitCounter = 0;
            System.out.println("Number of commits in repository: \t"+commitList.size());

            for (Commit commit : commitList) {

                if(false) {
                    if (commit.isLikelyBugFixingCommit()) {
                        commitCounter++;
                        //System.out.println("(#" + commitCounter + ") + bug fixing commit \t" + commit);
                        int fileCounter = 0;
                        findBugInducingCommits(commit, fileCounter, repository, blameMap);
                    }
                }else {


                    Boolean match = false;
//                    for(String bugNum: bugNumber){
//                        if (commit.getGitCommit().getFullMessage().contains(bugNum)){
//                            match=true;
//                        }
//                    }

                    List <Issue> issueMatched = new ArrayList<>();
                    for (Issue issue : issueList) {
                        boolean isBug=false;
                        for(Label label:issue.getLabels()){
                            if(label.getName().equals("bug")){
                                isBug=true;
                            }
                        }
                        isBug=true;
                        if (commit.getGitCommit().getFullMessage().contains("#"+String.valueOf(issue.getNumber())+" ")&&isBug) {
                            match = true;
                            issueMatched.add(issue);
                        }
                    }
                    match=true;

                    if ((commit.isLikelyBugFixingCommit()) && match) {
                        commitCounter++;
                        //System.out.println("(#" + commitCounter + ") + bug fixing commit \t" + commit);
                        int fileCounter = 0;
                        issueMap.put(commit, issueMatched);
                        findBugInducingCommits(commit, fileCounter, repository, blameMap);
                    }
                }


            }
            System.out.println("BugFix commits found:\t"+commitCounter);

        } else{
            flag=true;
            commitHash= args[1];
            RevWalk RW= new RevWalk(repository.getGitRepository());
            Commit commit= new Commit(repository,RW
                    .parseCommit(repository.getGitRepository()
                            .resolve(commitHash)));
            RW.dispose();

            System.out.println(" bug fixing commit \t" + commit);
            int fileCounter=0;
            findBugInducingCommits(commit,fileCounter,repository, blameMap);
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
        new CsvOut().writeToCSV(repoParts[0],blameMap,issueMap,user,repositoryName);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Set<String> bugFiles=new HashSet<String>();
        for( Map.Entry<Commit, List<Commit>> entry : blameMap.entrySet()) {
            Commit key = entry.getKey();
            Set<Commit> dedupeCommit = new HashSet<>();
            //dedupeCommit.add(key);

//            List<Issue> tempIssueList=issueMap.get(key);
//            Date earlyData=new Date();
//            for(Issue issue:tempIssueList){
//                Date data=issue.getCreatedAt();
//                if(data.before(earlyData)) earlyData=data;
//            }


            for (Commit value : entry.getValue()) {
                //if(value.getGitCommit().getCommitTime()<earlyData.getTime())  dedupeCommit.add(value);
                dedupeCommit.add(value);
            }

            List<PathChangeModel> plist = key.getFilesInCommit(true);



//            String bugfile=null;
//            int changeLine=0;
//            List<String> strList=new ArrayList<String>();
//            for(Commit commit:dedupeCommit){
//                List<PathChangeModel> tlist=commit.getFilesInCommit(true);
//                for(PathChangeModel file:tlist){
//                    String fileName=file.name.split("/")[file.name.split("/").length-1];
//                    if(!strList.contains(fileName)) strList.add(fileName);
//               }
//            }
//            for(PathChangeModel changefile:plist){
//                for(String str:strList){
//                    if(changefile.name.indexOf(str)!=-1&&(str.endsWith(".c")||str.endsWith(".cpp"))){
//                        //changeLine=changefile.insertions+changefile.deletions;
//
//                        if(changeLine<changefile.insertions+changefile.deletions){
//                            changeLine=changefile.insertions+changefile.deletions;
//                            bugfile=changefile.name;
//                        }
//
//
//                    }
//                }
//            }
//
//            if(bugfile!=null&&!bugFiles.contains(bugfile)) bugFiles.add(bugfile);




            Set<String> fileChangeList=new HashSet<String>();
            for(PathChangeModel file:plist){
                fileChangeList.add(file.name);
            }
            for(Commit commit:dedupeCommit){
                List<PathChangeModel> tlist=commit.getFilesInCommit(true);
                for(PathChangeModel file:tlist){
                    String fileName=file.name.split("/")[file.name.split("/").length-1];
                    for(String changeFileName:fileChangeList){
                        if(changeFileName.indexOf(fileName)!=-1){
                            if(file.name.endsWith(".c")||file.name.endsWith(".cpp"))
                                bugFiles.add(file.name);
                        }
                    }
                }
            }
        }
        for(String name:bugFiles){
            System.out.println(name);
        }
        System.out.println(bugFiles.size());
    }
}
