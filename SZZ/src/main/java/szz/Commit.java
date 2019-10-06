package szz;

/**
 * Created by usi on 11/28/16.
 */

import com.gitblit.models.PathModel;
import com.gitblit.utils.DiffStatFormatter;
import org.eclipse.jgit.api.errors.GitAPIException ;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.util.ArrayList;
import java.util.List;

public class Commit {

    private String id;
    private PersonIdent committer;
    private String message;
    private Repository repository;
    private RevCommit gitCommit;

    public Commit(Repository repository, RevCommit gitCommit) {
        this.repository = repository;
        this.gitCommit = gitCommit;
        this.id = gitCommit.getName();
        this.message = gitCommit.getFullMessage();
        this.committer = gitCommit.getCommitterIdent();

    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Commit)) {
            return false;
        }
        Commit other = (Commit) obj;
        return this.id.equals(other.id);
    }
    @Override

    public int hashCode() {
        return id.hashCode();
    }
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public PersonIdent getCommitter() {

        return committer;
    }

    public void setCommitter(PersonIdent commiter) {

        this.committer = commiter;
    }

    public String getMessage() {

        return message;
    }
    public RevCommit getGitCommit() {
        return gitCommit;
    }
    public void setMessage(String message) {

        this.message = message;
    }



    public boolean isLikelyBugFixingCommit() {
        return ((this.message.toLowerCase().contains("fixes")   ||
                this.message.toLowerCase().contains("fixed")    ||
                this.message.toLowerCase().contains("closes")   ||
                this.message.toLowerCase().contains("fix")      ||
                this.message.toLowerCase().contains("resolve")  ||
                this.message.toLowerCase().contains("resolves") ||
                this.message.toLowerCase().contains("resolved") ||
                this.message.toLowerCase().contains("closed")) &&
                (this.message.toLowerCase().contains("#") )
//                &&
//                ((this.message.toLowerCase().contains("bug") )||
//                (this.message.toLowerCase().contains("issue") ))
        );
    }

    public List<PathModel.PathChangeModel> getFilesInCommit(boolean calculateDiffStat) throws Exception {
        List<PathModel.PathChangeModel> list = new ArrayList<>();
        RevWalk rw = new RevWalk(this.repository.getGitRepository());
        try {
            if (gitCommit == null) {
                ObjectId object = this.repository.getDefaultBranch();
                gitCommit = rw.parseCommit(object);
            }
            if(gitCommit.getParentCount()==0) return list;
            RevCommit parent = rw.parseCommit(gitCommit.getParent(0).getId());
            DiffStatFormatter df = new DiffStatFormatter(gitCommit.getName(), this.repository.getGitRepository());
            df.setRepository(this.repository.getGitRepository());
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            List<DiffEntry> diffs = df.scan(parent.getTree(), gitCommit.getTree());
            //System.out.println(gitCommit.getId());
            for (DiffEntry diff: diffs) {
                //System.out.println(diff.getNewId().name().toString().equals("0000000000000000000000000000000000000000"));
                if(!diff.getNewId().name().toString().equals("0000000000000000000000000000000000000000")){
                    PathModel.PathChangeModel pcm = PathModel.PathChangeModel.from(diff, gitCommit.getName(), this.repository.getGitRepository());

                    if (calculateDiffStat) {
                        df.format(diff);
                        PathModel.PathChangeModel pathStat = df.getDiffStat().getPath(pcm.path);
                        if (pathStat != null) {
                            pcm.insertions = pathStat.insertions;
                            pcm.deletions = pathStat.deletions;
                        }
                    }
                    list.add(pcm);
                }
            }

        } catch (RevisionSyntaxException | org.eclipse.jgit.errors.MissingObjectException | GitAPIException e) {
            //e.printStackTrace();
        }

        rw.dispose();
        return list;
    }

    @Override public String toString() {
        return "id:\t" + this.id + ", committer:\t" + this.committer.getName() + ", commit message:\t" + this.message;
    }
}