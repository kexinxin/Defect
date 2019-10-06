package szz;

import com.gitblit.models.RefModel;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by usi on 11/29/16.
 */
public class Repository {

    private org.eclipse.jgit.lib.Repository gitRepository;
    private List<Commit> commits = null;

    public Repository(org.eclipse.jgit.lib.Repository gitRepository) {
        this.gitRepository = gitRepository;
    }

    public org.eclipse.jgit.lib.Repository getGitRepository() {
        return this.gitRepository;
    }

    public static Repository getRemoteRepository(String remoteURL, String repositoryName) throws IOException, GitAPIException {

        File repoPath = new File("/Users/usi/Git");
        if(!repoPath.exists()){
            repoPath.mkdir();
        }


        File localPath = new File(repoPath,repositoryName);
        if(!localPath.exists()){
            Git result = Git.cloneRepository().setURI(remoteURL).setDirectory(localPath).call();
            org.eclipse.jgit.lib.Repository repository;
            repository = result.getRepository();
            System.out.println("Cloning from " + remoteURL + " to " + localPath);

            return new Repository(repository);



        }else {
            Git result = Git.init().setDirectory(localPath).call();
            org.eclipse.jgit.lib.Repository repository;
            repository = result.getRepository();
            System.out.println("Fetching from  " + localPath);

            return new Repository(repository);
        }

    }


    public boolean hasCommits() {

        return !this.commits.isEmpty();
    }

    public List<RefModel> getLocalBranches(boolean fullName,
                                           int maxCount) throws IOException {
        return getRefs(Constants.R_HEADS, fullName, maxCount, 0);
    }

    private List<RefModel> getRefs(String refs, boolean fullName,
                                   int maxCount, int offset) throws IOException
    {
        List<RefModel> list = new ArrayList<>();
        if (maxCount == 0)
        {
            return list;
        }
        if (!this.hasCommits())
        {
            return list;
        }

        Map<String, Ref> map = this.gitRepository.getRefDatabase().getRefs(refs);
        RevWalk rw = new RevWalk(this.gitRepository);
        for (Map.Entry<String, Ref> entry : map.entrySet())
        {
            Ref ref = entry.getValue();
            RevObject object = rw.parseAny(ref.getObjectId());
            String name = entry.getKey();
            if (fullName && !StringUtils.isEmpty(refs))
            {
                name = refs + name;
            }
            list.add(new RefModel(name, ref, object));
        }
        rw.dispose();
        Collections.sort(list);
        Collections.reverse(list);
        if (maxCount > 0 && list.size() > maxCount)
        {
            if (offset < 0)
            {
                offset = 0;
            }
            int endIndex = offset + maxCount;
            if (endIndex > list.size())
            {
                endIndex = list.size();
            }
            list = new ArrayList<RefModel>(list.subList(offset, endIndex));
        }

        return list;
    }

    public ObjectId getDefaultBranch() throws Exception, NullPointerException {
        ObjectId object = this.gitRepository.resolve(Constants.HEAD);
        if (object == null) {
            List<RefModel> branchModels = getLocalBranches(true, -1);
            if (branchModels.size() > 0) {
                RefModel branch = null;
                Date lastDate = new Date(0);
                for (RefModel branchModel : branchModels) {
                    if (branchModel.getDate().after(lastDate)) {
                        branch = branchModel;
                        lastDate = branch.getDate();
                    }
                }
                object = branch.getReferencedObjectId();
            }
        }
        return object;
    }

    public List<Commit> getCommits() throws IOException, GitAPIException {
        // Lazy attribute.
        if (this.commits == null) {
            try (Git git = new Git(this.gitRepository)) {

                //Iterable<RevCommit> allCommits = git.log().all().call();// for all branches
                Iterable<RevCommit> allCommits= git.log().add(this.gitRepository.resolve("Head")).call();// for master branch only
                this.commits = new ArrayList<>();
                allCommits.forEach(gitCommit -> commits.add(new Commit(this, gitCommit)));
            }
        }

        return this.commits;
    }
}
