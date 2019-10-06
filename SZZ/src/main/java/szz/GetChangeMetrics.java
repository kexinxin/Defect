package szz;

import com.gitblit.models.PathModel.PathChangeModel;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.PersonIdent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class GetChangeMetrics {
    private static String project="neomutt";
    private static String REMOTE_URL = null;
    private static String commitHash=null;
    public static boolean flag= false;
    public static Map<Commit, List<Commit>> blameMap = new HashMap<>();
    public static Map<Commit, List<Issue>> issueMap = new HashMap<>();
    public static String user=null;
    public static String repositoryName=null;
    public static Map<String,Process> hashMap=new HashMap<>();

    public static void findBugInducingCommits(Commit commit, int fileCounter, Repository repository, Map<Commit, List<Commit>> blameMap ) throws Exception {
        List<PathChangeModel> plist = commit.getFilesInCommit(true);
        List<Commit> bCommits = new ArrayList<>();
        for (PathChangeModel path : plist) {
            Blamer blamer = new Blamer(path, repository);
            BlameResult bResult= blamer.blameGeneration(commit);
            new Diff(repository,commit).blameOnDiff(bResult, fileCounter,bCommits);
            fileCounter++;

        }
        blameMap.put(commit,bCommits);
    }


    public static void main(String[] args)throws Exception{
        REMOTE_URL = args[0];
        String[] urlParts= REMOTE_URL.split("/");
        String repoName=urlParts[urlParts.length-1];
        String[] repoParts= repoName.split("\\.");
        List<String> bugNumber = new LinkedList<>();
        List<Issue> issueList=null;
        user=urlParts[urlParts.length - 2];
        repositoryName=repoParts[0];
        Repository repository = Repository.getRemoteRepository(REMOTE_URL,urlParts[urlParts.length - 2]+repoParts[0].toUpperCase());


        //List<String> bugFileList=new ArrayList<String>();
        HashMap<String,Process> processMap=new HashMap<String,Process>();
        List<Commit> commitQueue=new ArrayList<Commit>();
        List<Commit> commitList = repository.getCommits();
        int currentData=commitList.get(0).getGitCommit().getCommitTime();
        for (Commit commit : commitList) {
            if (commit.isLikelyBugFixingCommit()) {
                int fileCounter = 0;
                findBugInducingCommits(commit, fileCounter, repository, blameMap);
            }
            //System.out.println(commit.getGitCommit().getFullMessage().toString());
            int commitTime=commit.getGitCommit().getCommitTime();
            int duration=currentData-commitTime;
            if(duration<24*60*60*30){
                commitQueue.add(commit);
            }
        }

        Set<String> bugFiles=new HashSet<String>();
        for( Map.Entry<Commit, List<Commit>> entry : blameMap.entrySet()){
            Commit key = entry.getKey();
            Set<Commit> dedupeCommit = new HashSet<>();
            for (Commit value : entry.getValue()) {
                dedupeCommit.add(value);
            }
            List<PathChangeModel> plist = key.getFilesInCommit(true);
            Set<String> fileChangeList=new HashSet<String>();
            for(PathChangeModel file:plist){
                fileChangeList.add(file.name);
            }
            for(Commit commit:dedupeCommit){
                List<PathChangeModel> tlist=commit.getFilesInCommit(true);
                for(PathChangeModel file:tlist){
                    String fileName=file.name.split("/")[file.name.split("/").length-1];
                    for(String changeFileName:fileChangeList){
                        if(changeFileName.indexOf(fileName)!=-1&&(changeFileName.endsWith(".c")||changeFileName.endsWith(".cpp"))){
                                bugFiles.add(file.name);
                        }
                    }
                }
            }
        }

        HashMap<String,List<FileCommit>> noBugCommitHash=new HashMap<String,List<FileCommit>>();
        for(Commit commit : commitList){
            List<PathChangeModel> plist=commit.getFilesInCommit(true);
            for(PathChangeModel file:plist){
                if(!bugFiles.contains(file.name)&&(file.name.endsWith(".c")||file.name.endsWith(".cpp"))){
                    if(!noBugCommitHash.containsKey(file.name)){
                        List<FileCommit> cl=new ArrayList<FileCommit>();
                        FileCommit fileCommit=new FileCommit();
                        fileCommit.add=file.insertions;
                        fileCommit.delete=file.deletions;
                        fileCommit.commitTime=commit.getGitCommit().getCommitTime();
                        cl.add(fileCommit);
                        noBugCommitHash.put(file.name,cl);
                    }else{
                        List<FileCommit> cl=noBugCommitHash.get(file.name);
                        FileCommit fileCommit=new FileCommit();
                        fileCommit.commitTime=commit.getGitCommit().getCommitTime();
                        fileCommit.add=file.insertions;
                        fileCommit.delete=file.deletions;
                        cl.add(fileCommit);
                        noBugCommitHash.put(file.name,cl);
                    }
                }
            }
        }

        for(String key:noBugCommitHash.keySet()){
            List<FileCommit> list=noBugCommitHash.get(key);
            int currentTime=list.get(0).commitTime;
            int delete=0;
            int add=0;
            for(FileCommit commit:list){
                int duration=currentData-commit.commitTime;
                if(duration<24*60*60*30*2){
                    delete+=commit.delete;
                    add+=commit.add;
                }else{
                    break;
                }
            }
            Process process=new Process();
            process.fileName=key;
            process.add=add;
            process.delete=delete;
            process.isBug=false;
            hashMap.put(key,process);
        }


//        //普通文件的过程度量元
//        for(int i=0;i<commitQueue.size();i++){
//            Commit commit=commitQueue.get(i);
//            List<PathChangeModel> plist = commit.getFilesInCommit(true);
//            for(PathChangeModel file:plist){
//                System.out.println(file.name);
//                if(!bugFiles.contains(file.name)&&(file.name.endsWith(".c")||file.name.endsWith(".cpp"))){
//                    if(!hashMap.containsKey(file.name)){
//                        Process process=new Process();
//                        process.fileName=file.name;
//                        process.currentData=commit.getGitCommit().getCommitTime();
//                        process.add=file.insertions;
//                        process.delete=file.deletions;
//                        process.modifier=file.insertions+file.deletions;
//                        process.isBug=false;
//                        String email=commit.getCommitter().getEmailAddress();
//                        process.emials.add(email);
//                        hashMap.put(file.name,process);
//                    }else{
//                        Process process=hashMap.get(file.name);
//                        process.add=process.add+file.insertions;
//                        process.delete=process.delete+file.deletions;
//                        process.modifier=process.modifier+file.deletions+file.insertions;
//                        String email=commit.getCommitter().getEmailAddress();
//                        process.emials.add(email);
//                        process.isBug=false;
//                        hashMap.put(file.name,process);
//                    }
//                }
//            }
//        }
//        System.out.println(hashMap.size());



        for( Map.Entry<Commit, List<Commit>> entry : blameMap.entrySet()) {
            Commit key = entry.getKey();
            int currentTime=key.getGitCommit().getCommitTime();
            List<Commit> dedupeCommit = new ArrayList<>();
            for (Commit value : entry.getValue()) {
                int commitTime=value.getGitCommit().getCommitTime();
                int duration=commitTime-currentTime;
                if(duration<24*60*60*30){
                    dedupeCommit.add(value);
                }
            }

            Set<String> alreadyDone=new HashSet<String>();
            for(int i=0;i<dedupeCommit.size();i++){
                Commit commit=dedupeCommit.get(i);
                List<PathChangeModel> plist = commit.getFilesInCommit(true);
                Set<String> temp=new HashSet<String>();
                for(PathChangeModel file:plist){
                    System.out.println(file.name);
                    if(bugFiles.contains(file.name)&&!alreadyDone.contains(file.name)){
                        if(!hashMap.containsKey(file.name)){
                            Process process=new Process();
                            process.fileName=file.name;
                            process.currentData=commit.getGitCommit().getCommitTime();
                            process.add=file.insertions;
                            process.delete=file.deletions;
                            process.modifier=file.insertions+file.deletions;
                            process.isBug=true;
                            String email=commit.getCommitter().getEmailAddress();
                            process.emials.add(email);
                            hashMap.put(file.name,process);
                        }else{
                            Process process=hashMap.get(file.name);
                            process.add=process.add+file.insertions;
                            process.delete=process.delete+file.deletions;
                            process.modifier=process.modifier+file.deletions+file.insertions;
                            String email=commit.getCommitter().getEmailAddress();
                            process.emials.add(email);
                            process.isBug=true;
                            hashMap.put(file.name,process);
                        }
                        temp.add(file.name);
                    }
                }
                alreadyDone.addAll(temp);
            }
            System.out.println("kexinxin");
        }


        File writeName = new File(project+".txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
        writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
        FileWriter writer = new FileWriter(writeName);
        BufferedWriter out = new BufferedWriter(writer);

        for(String key:hashMap.keySet()){
            Process process=hashMap.get(key);
            String fileName="";
            if(process.fileName.split("/").length>1){
                fileName=process.fileName.split("/")[process.fileName.split("/").length-2]+"/"+
                        process.fileName.split("/")[process.fileName.split("/").length-1];
            }else{
                fileName=process.fileName;
            }
            out.write(fileName+","+process.delete+","+process.add +","+process.isBug+"\r\n");
            out.flush(); // 把缓存区内容压入文件
        }
        System.out.println("");
    }
}