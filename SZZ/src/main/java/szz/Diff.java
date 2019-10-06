package szz;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static szz.Main.flag;

/**
 * Created by usi on 12/2/16.
 */
public class Diff {

    private Repository repository;
    private Commit commit;
    private String path;

    public Diff(Repository repository, Commit commit) {

        this.repository = repository;
        this.commit = commit;


    }

    public void blameOnDiff(BlameResult bResult, int fileCounter,List<Commit>bCommits) throws IOException, GitAPIException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(repository.getGitRepository());
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setDetectRenames(true);
        ObjectReader reader = repository.getGitRepository().newObjectReader();
        List<DiffEntry> diffs;
        if (flag==false) {
            diffs = new Git(repository.getGitRepository()).diff()
                    .setNewTree(new CanonicalTreeParser().resetRoot(reader, commit.getGitCommit().getTree()))
                    .setOldTree(new CanonicalTreeParser().resetRoot(reader, commit.getGitCommit().getParent(0).getTree()))
                    .call();

        }else {
            RevCommit parent= new RevWalk(repository.getGitRepository()).parseCommit(commit.getGitCommit().getParent(0));
            diffs = new Git(repository.getGitRepository()).diff()
                    .setNewTree(new CanonicalTreeParser().resetRoot(reader, commit.getGitCommit().getTree()))
                    .setOldTree(new CanonicalTreeParser().resetRoot(reader, parent.getTree()))
                    .call();
        }

        int filesChanged = diffs.size();

        EditList Elist=df.toFileHeader(diffs.get(fileCounter)).toEditList();

        for(int i=0; i<Elist.size();i++) {
            if ((Elist.get(i).getType().name() == "DELETE") ||
                    (Elist.get(i).getType().name() == "REPLACE")&&
                            (!(Elist.get(i).getLengthA()==0))) {


                try {
                    for (int j = Elist.get(i).getBeginA()+1; j <= Elist.get(i).getEndA(); j++) {
                        try {
                            if (!(bResult.getSourceCommit(j) == null)) {
                                RevCommit blamedCommit = bResult.getSourceCommit(j);

                                bCommits.add(new Commit(repository,blamedCommit));
                                //System.out.println("Blamed commit:  " + blamedCommit  + "   Author:  " + bResult.getSourceAuthor(j));


                            }
                        }catch(NullPointerException nlp){
                            //missing blob on git server
                        }
                    }
                }catch (ArrayIndexOutOfBoundsException e)
                {
                    System.out.println("Entry for this line not found");
                }

            }
        }
    }
}





