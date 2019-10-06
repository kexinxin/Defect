package szz;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.egit.github.core.Issue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;


/**
 * Created by usi on 12/7/16.
 */
public class CsvOut {

    private  String lineSeparator = "\n";

    private  String fileHeader = "BugFix-CommitId,BugInducing-CommitId";


    public void writeToCSV(String fileName, Map<Commit, List<Commit>> blameMap, Map<Commit, List<Issue>> issueMap, String user, String repositoryName ) throws IOException, ParseException {

        FileWriter writer = null;
        CSVPrinter csvPrinter = null;

        File dir=new File("/Users/usi/AllCSV");
        if(!dir.exists()){
            dir.mkdir();
        }
        File tagFile=new File(dir,fileName+".csv");
        if(!tagFile.exists()){
            tagFile.createNewFile();
        }
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(lineSeparator).withHeader(fileHeader.trim());

        writer = new FileWriter(tagFile);
        csvPrinter = new CSVPrinter(writer, csvFileFormat);
        Set<Commit> fixCommit = new HashSet<>();
        Set<Commit> induceCommit = new HashSet<>();

        for( Map.Entry<Commit, List<Commit>> entry : blameMap.entrySet()) {
            Commit key = entry.getKey();


            for (Commit value : entry.getValue()) {
                List<java.io.Serializable> szzOutput= new ArrayList<>();

                fixCommit.add(key);
                szzOutput.add(key.getId());

                //szzOutput.add(key.getCommitter().getName());
                szzOutput.add(value.getId());
                //szzOutput.add(value.getGitCommit().getAuthorIdent().getName());
                csvPrinter.printRecord(szzOutput);

                induceCommit.add(value);

            }



        }
        System.out.println(fixCommit.size());
        System.out.println(induceCommit.size());

      //  new CommitPlot("Bug Inducing relationship", "", fixCommit,induceCommit);
       // CommitPlot.showPlot(fixCommit,induceCommit);
        new CommitPlot("Bug Inducing relationship", "", blameMap, fixCommit.size(),issueMap,user,repositoryName);
        CommitPlot.showPlot(blameMap,fixCommit.size(),issueMap);
        writer.close();
        csvPrinter.close();
    }

}
